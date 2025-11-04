<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Models\TuyenBay;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class RouteController extends Controller
{
    public function index()
    {
        return response()->json(['data' => TuyenBay::with(['san_bay_di', 'san_bay_den'])->get()]);
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'san_bay_di' => 'required|exists:san_bay,id|different:san_bay_den',
            'san_bay_den' => 'required|exists:san_bay,id',
            'duoc_phe_duyet' => 'sometimes|boolean',
        ]);
        if ($validator->fails()) {
            return response()->json(['message' => 'Dữ liệu không hợp lệ', 'errors' => $validator->errors()], 422);
        }
        $route = TuyenBay::create($validator->validated());
        return response()->json(['message' => 'Tạo tuyến bay thành công', 'data' => $route->load(['san_bay_di', 'san_bay_den'])], 201);
    }

    public function show($id)
    {
        $route = TuyenBay::with(['san_bay_di', 'san_bay_den'])->find($id);
        if (!$route) {
            return response()->json(['message' => 'Không tìm thấy tuyến bay'], 404);
        }
        return response()->json(['data' => $route]);
    }

    public function update(Request $request, $id)
    {
        $route = TuyenBay::find($id);
        if (!$route) {
            return response()->json(['message' => 'Không tìm thấy tuyến bay'], 404);
        }
        $validator = Validator::make($request->all(), [
            'san_bay_di' => 'sometimes|required|exists:san_bay,id|different:san_bay_den',
            'san_bay_den' => 'sometimes|required|exists:san_bay,id',
            'duoc_phe_duyet' => 'sometimes|boolean',
        ]);
        if ($validator->fails()) {
            return response()->json(['message' => 'Dữ liệu không hợp lệ', 'errors' => $validator->errors()], 422);
        }
        $route->update($validator->validated());
        return response()->json(['message' => 'Cập nhật tuyến bay thành công', 'data' => $route->fresh()->load(['san_bay_di', 'san_bay_den'])]);
    }

    public function destroy($id)
    {
        $route = TuyenBay::find($id);
        if (!$route) {
            return response()->json(['message' => 'Không tìm thấy tuyến bay'], 404);
        }
        $route->delete();
        return response()->json(['message' => 'Đã xóa tuyến bay']);
    }

    public function approve($id)
    {
        $route = TuyenBay::find($id);
        if (!$route) {
            return response()->json(['message' => 'Không tìm thấy tuyến bay'], 404);
        }
        $route->update(['duoc_phe_duyet' => true]);
        return response()->json(['message' => 'Đã phê duyệt tuyến bay', 'data' => $route]);
    }

    public function revoke($id)
    {
        $route = TuyenBay::find($id);
        if (!$route) {
            return response()->json(['message' => 'Không tìm thấy tuyến bay'], 404);
        }
        $route->update(['duoc_phe_duyet' => false]);
        return response()->json(['message' => 'Đã thu hồi phê duyệt', 'data' => $route]);
    }
}
