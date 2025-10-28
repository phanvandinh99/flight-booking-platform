<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\NguoiDung;
use App\Models\HangHangKhong;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;

class AuthController extends Controller
{
    public function register(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'ten_day_du' => 'required|string|max:255',
            'email' => 'required|string|email|max:255|unique:nguoi_dung',
            'so_dien_thoai' => 'nullable|string|max:20',
            'password' => 'required|string|min:8|confirmed',
            'vai_tro' => 'required|in:khach_hang,dai_dien_hang',
            'ma_hang_hang_khong' => 'required_if:vai_tro,dai_dien_hang|nullable|exists:hang_hang_khong,id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Dữ liệu không hợp lệ',
                'errors' => $validator->errors()
            ], 422);
        }

        // Tạo người dùng mới
        $user = NguoiDung::create([
            'ten_day_du' => $request->ten_day_du,
            'email' => $request->email,
            'so_dien_thoai' => $request->so_dien_thoai,
            'mat_khau' => Hash::make($request->password),
            'vai_tro' => $request->vai_tro,
            'ma_hang_hang_khong' => $request->vai_tro === 'dai_dien_hang' ? $request->ma_hang_hang_khong : null,
        ]);

        // Tạo token
        $token = $user->createToken('flight-booking-token')->plainTextToken;

        return response()->json([
            'message' => 'Đăng ký thành công',
            'user' => [
                'id' => $user->id,
                'ten_day_du' => $user->ten_day_du,
                'email' => $user->email,
                'so_dien_thoai' => $user->so_dien_thoai,
                'vai_tro' => $user->vai_tro,
                'ma_hang_hang_khong' => $user->ma_hang_hang_khong,
            ],
            'token' => $token
        ], 201);
    }

    public function login(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'email' => 'required|email',
            'password' => 'required|string',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Dữ liệu không hợp lệ',
                'errors' => $validator->errors()
            ], 422);
        }

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
                'so_dien_thoai' => $user->so_dien_thoai,
                'vai_tro' => $user->vai_tro,
                'ma_hang_hang_khong' => $user->ma_hang_hang_khong,
            ],
            'token' => $token
        ]);
    }

    public function logout(Request $request)
    {
        // Xóa token hiện tại
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'message' => 'Đăng xuất thành công'
        ]);
    }

    public function logoutAll(Request $request)
    {
        // Xóa tất cả token của user
        $request->user()->tokens()->delete();

        return response()->json([
            'message' => 'Đăng xuất tất cả thiết bị thành công'
        ]);
    }

    public function me(Request $request)
    {
        $user = $request->user();
        
        return response()->json([
            'user' => [
                'id' => $user->id,
                'ten_day_du' => $user->ten_day_du,
                'email' => $user->email,
                'so_dien_thoai' => $user->so_dien_thoai,
                'vai_tro' => $user->vai_tro,
                'ma_hang_hang_khong' => $user->ma_hang_hang_khong,
                'hang_hang_khong' => $user->hang_hang_khong,
            ]
        ]);
    }

    public function updateProfile(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'ten_day_du' => 'sometimes|required|string|max:255',
            'so_dien_thoai' => 'sometimes|nullable|string|max:20',
            'password' => 'sometimes|nullable|string|min:8|confirmed',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Dữ liệu không hợp lệ',
                'errors' => $validator->errors()
            ], 422);
        }

        $updateData = $request->only(['ten_day_du', 'so_dien_thoai']);
        
        if ($request->filled('password')) {
            $updateData['mat_khau'] = Hash::make($request->password);
        }

        $user->update($updateData);

        return response()->json([
            'message' => 'Cập nhật thông tin thành công',
            'user' => [
                'id' => $user->id,
                'ten_day_du' => $user->ten_day_du,
                'email' => $user->email,
                'so_dien_thoai' => $user->so_dien_thoai,
                'vai_tro' => $user->vai_tro,
                'ma_hang_hang_khong' => $user->ma_hang_hang_khong,
            ]
        ]);
    }
}
