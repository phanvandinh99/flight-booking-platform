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

        // Đảm bảo endDate bao gồm cả ngày cuối (đến 23:59:59)
        $endDateWithTime = Carbon::parse($endDate)->endOfDay();

        $revenue = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })
            ->where('trang_thai', 'da_thanh_toan')
            ->whereNotNull('tong_tien')
            ->where('tong_tien', '>', 0)
            ->whereBetween('created_at', [$startDate, $endDateWithTime])
            ->select(
                DB::raw('DATE(created_at) as ngay'),
                DB::raw('COUNT(*) as so_dat_ve'),
                DB::raw('SUM(CAST(tong_tien AS DECIMAL(12,2))) as doanh_thu')
            )
            ->groupBy('ngay')
            ->orderBy('ngay')
            ->get()
            ->map(function ($item) {
                return [
                    'ngay' => $item->ngay,
                    'so_dat_ve' => (int) $item->so_dat_ve,
                    'doanh_thu' => (float) ($item->doanh_thu ?: 0)
                ];
            });

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

        // Đảm bảo endDate bao gồm cả ngày cuối (đến 23:59:59)
        $endDateWithTime = Carbon::parse($endDate)->endOfDay();

        $revenue = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })
            ->where('trang_thai', 'da_thanh_toan')
            ->whereNotNull('tong_tien')
            ->where('tong_tien', '>', 0)
            ->whereBetween('created_at', [$startDate, $endDateWithTime])
            ->select(
                DB::raw('YEAR(created_at) as nam'),
                DB::raw('WEEK(created_at) as tuan'),
                DB::raw('COUNT(*) as so_dat_ve'),
                DB::raw('SUM(CAST(tong_tien AS DECIMAL(12,2))) as doanh_thu')
            )
            ->groupBy('nam', 'tuan')
            ->orderBy('nam')
            ->orderBy('tuan')
            ->get()
            ->map(function ($item) {
                return [
                    'nam' => (int) $item->nam,
                    'tuan' => (int) $item->tuan,
                    'so_dat_ve' => (int) $item->so_dat_ve,
                    'doanh_thu' => (float) ($item->doanh_thu ?: 0)
                ];
            });

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

        // Đảm bảo endDate bao gồm cả ngày cuối (đến 23:59:59)
        $endDateWithTime = Carbon::parse($endDate)->endOfDay();

        $revenue = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })
            ->where('trang_thai', 'da_thanh_toan')
            ->whereNotNull('tong_tien')
            ->where('tong_tien', '>', 0)
            ->whereBetween('created_at', [$startDate, $endDateWithTime])
            ->select(
                DB::raw('YEAR(created_at) as nam'),
                DB::raw('MONTH(created_at) as thang'),
                DB::raw('COUNT(*) as so_dat_ve'),
                DB::raw('SUM(CAST(tong_tien AS DECIMAL(12,2))) as doanh_thu')
            )
            ->groupBy('nam', 'thang')
            ->orderBy('nam')
            ->orderBy('thang')
            ->get()
            ->map(function ($item) {
                return [
                    'nam' => (int) $item->nam,
                    'thang' => (int) $item->thang,
                    'so_dat_ve' => (int) $item->so_dat_ve,
                    'doanh_thu' => (float) ($item->doanh_thu ?: 0)
                ];
            });

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

        // Đảm bảo endDate bao gồm cả ngày cuối (đến 23:59:59)
        $endDateWithTime = Carbon::parse($endDate)->endOfDay();

        $flights = ChuyenBay::where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->whereBetween('gio_khoi_hanh', [$startDate, $endDateWithTime])
            ->with(['tuyen_bay.san_bay_di', 'tuyen_bay.san_bay_den'])
            ->withCount(['dat_ve as so_dat_ve' => function ($query) {
                $query->where('trang_thai', 'da_thanh_toan')
                    ->whereNotNull('tong_tien')
                    ->where('tong_tien', '>', 0);
            }])
            ->withSum(['dat_ve as tong_doanh_thu' => function ($query) {
                $query->where('trang_thai', 'da_thanh_toan')
                    ->whereNotNull('tong_tien')
                    ->where('tong_tien', '>', 0);
            }], 'tong_tien')
            ->get()
            ->map(function ($flight) {
                return [
                    'id' => $flight->id,
                    'ma_chuyen_bay' => $flight->ma_chuyen_bay,
                    'tuyen_bay' => $flight->tuyen_bay,
                    'so_dat_ve' => (int) ($flight->so_dat_ve ?: 0),
                    'tong_doanh_thu' => (float) ($flight->tong_doanh_thu ?: 0)
                ];
            });

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

        // Đảm bảo endDate bao gồm cả ngày cuối (đến 23:59:59)
        $endDateWithTime = Carbon::parse($endDate)->endOfDay();

        $fareStats = DB::table('dat_ve')
            ->join('chuyen_bay', 'dat_ve.ma_chuyen_bay', '=', 'chuyen_bay.id')
            ->join('gia_ve', 'dat_ve.ma_chuyen_bay', '=', 'gia_ve.ma_chuyen_bay')
            ->where('chuyen_bay.ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->where('dat_ve.trang_thai', 'da_thanh_toan')
            ->whereNotNull('dat_ve.tong_tien')
            ->where('dat_ve.tong_tien', '>', 0)
            ->whereBetween('dat_ve.created_at', [$startDate, $endDateWithTime])
            ->select(
                'gia_ve.hang_ve',
                DB::raw('COUNT(*) as so_dat_ve'),
                DB::raw('SUM(CAST(dat_ve.tong_tien AS DECIMAL(12,2))) as doanh_thu'),
                DB::raw('AVG(CAST(dat_ve.tong_tien AS DECIMAL(12,2))) as gia_trung_binh')
            )
            ->groupBy('gia_ve.hang_ve')
            ->get()
            ->map(function ($item) {
                return [
                    'hang_ve' => $item->hang_ve,
                    'so_dat_ve' => (int) $item->so_dat_ve,
                    'doanh_thu' => (float) ($item->doanh_thu ?: 0),
                    'gia_trung_binh' => (float) ($item->gia_trung_binh ?: 0)
                ];
            });

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

        // Đảm bảo endDate bao gồm cả ngày cuối (đến 23:59:59)
        $endDateWithTime = Carbon::parse($endDate)->endOfDay();

        // Tổng số chuyến bay
        $totalFlights = ChuyenBay::where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->whereBetween('gio_khoi_hanh', [$startDate, $endDateWithTime])
            ->count();

        // Tổng số đặt vé
        $totalBookings = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })
            ->whereBetween('created_at', [$startDate, $endDateWithTime])
            ->count();

        // Tổng doanh thu - chỉ tính các đặt vé đã thanh toán có tong_tien > 0
        $totalRevenue = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })
            ->where('trang_thai', 'da_thanh_toan')
            ->whereNotNull('tong_tien')
            ->where('tong_tien', '>', 0)
            ->whereBetween('created_at', [$startDate, $endDateWithTime])
            ->sum('tong_tien');

        // Tỷ lệ đặt vé thành công - chỉ tính các đặt vé đã thanh toán có tong_tien > 0
        $successfulBookings = DatVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })
            ->where('trang_thai', 'da_thanh_toan')
            ->whereNotNull('tong_tien')
            ->where('tong_tien', '>', 0)
            ->whereBetween('created_at', [$startDate, $endDateWithTime])
            ->count();

        $successRate = $totalBookings > 0 ? ($successfulBookings / $totalBookings) * 100 : 0;

        // Doanh thu trung bình
        $doanhThuTrungBinh = $successfulBookings > 0
            ? round((float)$totalRevenue / $successfulBookings, 2)
            : 0;

        return response()->json([
            'data' => [
                'tong_so_chuyen_bay' => $totalFlights,
                'tong_so_dat_ve' => $totalBookings,
                'tong_doanh_thu' => (float)$totalRevenue ?: 0,
                'ty_le_thanh_cong' => round($successRate, 2),
                'doanh_thu_trung_binh' => $doanhThuTrungBinh
            ],
            'khoang_thoi_gian' => [
                'tu_ngay' => $startDate,
                'den_ngay' => $endDate
            ]
        ]);
    }
}
