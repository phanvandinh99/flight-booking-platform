<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Here is where you can register API routes for your application.
|
*/

// Public routes
Route::post('/login', [AuthController::class, 'login']);

// Protected routes
Route::middleware('auth:sanctum')->group(function () {

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
