<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\NguoiDung;
use Illuminate\Support\Facades\Hash;

class AuthController extends Controller
{
    public function login(Request $request)
    {
        // Validate input
        $request->validate([
            'email' => 'required|email',
            'password' => 'required|string',
        ]);

        // Tìm người dùng theo email
        $user = NguoiDung::where('email', $request->email)->first();

        // Kiểm tra tồn tại và mật khẩu
        if (!$user || !Hash::check($request->password, $user->mat_khau)) {
            return response()->json([
                'message' => 'Email hoặc mật khẩu không đúng.'
            ], 401);
        }

        // Tạo token Sanctum
        $token = $user->createToken('flight-booking-token')->plainTextToken;

        return response()->json([
            'message' => 'Đăng nhập thành công',
            'user' => [
                'id' => $user->id,
                'ten_day_du' => $user->ten_day_du,
                'email' => $user->email,
                'vai_tro' => $user->vai_tro,
            ],
            'token' => $token
        ]);
    }
}
