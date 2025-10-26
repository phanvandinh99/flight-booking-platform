<?php

namespace App\Http\Controllers\Api\HangHangKhong;

use App\Models\DatVe;
use App\Models\ChuyenBay;
use App\Http\Controllers\Controller;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

use Carbon\Carbon;

class BaoCaoController extends Controller
{
    /**
     * Báo cáo doanh thu theo ngày
     */
    public function doanhThuTheoNgay(Request $request)
    {
        $user = $request->user();

        $startDate = $request->get('tu_ngay', Carbon::now()->startOfMonth());
        $endDate = $request->get('den_ngay', Carbon::now()->endOfMonth());

        $revenue = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })
            ->where('trang_thai', 'da_thanh_toan')
            ->whereBetween('created_at', [$startDate, $endDate])
            ->select(
                DB::raw('DATE(created_at) as ngay'),
                DB::raw('COUNT(*) as so_dat_ve'),
                DB::raw('SUM(tong_tien) as doanh_thu')
            )
            ->groupBy('ngay')
            ->orderBy('ngay')
            ->get();

        return response()->json([
            'data' => $revenue,
            'khoang_thoi_gian' => [
                'tu_ngay' => $startDate,
                'den_ngay' => $endDate
            ]
        ]);
    }

    /**
     * Báo cáo doanh thu theo tuần
     */
    public function doanhThuTheoTuan(Request $request)
    {
        $user = $request->user();

        $startDate = $request->get('tu_ngay', Carbon::now()->startOfMonth());
        $endDate = $request->get('den_ngay', Carbon::now()->endOfMonth());

        $revenue = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })
            ->where('trang_thai', 'da_thanh_toan')
            ->whereBetween('created_at', [$startDate, $endDate])
            ->select(
                DB::raw('YEAR(created_at) as nam'),
                DB::raw('WEEK(created_at) as tuan'),
                DB::raw('COUNT(*) as so_dat_ve'),
                DB::raw('SUM(tong_tien) as doanh_thu')
            )
            ->groupBy('nam', 'tuan')
            ->orderBy('nam')
            ->orderBy('tuan')
            ->get();

        return response()->json([
            'data' => $revenue,
            'khoang_thoi_gian' => [
                'tu_ngay' => $startDate,
                'den_ngay' => $endDate
            ]
        ]);
    }

    /**
     * Báo cáo doanh thu theo tháng
     */
    public function doanhThuTheoThang(Request $request)
    {
        $user = $request->user();

        $startDate = $request->get('tu_ngay', Carbon::now()->startOfYear());
        $endDate = $request->get('den_ngay', Carbon::now()->endOfYear());

        $revenue = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })
            ->where('trang_thai', 'da_thanh_toan')
            ->whereBetween('created_at', [$startDate, $endDate])
            ->select(
                DB::raw('YEAR(created_at) as nam'),
                DB::raw('MONTH(created_at) as thang'),
                DB::raw('COUNT(*) as so_dat_ve'),
                DB::raw('SUM(tong_tien) as doanh_thu')
            )
            ->groupBy('nam', 'thang')
            ->orderBy('nam')
            ->orderBy('thang')
            ->get();

        return response()->json([
            'data' => $revenue,
            'khoang_thoi_gian' => [
                'tu_ngay' => $startDate,
                'den_ngay' => $endDate
            ]
        ]);
    }

    /**
     * Báo cáo theo chuyến bay
     */
    public function baoCaoTheoChuyenBay(Request $request)
    {
        $user = $request->user();

        $startDate = $request->get('tu_ngay', Carbon::now()->startOfMonth());
        $endDate = $request->get('den_ngay', Carbon::now()->endOfMonth());

        $flights = ChuyenBay::where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->whereBetween('gio_khoi_hanh', [$startDate, $endDate])
            ->with(['tuyen_bay.san_bay_di', 'tuyen_bay.san_bay_den'])
            ->withCount(['dat_ve as so_dat_ve' => function ($query) {
                $query->where('trang_thai', 'da_thanh_toan');
            }])
            ->withSum(['dat_ve as tong_doanh_thu' => function ($query) {
                $query->where('trang_thai', 'da_thanh_toan');
            }], 'tong_tien')
            ->get();

        return response()->json([
            'data' => $flights,
            'khoang_thoi_gian' => [
                'tu_ngay' => $startDate,
                'den_ngay' => $endDate
            ]
        ]);
    }

    /**
     * Báo cáo theo hạng vé
     */
    public function baoCaoTheoHangVe(Request $request)
    {
        $user = $request->user();

        $startDate = $request->get('tu_ngay', Carbon::now()->startOfMonth());
        $endDate = $request->get('den_ngay', Carbon::now()->endOfMonth());

        $fareStats = DB::table('dat_ve')
            ->join('chuyen_bay', 'dat_ve.ma_chuyen_bay', '=', 'chuyen_bay.id')
            ->join('gia_ve', 'dat_ve.ma_chuyen_bay', '=', 'gia_ve.ma_chuyen_bay')
            ->where('chuyen_bay.ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->where('dat_ve.trang_thai', 'da_thanh_toan')
            ->whereBetween('dat_ve.created_at', [$startDate, $endDate])
            ->select(
                'gia_ve.hang_ve',
                DB::raw('COUNT(*) as so_dat_ve'),
                DB::raw('SUM(dat_ve.tong_tien) as doanh_thu'),
                DB::raw('AVG(dat_ve.tong_tien) as gia_trung_binh')
            )
            ->groupBy('gia_ve.hang_ve')
            ->get();

        return response()->json([
            'data' => $fareStats,
            'khoang_thoi_gian' => [
                'tu_ngay' => $startDate,
                'den_ngay' => $endDate
            ]
        ]);
    }

    /**
     * Tổng quan báo cáo
     */
    public function tongQuan(Request $request)
    {
        $user = $request->user();

        $startDate = $request->get('tu_ngay', Carbon::now()->startOfMonth());
        $endDate = $request->get('den_ngay', Carbon::now()->endOfMonth());

        // Tổng số chuyến bay
        $totalFlights = ChuyenBay::where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->whereBetween('gio_khoi_hanh', [$startDate, $endDate])
            ->count();

        // Tổng số đặt vé
        $totalBookings = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })
            ->whereBetween('created_at', [$startDate, $endDate])
            ->count();

        // Tổng doanh thu
        $totalRevenue = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })
            ->where('trang_thai', 'da_thanh_toan')
            ->whereBetween('created_at', [$startDate, $endDate])
            ->sum('tong_tien');

        // Tỷ lệ đặt vé thành công
        $successfulBookings = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })
            ->where('trang_thai', 'da_thanh_toan')
            ->whereBetween('created_at', [$startDate, $endDate])
            ->count();

        $successRate = $totalBookings > 0 ? ($successfulBookings / $totalBookings) * 100 : 0;

        return response()->json([
            'data' => [
                'tong_so_chuyen_bay' => $totalFlights,
                'tong_so_dat_ve' => $totalBookings,
                'tong_doanh_thu' => $totalRevenue,
                'ty_le_thanh_cong' => round($successRate, 2),
                'doanh_thu_trung_binh' => $successfulBookings > 0 ? round($totalRevenue / $successfulBookings, 2) : 0
            ],
            'khoang_thoi_gian' => [
                'tu_ngay' => $startDate,
                'den_ngay' => $endDate
            ]
        ]);
    }
}
