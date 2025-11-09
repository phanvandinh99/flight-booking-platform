<?php

namespace App\Http\Controllers\Api\Admin;

use App\Models\DatVe;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use App\Http\Controllers\Controller;

class ReportsController extends Controller
{
    /**
     * Tổng doanh thu (tất cả các đặt vé đã thanh toán)
     */
    public function revenueSummary(Request $request)
    {
        // Tổng doanh thu từ tất cả các đặt vé đã thanh toán
        $totalRevenue = DatVe::where('trang_thai', 'da_thanh_toan')
            ->sum('tong_tien');

        // Tổng số đặt vé đã thanh toán
        $totalPaidBookings = DatVe::where('trang_thai', 'da_thanh_toan')
            ->count();

        // Tổng số đặt vé (tất cả trạng thái)
        $totalBookings = DatVe::count();

        // Doanh thu trung bình
        $averageRevenue = $totalPaidBookings > 0
            ? round($totalRevenue / $totalPaidBookings, 2)
            : 0;

        return response()->json([
            'data' => [
                'tong_doanh_thu' => $totalRevenue,
                'tong_dat_ve_da_thanh_toan' => $totalPaidBookings,
                'tong_dat_ve' => $totalBookings,
                'doanh_thu_trung_binh' => $averageRevenue
            ]
        ]);
    }

    /**
     * Doanh thu theo tháng
     */
    public function monthlyRevenue(Request $request)
    {
        $startDate = $request->get('tu_ngay', Carbon::now()->startOfYear());
        $endDate = $request->get('den_ngay', Carbon::now()->endOfYear());

        $revenue = DatVe::where('trang_thai', 'da_thanh_toan')
            ->whereBetween('created_at', [$startDate, $endDate])
            ->select(
                DB::raw('YEAR(created_at) as year'),
                DB::raw('MONTH(created_at) as month'),
                DB::raw('COUNT(*) as orders'),
                DB::raw('SUM(tong_tien) as revenue')
            )
            ->groupBy('year', 'month')
            ->orderBy('year')
            ->orderBy('month')
            ->get()
            ->map(function ($item) {
                return [
                    'month' => $item->year . '-' . str_pad($item->month, 2, '0', STR_PAD_LEFT),
                    'revenue' => (float) $item->revenue,
                    'orders' => (int) $item->orders
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
     * Top hãng hàng không theo doanh thu
     */
    public function topAirlines(Request $request)
    {
        $limit = $request->get('limit', 10);

        $topAirlines = DatVe::where('dat_ve.trang_thai', 'da_thanh_toan')
            ->join('chuyen_bay', 'dat_ve.ma_chuyen_bay', '=', 'chuyen_bay.id')
            ->join('hang_hang_khong', 'chuyen_bay.ma_hang_hang_khong', '=', 'hang_hang_khong.id')
            ->select(
                'hang_hang_khong.id',
                'hang_hang_khong.ten_hang',
                'hang_hang_khong.ma_hang',
                DB::raw('COUNT(dat_ve.id) as so_dat_ve'),
                DB::raw('SUM(dat_ve.tong_tien) as tong_doanh_thu')
            )
            ->groupBy('hang_hang_khong.id', 'hang_hang_khong.ten_hang', 'hang_hang_khong.ma_hang')
            ->orderBy('tong_doanh_thu', 'desc')
            ->limit($limit)
            ->get()
            ->map(function ($item) {
                return [
                    'id' => $item->id,
                    'ten_hang' => $item->ten_hang,
                    'ma_hang' => $item->ma_hang,
                    'so_dat_ve' => (int) $item->so_dat_ve,
                    'tong_doanh_thu' => (float) $item->tong_doanh_thu,
                    'doanh_thu_trung_binh' => $item->so_dat_ve > 0
                        ? round($item->tong_doanh_thu / $item->so_dat_ve, 2)
                        : 0
                ];
            });

        return response()->json([
            'data' => $topAirlines
        ]);
    }
}
