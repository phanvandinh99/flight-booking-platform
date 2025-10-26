<?php

namespace App\Http\Controllers\Api\HangHangKhong;

use App\Models\MayBay;
use App\Http\Controllers\Controller;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class MayBayController extends Controller
{
    /**
     * Lấy danh sách máy bay của hãng
     */
    public function index(Request $request)
    {
        $user = $request->user();
        $aircrafts = MayBay::where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->with('hang_hang_khong')
            ->get();

        return response()->json([
            'data' => $aircrafts
        ]);
    }

    /**
     * Lấy chi tiết máy bay
     */
    public function show(Request $request, $id)
    {
        $user = $request->user();
        $aircraft = MayBay::where('id', $id)
            ->where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->with('hang_hang_khong')
            ->first();

        if (!$aircraft) {
            return response()->json([
                'message' => 'Không tìm thấy máy bay'
            ], 404);
        }

        return response()->json([
            'data' => $aircraft
        ]);
    }

    /**
     * Thêm máy bay mới
     */
    public function store(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'loai_may_bay' => 'required|string|max:255',
            'tong_so_ghe' => 'required|integer|min:1|max:1000',
            'so_do_ghe' => 'nullable|array'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Dữ liệu không hợp lệ',
                'errors' => $validator->errors()
            ], 422);
        }

        $aircraft = MayBay::create([
            'ma_hang_hang_khong' => $user->ma_hang_hang_khong,
            'loai_may_bay' => $request->loai_may_bay,
            'tong_so_ghe' => $request->tong_so_ghe,
            'so_do_ghe' => $request->so_do_ghe ?? []
        ]);

        return response()->json([
            'message' => 'Thêm máy bay thành công',
            'data' => $aircraft->load('hang_hang_khong')
        ], 201);
    }

    /**
     * Cập nhật thông tin máy bay
     */
    public function update(Request $request, $id)
    {
        $user = $request->user();
        $aircraft = MayBay::where('id', $id)
            ->where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->first();

        if (!$aircraft) {
            return response()->json([
                'message' => 'Không tìm thấy máy bay'
            ], 404);
        }

        $validator = Validator::make($request->all(), [
            'loai_may_bay' => 'sometimes|required|string|max:255',
            'tong_so_ghe' => 'sometimes|required|integer|min:1|max:1000',
            'so_do_ghe' => 'sometimes|nullable|array'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Dữ liệu không hợp lệ',
                'errors' => $validator->errors()
            ], 422);
        }

        $aircraft->update($request->only([
            'loai_may_bay',
            'tong_so_ghe',
            'so_do_ghe'
        ]));

        return response()->json([
            'message' => 'Cập nhật máy bay thành công',
            'data' => $aircraft->load('hang_hang_khong')
        ]);
    }

    /**
     * Xóa máy bay
     */
    public function destroy(Request $request, $id)
    {
        $user = $request->user();
        $aircraft = MayBay::where('id', $id)
            ->where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->first();

        if (!$aircraft) {
            return response()->json([
                'message' => 'Không tìm thấy máy bay'
            ], 404);
        }

        // Kiểm tra xem máy bay có đang được sử dụng trong chuyến bay không
        if ($aircraft->chuyen_bay()->exists()) {
            return response()->json([
                'message' => 'Không thể xóa máy bay đang được sử dụng trong chuyến bay'
            ], 400);
        }

        $aircraft->delete();

        return response()->json([
            'message' => 'Xóa máy bay thành công'
        ]);
    }
}
