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
Route::post('/login', [AuthController::class, 'login']);

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
    Route::middleware('role:admin')->group(function () {
        //
    });

    // Chỉ đại diện hãng
    Route::middleware('role:dai_dien_hang')->prefix('airline')->group(function () {
        // Quản lý máy bay
        Route::apiResource('aircrafts', \App\Http\Controllers\Api\HangHangKhong\MayBayController::class);

        // Quản lý chuyến bay
        Route::apiResource('flights', \App\Http\Controllers\Api\HangHangKhong\ChuyenBayController::class);
        Route::get('flights/routes/approved', [\App\Http\Controllers\Api\HangHangKhong\ChuyenBayController::class, 'getApprovedRoutes']);

        // Quản lý giá vé
        Route::apiResource('pricing', \App\Http\Controllers\Api\HangHangKhong\GiaVeController::class);
        Route::get('pricing/flights', [\App\Http\Controllers\Api\HangHangKhong\GiaVeController::class, 'getFlights']);

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
    Route::middleware('role:khach_hang')->group(function () {
        //
    });
});
