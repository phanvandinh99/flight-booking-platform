<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Models\DatVe;
use Illuminate\Support\Facades\DB;

class ReportsController extends Controller
{
    public function revenueSummary()
    {
        $totalRevenue = (float) DatVe::where('trang_thai', 'da_thanh_toan')->sum('tong_tien');
        $totalOrders = DatVe::where('trang_thai', 'da_thanh_toan')->count();
        return response()->json([
            'data' => [
                'totalRevenue' => $totalRevenue,
                'totalPaidOrders' => $totalOrders,
            ]
        ]);
    }

    public function monthlyRevenue()
    {
        $rows = DatVe::select(
            DB::raw('strftime("%Y-%m", created_at) as month'),
            DB::raw('SUM(tong_tien) as revenue'),
            DB::raw('COUNT(*) as orders')
        )
            ->where('trang_thai', 'da_thanh_toan')
            ->groupBy('month')
            ->orderBy('month')
            ->get();
        return response()->json(['data' => $rows]);
    }

    public function topAirlines()
    {
        $rows = DatVe::join('chuyen_bay', 'dat_ve.ma_chuyen_bay', '=', 'chuyen_bay.id')
            ->join('hang_hang_khong', 'chuyen_bay.ma_hang_hang_khong', '=', 'hang_hang_khong.id')
            ->where('dat_ve.trang_thai', 'da_thanh_toan')
            ->groupBy('hang_hang_khong.id', 'hang_hang_khong.ten_hang', 'hang_hang_khong.ma_hang')
            ->select('hang_hang_khong.id', 'hang_hang_khong.ten_hang', 'hang_hang_khong.ma_hang', DB::raw('SUM(dat_ve.tong_tien) as revenue'))
            ->orderByDesc('revenue')
            ->limit(10)
            ->get();
        return response()->json(['data' => $rows]);
    }
}
