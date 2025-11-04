<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Models\SanBay;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class AirportController extends Controller
{
    public function index()
    {
        return response()->json(['data' => SanBay::all()]);
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'ma_san_bay' => 'required|string|max:10|unique:san_bay,ma_san_bay',
            'ten_san_bay' => 'required|string|max:255',
            'thanh_pho' => 'required|string|max:255',
            'quoc_gia' => 'required|string|max:255',
        ]);
        if ($validator->fails()) {
            return response()->json(['message' => 'Dữ liệu không hợp lệ', 'errors' => $validator->errors()], 422);
        }

        $airport = SanBay::create($validator->validated());
        return response()->json(['message' => 'Tạo sân bay thành công', 'data' => $airport], 201);
    }

    public function show($id)
    {
        $airport = SanBay::find($id);
        if (!$airport) {
            return response()->json(['message' => 'Không tìm thấy sân bay'], 404);
        }
        return response()->json(['data' => $airport]);
    }

    public function update(Request $request, $id)
    {
        $airport = SanBay::find($id);
        if (!$airport) {
            return response()->json(['message' => 'Không tìm thấy sân bay'], 404);
        }
        $validator = Validator::make($request->all(), [
            'ma_san_bay' => 'sometimes|required|string|max:10|unique:san_bay,ma_san_bay,' . $airport->id,
            'ten_san_bay' => 'sometimes|required|string|max:255',
            'thanh_pho' => 'sometimes|required|string|max:255',
            'quoc_gia' => 'sometimes|required|string|max:255',
        ]);
        if ($validator->fails()) {
            return response()->json(['message' => 'Dữ liệu không hợp lệ', 'errors' => $validator->errors()], 422);
        }
        $airport->update($validator->validated());
        return response()->json(['message' => 'Cập nhật sân bay thành công', 'data' => $airport]);
    }

    public function destroy($id)
    {
        $airport = SanBay::find($id);
        if (!$airport) {
            return response()->json(['message' => 'Không tìm thấy sân bay'], 404);
        }
        $airport->delete();
        return response()->json(['message' => 'Đã xóa sân bay']);
    }
}
