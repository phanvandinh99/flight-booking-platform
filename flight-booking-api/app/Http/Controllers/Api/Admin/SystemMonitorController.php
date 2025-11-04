<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Models\DatVe;
use App\Models\ChuyenBay;
use App\Models\HangHangKhong;

class SystemMonitorController extends Controller
{
    public function overview()
    {
        $totalBookings = DatVe::count();
        $bookingsPaid = DatVe::where('trang_thai', 'da_thanh_toan')->count();
        $totalFlights = ChuyenBay::count();
        $airlinesActive = HangHangKhong::where('trang_thai', 'hoat_dong')->count();
        $airlinesSuspended = HangHangKhong::where('trang_thai', 'dinh_chi')->count();

        return response()->json([
            'data' => [
                'searchCount' => 0, // Chưa tracking; có thể bổ sung sau
                'totalBookings' => $totalBookings,
                'bookingsPaid' => $bookingsPaid,
                'totalFlights' => $totalFlights,
                'airlines' => [
                    'active' => $airlinesActive,
                    'suspended' => $airlinesSuspended,
                ],
            ]
        ]);
    }
}
