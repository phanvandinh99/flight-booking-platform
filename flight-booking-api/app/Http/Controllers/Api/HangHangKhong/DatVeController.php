<?php

namespace App\Http\Controllers\Api\HangHangKhong;

use App\Models\DatVe;
use App\Models\ChuyenBay;
use App\Http\Controllers\Controller;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

use Carbon\Carbon;

class DatVeController extends Controller
{
    /**
     * Lấy danh sách đặt vé của hãng
     */
    public function index(Request $request)
    {
        $user = $request->user();

        $query = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })->with([
            'khach_hang',
            'chuyen_bay.hang_hang_khong',
            'chuyen_bay.tuyen_bay.san_bay_di',
            'chuyen_bay.tuyen_bay.san_bay_den',
            'hanh_khach'
        ]);

        // Filter theo chuyến bay
        if ($request->has('ma_chuyen_bay')) {
            $query->where('ma_chuyen_bay', $request->ma_chuyen_bay);
        }

        // Filter theo trạng thái
        if ($request->has('trang_thai')) {
            $query->where('trang_thai', $request->trang_thai);
        }

        // Filter theo ngày
        if ($request->has('ngay_dat')) {
            $query->whereDate('created_at', $request->ngay_dat);
        }

        // Filter theo mã đặt vé
        if ($request->has('ma_dat_ve')) {
            $query->where('ma_dat_ve', 'like', '%' . $request->ma_dat_ve . '%');
        }

        $bookings = $query->orderBy('created_at', 'desc')->paginate(20);

        return response()->json([
            'data' => $bookings->items(),
            'pagination' => [
                'current_page' => $bookings->currentPage(),
                'last_page' => $bookings->lastPage(),
                'per_page' => $bookings->perPage(),
                'total' => $bookings->total()
            ]
        ]);
    }

    /**
     * Lấy chi tiết đặt vé
     */
    public function show(Request $request, $id)
    {
        $user = $request->user();
        $booking = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })->with([
            'khach_hang',
            'chuyen_bay.hang_hang_khong',
            'chuyen_bay.tuyen_bay.san_bay_di',
            'chuyen_bay.tuyen_bay.san_bay_den',
            'hanh_khach'
        ])
            ->find($id);

        if (!$booking) {
            return response()->json([
                'message' => 'Không tìm thấy đặt vé'
            ], 404);
        }

        return response()->json([
            'data' => $booking
        ]);
    }

    /**
     * Cập nhật trạng thái đặt vé
     */
    public function updateStatus(Request $request, $id)
    {
        $user = $request->user();
        $booking = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })->find($id);

        if (!$booking) {
            return response()->json([
                'message' => 'Không tìm thấy đặt vé'
            ], 404);
        }

        $validator = Validator::make($request->all(), [
            'trang_thai' => 'required|in:giu_cho,da_thanh_toan,da_huy'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Dữ liệu không hợp lệ',
                'errors' => $validator->errors()
            ], 422);
        }

        $booking->update([
            'trang_thai' => $request->trang_thai
        ]);

        return response()->json([
            'message' => 'Cập nhật trạng thái đặt vé thành công',
            'data' => $booking->load([
                'khach_hang',
                'chuyen_bay.hang_hang_khong',
                'chuyen_bay.tuyen_bay.san_bay_di',
                'chuyen_bay.tuyen_bay.san_bay_den',
                'hanh_khach'
            ])
        ]);
    }

    /**
     * Lấy thống kê đặt vé
     */
    public function getStatistics(Request $request)
    {
        $user = $request->user();

        // Filter theo khoảng thời gian
        $startDate = $request->get('tu_ngay', Carbon::now()->startOfMonth());
        $endDate = $request->get('den_ngay', Carbon::now()->endOfMonth());

        // Đảm bảo endDate bao gồm cả ngày cuối (đến 23:59:59)
        $endDateWithTime = Carbon::parse($endDate)->endOfDay();

        $baseQuery = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })->whereBetween('created_at', [$startDate, $endDateWithTime]);

        // Tổng số đặt vé
        $tongSoDatVe = (clone $baseQuery)->count();

        // Đã thanh toán
        $daThanhToanQuery = (clone $baseQuery)->where('trang_thai', 'da_thanh_toan');
        $daThanhToan = $daThanhToanQuery->count();

        // Giữ chỗ
        $giuCho = (clone $baseQuery)->where('trang_thai', 'giu_cho')->count();

        // Đã hủy
        $daHuy = (clone $baseQuery)->where('trang_thai', 'da_huy')->count();

        // Tổng doanh thu - chỉ tính các đặt vé đã thanh toán có tong_tien > 0
        $tongDoanhThu = (clone $baseQuery)
            ->where('trang_thai', 'da_thanh_toan')
            ->whereNotNull('tong_tien')
            ->where('tong_tien', '>', 0)
            ->sum('tong_tien');

        // Doanh thu trung bình
        $doanhThuTrungBinh = 0;
        if ($daThanhToan > 0) {
            $totalRevenueForAvg = (clone $baseQuery)
                ->where('trang_thai', 'da_thanh_toan')
                ->whereNotNull('tong_tien')
                ->where('tong_tien', '>', 0)
                ->avg('tong_tien');
            $doanhThuTrungBinh = round((float)$totalRevenueForAvg, 2);
        }

        $statistics = [
            'tong_so_dat_ve' => $tongSoDatVe,
            'da_thanh_toan' => $daThanhToan,
            'giu_cho' => $giuCho,
            'da_huy' => $daHuy,
            'tong_doanh_thu' => (float)$tongDoanhThu ?: 0,
            'doanh_thu_trung_binh' => $doanhThuTrungBinh
        ];

        return response()->json([
            'data' => $statistics,
            'khoang_thoi_gian' => [
                'tu_ngay' => $startDate,
                'den_ngay' => $endDate
            ]
        ]);
    }

    /**
     * Lấy danh sách chuyến bay của hãng để xem đặt vé
     */
    public function getFlights()
    {
        $user = request()->user();
        $flights = ChuyenBay::where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->with([
                'tuyen_bay.san_bay_di',
                'tuyen_bay.san_bay_den'
            ])
            ->get();

        return response()->json([
            'data' => $flights
        ]);
    }
}
