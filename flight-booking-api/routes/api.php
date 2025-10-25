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
    Route::middleware('role:dai_dien_hang')->group(function () {
        //
    });

    // Khách hàng
    Route::middleware('role:khach_hang')->group(function () {
        //
    });
});
