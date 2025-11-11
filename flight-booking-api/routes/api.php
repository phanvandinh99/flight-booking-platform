<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\HangHangKhongController;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Here is where you can register API routes for your application.
|
*/

// Public routes
Route::post('/register', [AuthController::class, 'register']);
Route::post('/login', [AuthController::class, 'login'])->name('login');

// Public data routes
Route::get('/airlines', [HangHangKhongController::class, 'index']);
Route::get('/airlines/{id}', [HangHangKhongController::class, 'show']);

// Protected routes
Route::middleware('auth:sanctum')->group(function () {

    // Authentication routes
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::post('/logout-all', [AuthController::class, 'logoutAll']);
    Route::get('/me', [AuthController::class, 'me']);
    Route::put('/profile', [AuthController::class, 'updateProfile']);

    // Chỉ admin mới dùng được
    Route::middleware('role:admin')->prefix('admin')->group(function () {
        // Duyệt/Quản lý hãng hàng không
        Route::get('airlines/pending', [\App\Http\Controllers\Api\Admin\AirlineApprovalController::class, 'pending']);
        Route::post('airlines/{id}/approve', [\App\Http\Controllers\Api\Admin\AirlineApprovalController::class, 'approve']);
        Route::post('airlines/{id}/reject', [\App\Http\Controllers\Api\Admin\AirlineApprovalController::class, 'reject']);
        Route::post('airlines/{id}/activate', [\App\Http\Controllers\Api\Admin\AirlineApprovalController::class, 'activate']);
        Route::post('airlines/{id}/suspend', [\App\Http\Controllers\Api\Admin\AirlineApprovalController::class, 'suspend']);

        // Quản lý sân bay
        Route::apiResource('airports', \App\Http\Controllers\Api\Admin\AirportController::class);

        // Quản lý tuyến bay
        Route::apiResource('routes', \App\Http\Controllers\Api\Admin\RouteController::class);
        Route::post('routes/{id}/approve', [\App\Http\Controllers\Api\Admin\RouteController::class, 'approve']);
        Route::post('routes/{id}/revoke', [\App\Http\Controllers\Api\Admin\RouteController::class, 'revoke']);

        // Giám sát hoạt động hệ thống
        Route::get('monitoring/overview', [\App\Http\Controllers\Api\Admin\SystemMonitorController::class, 'overview']);

        // Báo cáo tổng hợp
        Route::prefix('reports')->group(function () {
            Route::get('revenue/summary', [\App\Http\Controllers\Api\Admin\ReportsController::class, 'revenueSummary']);
            Route::get('revenue/monthly', [\App\Http\Controllers\Api\Admin\ReportsController::class, 'monthlyRevenue']);
            Route::get('top-airlines', [\App\Http\Controllers\Api\Admin\ReportsController::class, 'topAirlines']);
        });

        // Cấu hình hệ thống
        Route::get('config', [\App\Http\Controllers\Api\Admin\ConfigController::class, 'index']);
        Route::post('config', [\App\Http\Controllers\Api\Admin\ConfigController::class, 'store']);
        Route::put('config/{key}', [\App\Http\Controllers\Api\Admin\ConfigController::class, 'update']);
        Route::delete('config/{key}', [\App\Http\Controllers\Api\Admin\ConfigController::class, 'destroy']);
    });

    // Chỉ đại diện hãng
    Route::middleware('role:dai_dien_hang')->prefix('airline')->group(function () {
        // Quản lý máy bay
        Route::apiResource('aircrafts', \App\Http\Controllers\Api\HangHangKhong\MayBayController::class);

        // Quản lý chuyến bay
        Route::apiResource('flights', \App\Http\Controllers\Api\HangHangKhong\ChuyenBayController::class);
        Route::get('flights/routes/approved', [\App\Http\Controllers\Api\HangHangKhong\ChuyenBayController::class, 'getApprovedRoutes']);

        // Quản lý giá vé
        Route::get('pricing/flights', [\App\Http\Controllers\Api\HangHangKhong\GiaVeController::class, 'getFlights']);
        Route::apiResource('pricing', \App\Http\Controllers\Api\HangHangKhong\GiaVeController::class);

        // Quản lý đặt vé
        Route::apiResource('bookings', \App\Http\Controllers\Api\HangHangKhong\DatVeController::class);
        Route::put('bookings/{id}/status', [\App\Http\Controllers\Api\HangHangKhong\DatVeController::class, 'updateStatus']);
        Route::get('bookings/statistics', [\App\Http\Controllers\Api\HangHangKhong\DatVeController::class, 'getStatistics']);
        Route::get('bookings/flights', [\App\Http\Controllers\Api\HangHangKhong\DatVeController::class, 'getFlights']);

        // Báo cáo
        Route::prefix('reports')->group(function () {
            Route::get('daily-revenue', [\App\Http\Controllers\Api\HangHangKhong\BaoCaoController::class, 'doanhThuTheoNgay']);
            Route::get('weekly-revenue', [\App\Http\Controllers\Api\HangHangKhong\BaoCaoController::class, 'doanhThuTheoTuan']);
            Route::get('monthly-revenue', [\App\Http\Controllers\Api\HangHangKhong\BaoCaoController::class, 'doanhThuTheoThang']);
            Route::get('flight-report', [\App\Http\Controllers\Api\HangHangKhong\BaoCaoController::class, 'baoCaoTheoChuyenBay']);
            Route::get('fare-class-report', [\App\Http\Controllers\Api\HangHangKhong\BaoCaoController::class, 'baoCaoTheoHangVe']);
            Route::get('overview', [\App\Http\Controllers\Api\HangHangKhong\BaoCaoController::class, 'tongQuan']);
        });
    });

    // Khách hàng
    Route::middleware('role:khach_hang')->prefix('customer')->group(function () {
        // Tìm kiếm chuyến bay
        Route::post('search/flights', [\App\Http\Controllers\Api\KhachHang\TimKiemChuyenBayController::class, 'timKiem']);
        Route::get('search/airports', [\App\Http\Controllers\Api\KhachHang\TimKiemChuyenBayController::class, 'danhSachSanBay']);
        Route::get('search/airlines', [\App\Http\Controllers\Api\KhachHang\TimKiemChuyenBayController::class, 'danhSachHangHangKhong']);
        Route::get('search/flights/{id}', [\App\Http\Controllers\Api\KhachHang\TimKiemChuyenBayController::class, 'chiTietChuyenBay']);

        // Đặt vé (explicit routes)
        Route::post('bookings', [\App\Http\Controllers\Api\KhachHang\DatVeController::class, 'datVe']);
        Route::get('bookings', [\App\Http\Controllers\Api\KhachHang\DatVeController::class, 'danhSachDatVe']);
        Route::get('bookings/{id}', [\App\Http\Controllers\Api\KhachHang\DatVeController::class, 'chiTietDatVe']);
        Route::post('bookings/{id}/payment', [\App\Http\Controllers\Api\KhachHang\DatVeController::class, 'thanhToan']);
        Route::put('bookings/{id}/cancel', [\App\Http\Controllers\Api\KhachHang\DatVeController::class, 'huyDatVe']);
    });
});
