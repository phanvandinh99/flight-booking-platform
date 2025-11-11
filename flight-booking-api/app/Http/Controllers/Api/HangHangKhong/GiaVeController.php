<?php

namespace App\Http\Controllers\Api\HangHangKhong;

use App\Models\GiaVe;
use App\Models\ChuyenBay;
use App\Http\Controllers\Controller;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class GiaVeController extends Controller
{
    /**
     * Lấy danh sách giá vé của hãng
     */
    public function index(Request $request)
    {
        $user = $request->user();

        $query = GiaVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })->with(['chuyen_bay.hang_hang_khong', 'chuyen_bay.tuyen_bay.san_bay_di', 'chuyen_bay.tuyen_bay.san_bay_den']);

        // Filter theo chuyến bay
        if ($request->has('ma_chuyen_bay')) {
            $query->where('ma_chuyen_bay', $request->ma_chuyen_bay);
        }

        // Filter theo hạng vé
        if ($request->has('hang_ve')) {
            $query->where('hang_ve', $request->hang_ve);
        }

        // Filter theo ngày
        if ($request->has('ngay_bat_dau')) {
            $query->where('ngay_bat_dau', '<=', $request->ngay_bat_dau)
                ->where('ngay_ket_thuc', '>=', $request->ngay_bat_dau);
        }

        $prices = $query->orderBy('ngay_bat_dau', 'desc')->paginate(20);

        return response()->json([
            'data' => $prices->items(),
            'pagination' => [
                'current_page' => $prices->currentPage(),
                'last_page' => $prices->lastPage(),
                'per_page' => $prices->perPage(),
                'total' => $prices->total()
            ]
        ]);
    }

    /**
     * Lấy chi tiết giá vé
     */
    public function show(Request $request, $id)
    {
        $user = $request->user();
        $price = GiaVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })->with([
            'chuyen_bay.hang_hang_khong',
            'chuyen_bay.tuyen_bay.san_bay_di',
            'chuyen_bay.tuyen_bay.san_bay_den'
        ])
            ->find($id);

        if (!$price) {
            return response()->json([
                'message' => 'Không tìm thấy giá vé'
            ], 404);
        }

        return response()->json([
            'data' => $price
        ]);
    }

    /**
     * Tạo giá vé mới
     */
    public function store(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'ma_chuyen_bay' => 'required|exists:chuyen_bay,id',
            'hang_ve' => 'required|in:pho_thong,thuong_gia,hang_nhat',
            'gia' => 'required|numeric|min:0',
            'hanh_ly_ky_gui' => 'required|string|max:50',
            'chinh_sach_huy_ve' => 'nullable|string',
            'chinh_sach_doi_ve' => 'nullable|string',
            'ngay_bat_dau' => 'required|date',
            'ngay_ket_thuc' => 'required|date|after:ngay_bat_dau'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Dữ liệu không hợp lệ',
                'errors' => $validator->errors()
            ], 422);
        }

        // Kiểm tra chuyến bay thuộc về hãng
        $flight = ChuyenBay::where('id', $request->ma_chuyen_bay)
            ->where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->first();

        if (!$flight) {
            return response()->json([
                'message' => 'Chuyến bay không thuộc về hãng của bạn'
            ], 400);
        }

        // Kiểm tra trùng lặp giá vé
        $existingPrice = GiaVe::where('ma_chuyen_bay', $request->ma_chuyen_bay)
            ->where('hang_ve', $request->hang_ve)
            ->where('ngay_bat_dau', $request->ngay_bat_dau)
            ->first();

        if ($existingPrice) {
            return response()->json([
                'message' => 'Đã tồn tại giá vé cho hạng này trong khoảng thời gian này'
            ], 400);
        }

        $price = GiaVe::create([
            'ma_chuyen_bay' => $request->ma_chuyen_bay,
            'hang_ve' => $request->hang_ve,
            'gia' => $request->gia,
            'hanh_ly_ky_gui' => $request->hanh_ly_ky_gui,
            'chinh_sach_huy_ve' => $request->chinh_sach_huy_ve,
            'chinh_sach_doi_ve' => $request->chinh_sach_doi_ve,
            'ngay_bat_dau' => $request->ngay_bat_dau,
            'ngay_ket_thuc' => $request->ngay_ket_thuc
        ]);

        return response()->json([
            'message' => 'Tạo giá vé thành công',
            'data' => $price->load([
                'chuyen_bay.hang_hang_khong',
                'chuyen_bay.tuyen_bay.san_bay_di',
                'chuyen_bay.tuyen_bay.san_bay_den'
            ])
        ], 201);
    }

    /**
     * Cập nhật giá vé
     */
    public function update(Request $request, $id)
    {
        $user = $request->user();
        $price = GiaVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })->find($id);

        if (!$price) {
            return response()->json([
                'message' => 'Không tìm thấy giá vé'
            ], 404);
        }

        $validator = Validator::make($request->all(), [
            'ma_chuyen_bay' => 'sometimes|required|exists:chuyen_bay,id',
            'hang_ve' => 'sometimes|required|in:pho_thong,thuong_gia,hang_nhat',
            'gia' => 'sometimes|required|numeric|min:0',
            'hanh_ly_ky_gui' => 'sometimes|required|string|max:50',
            'chinh_sach_huy_ve' => 'sometimes|nullable|string',
            'chinh_sach_doi_ve' => 'sometimes|nullable|string',
            'ngay_bat_dau' => 'sometimes|required|date',
            'ngay_ket_thuc' => [
                'sometimes',
                'required',
                'date',
                function ($attribute, $value, $fail) use ($request, $price) {
                    // Lấy ngày bắt đầu từ request hoặc từ giá vé hiện tại
                    $ngayBatDau = null;
                    if ($request->has('ngay_bat_dau')) {
                        $ngayBatDau = $request->ngay_bat_dau;
                    } elseif ($price && $price->ngay_bat_dau) {
                        // Format date từ model (Carbon instance)
                        $ngayBatDau = $price->ngay_bat_dau instanceof \Carbon\Carbon
                            ? $price->ngay_bat_dau->format('Y-m-d')
                            : $price->ngay_bat_dau;
                    }

                    if ($value && $ngayBatDau && strtotime($value) <= strtotime($ngayBatDau)) {
                        $fail('Ngày kết thúc phải sau ngày bắt đầu.');
                    }
                }
            ]
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Dữ liệu không hợp lệ',
                'errors' => $validator->errors()
            ], 422);
        }

        // Kiểm tra chuyến bay thuộc về hãng (nếu có cập nhật)
        if ($request->has('ma_chuyen_bay')) {
            $flight = ChuyenBay::where('id', $request->ma_chuyen_bay)
                ->where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
                ->first();

            if (!$flight) {
                return response()->json([
                    'message' => 'Chuyến bay không thuộc về hãng của bạn'
                ], 400);
            }
        }

        // Kiểm tra unique constraint khi cập nhật ngày bắt đầu hoặc hạng vé
        $maChuyenBay = $request->has('ma_chuyen_bay') ? $request->ma_chuyen_bay : $price->ma_chuyen_bay;
        $hangVe = $request->has('hang_ve') ? $request->hang_ve : $price->hang_ve;
        $ngayBatDau = $request->has('ngay_bat_dau') ? $request->ngay_bat_dau : $price->ngay_bat_dau;

        $existingPrice = GiaVe::where('ma_chuyen_bay', $maChuyenBay)
            ->where('hang_ve', $hangVe)
            ->where('ngay_bat_dau', $ngayBatDau)
            ->where('id', '!=', $id)
            ->first();

        if ($existingPrice) {
            return response()->json([
                'message' => 'Đã tồn tại giá vé cho hạng này trong khoảng thời gian này'
            ], 400);
        }

        // Update tất cả các trường được gửi từ request
        // Frontend luôn gửi đầy đủ các trường, nên chỉ cần lấy từ request
        $updateData = [
            'ma_chuyen_bay' => $request->input('ma_chuyen_bay', $price->ma_chuyen_bay),
            'hang_ve' => $request->input('hang_ve', $price->hang_ve),
            'gia' => $request->input('gia', $price->gia),
            'hanh_ly_ky_gui' => $request->input('hanh_ly_ky_gui', $price->hanh_ly_ky_gui),
            'chinh_sach_huy_ve' => $request->input('chinh_sach_huy_ve', $price->chinh_sach_huy_ve),
            'chinh_sach_doi_ve' => $request->input('chinh_sach_doi_ve', $price->chinh_sach_doi_ve),
            'ngay_bat_dau' => $request->input('ngay_bat_dau', $price->ngay_bat_dau),
            'ngay_ket_thuc' => $request->input('ngay_ket_thuc', $price->ngay_ket_thuc),
        ];

        $price->update($updateData);

        return response()->json([
            'message' => 'Cập nhật giá vé thành công',
            'data' => $price->load([
                'chuyen_bay.hang_hang_khong',
                'chuyen_bay.tuyen_bay.san_bay_di',
                'chuyen_bay.tuyen_bay.san_bay_den'
            ])
        ]);
    }

    /**
     * Xóa giá vé
     */
    public function destroy(Request $request, $id)
    {
        $user = $request->user();
        $price = GiaVe::whereHas('chuyen_bay', function ($q) use ($user) {
            $q->where('ma_hang_hang_khong', $user->ma_hang_hang_khong);
        })->find($id);

        if (!$price) {
            return response()->json([
                'message' => 'Không tìm thấy giá vé'
            ], 404);
        }

        $price->delete();

        return response()->json([
            'message' => 'Xóa giá vé thành công'
        ]);
    }

    /**
     * Lấy danh sách chuyến bay của hãng để tạo giá vé
     */
    public function getFlights()
    {
        $user = request()->user();
        $flights = ChuyenBay::where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->where('trang_thai', 'du_kien')
            ->with([
                'tuyen_bay.san_bay_di',
                'tuyen_bay.san_bay_den'
            ])
            ->get();

        return response()->json([
            'data' => $flights
        ]);
    }
}
