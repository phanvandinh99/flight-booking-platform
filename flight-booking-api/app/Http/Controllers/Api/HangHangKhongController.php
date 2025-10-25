<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\HangHangKhong;

class HangHangKhongController extends Controller
{
    /**
     * Lấy danh sách hãng hàng không (public endpoint)
     */
    public function index(Request $request)
    {
        $hangHangKhong = HangHangKhong::where('trang_thai', 'hoat_dong')
            ->select('id', 'ten_hang', 'ma_hang', 'logo_url')
            ->get();

        return response()->json([
            'data' => $hangHangKhong
        ]);
    }

    /**
     * Lấy chi tiết hãng hàng không
     */
    public function show($id)
    {
        $hangHangKhong = HangHangKhong::find($id);

        if (!$hangHangKhong) {
            return response()->json([
                'message' => 'Không tìm thấy hãng hàng không'
            ], 404);
        }

        return response()->json([
            'data' => $hangHangKhong
        ]);
    }
}
