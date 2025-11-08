<?php

namespace App\Http\Controllers\Api\Admin;

use App\Models\CauHinhHeThong;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class ConfigController extends Controller
{
    public function index()
    {
        return response()->json(['data' => CauHinhHeThong::all()]);
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'ten_cau_hinh' => 'required|string|max:255|unique:cau_hinh_he_thong,ten_cau_hinh',
            'gia_tri' => 'required|string',
        ]);
        if ($validator->fails()) {
            return response()->json(['message' => 'Dữ liệu không hợp lệ', 'errors' => $validator->errors()], 422);
        }
        $config = CauHinhHeThong::create($validator->validated());
        return response()->json(['message' => 'Đã tạo cấu hình', 'data' => $config], 201);
    }

    public function update(Request $request, $key)
    {
        $config = CauHinhHeThong::where('ten_cau_hinh', $key)->first();
        if (!$config) {
            return response()->json(['message' => 'Không tìm thấy cấu hình'], 404);
        }
        $validator = Validator::make($request->all(), [
            'gia_tri' => 'required|string',
        ]);
        if ($validator->fails()) {
            return response()->json(['message' => 'Dữ liệu không hợp lệ', 'errors' => $validator->errors()], 422);
        }
        $config->update(['gia_tri' => $request->gia_tri]);
        return response()->json(['message' => 'Đã cập nhật cấu hình', 'data' => $config]);
    }

    public function destroy($key)
    {
        $config = CauHinhHeThong::where('ten_cau_hinh', $key)->first();
        if (!$config) {
            return response()->json(['message' => 'Không tìm thấy cấu hình'], 404);
        }
        $config->delete();
        return response()->json(['message' => 'Đã xóa cấu hình']);
    }
}

