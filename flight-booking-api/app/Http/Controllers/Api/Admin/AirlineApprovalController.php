<?php

namespace App\Http\Controllers\Api\Admin;

use App\Models\HangHangKhong;
use App\Http\Controllers\Controller;

class AirlineApprovalController extends Controller
{
    public function pending()
    {
        $pending = HangHangKhong::where('trang_thai', 'cho_duyet')
            ->orWhereNull('trang_thai')
            ->get();

        return response()->json(['data' => $pending]);
    }

    public function approve($id)
    {
        $airline = HangHangKhong::find($id);
        if (!$airline) {
            return response()->json(['message' => 'Không tìm thấy hãng hàng không'], 404);
        }
        $airline->update(['trang_thai' => 'hoat_dong']);
        return response()->json(['message' => 'Đã phê duyệt', 'data' => $airline]);
    }

    public function reject($id)
    {
        $airline = HangHangKhong::find($id);
        if (!$airline) {
            return response()->json(['message' => 'Không tìm thấy hãng hàng không'], 404);
        }
        $airline->update(['trang_thai' => 'tu_choi']);
        return response()->json(['message' => 'Đã từ chối', 'data' => $airline]);
    }

    public function activate($id)
    {
        $airline = HangHangKhong::find($id);
        if (!$airline) {
            return response()->json(['message' => 'Không tìm thấy hãng hàng không'], 404);
        }
        $airline->update(['trang_thai' => 'hoat_dong']);
        return response()->json(['message' => 'Đã kích hoạt', 'data' => $airline]);
    }

    public function suspend($id)
    {
        $airline = HangHangKhong::find($id);
        if (!$airline) {
            return response()->json(['message' => 'Không tìm thấy hãng hàng không'], 404);
        }
        $airline->update(['trang_thai' => 'dinh_chi']);
        return response()->json(['message' => 'Đã đình chỉ', 'data' => $airline]);
    }
}
