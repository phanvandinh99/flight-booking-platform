<?php

namespace App\Http\Controllers\Api\KhachHang;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\DatVe;
use App\Models\ChuyenBay;
use App\Models\HanhKhach;
use App\Models\GiaVe;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;

class DatVeController extends Controller
{
    /**
     * Đặt vé chuyến bay
     */
    public function datVe(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'ma_chuyen_bay_di' => 'required|exists:chuyen_bay,id',
            'ma_chuyen_bay_ve' => 'nullable|exists:chuyen_bay,id',
            'hang_ve' => 'required|in:pho_thong,thuong_gia,hang_nhat',
            'hanh_khach' => 'required|array|min:1',
            'hanh_khach.*.ho_ten' => 'required|string|max:255',
            'hanh_khach.*.so_ho_chieu' => 'nullable|string|max:20',
            'hanh_khach.*.so_ghe' => 'nullable|string|max:10',
            'hanh_khach.*.loai_hanh_khach' => 'required|in:nguoi_lon,tre_em,em_be',
            'thong_tin_lien_he' => 'required|array',
            'thong_tin_lien_he.email' => 'required|email',
            'thong_tin_lien_he.so_dien_thoai' => 'required|string|max:20',
            'thong_tin_lien_he.ten_day_du' => 'required|string|max:255'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Dữ liệu không hợp lệ',
                'errors' => $validator->errors()
            ], 422);
        }

        $user = $request->user();

        // Kiểm tra chuyến bay đi
        $chuyenBayDi = ChuyenBay::where('id', $request->ma_chuyen_bay_di)
            ->where('trang_thai', 'du_kien')
            ->first();

        if (!$chuyenBayDi) {
            return response()->json([
                'message' => 'Chuyến bay đi không hợp lệ'
            ], 400);
        }

        // Kiểm tra chuyến bay về (nếu có)
        $chuyenBayVe = null;
        if ($request->ma_chuyen_bay_ve) {
            $chuyenBayVe = ChuyenBay::where('id', $request->ma_chuyen_bay_ve)
                ->where('trang_thai', 'du_kien')
                ->first();

            if (!$chuyenBayVe) {
                return response()->json([
                    'message' => 'Chuyến bay về không hợp lệ'
                ], 400);
            }
        }

        // Tính tổng giá vé
        $tongGia = $this->tinhTongGiaVe($chuyenBayDi, $chuyenBayVe, $request->hang_ve, $request->hanh_khach);

        // Tạo mã đặt vé
        $maDatVe = $this->taoMaDatVe();

        // Tạo đặt vé
        $datVe = DatVe::create([
            'ma_khach_hang' => $user->id,
            'ma_chuyen_bay' => $chuyenBayDi->id,
            'ma_dat_ve' => $maDatVe,
            'trang_thai' => 'giu_cho',
            'thoi_gian_het_han_giu_cho' => now()->addMinutes(15), // Giữ chỗ 15 phút
            'tong_tien' => $tongGia
        ]);

        // Tạo thông tin hành khách
        foreach ($request->hanh_khach as $hanhKhachData) {
            HanhKhach::create([
                'ma_dat_ve' => $datVe->id,
                'ho_ten' => $hanhKhachData['ho_ten'],
                'so_ho_chieu' => $hanhKhachData['so_ho_chieu'] ?? null,
                'so_ghe' => $hanhKhachData['so_ghe'] ?? null,
                'hang_ve' => $request->hang_ve,
                'loai_hanh_khach' => $hanhKhachData['loai_hanh_khach']
            ]);
        }

        // Load thông tin đầy đủ
        $datVe->load([
            'khach_hang',
            'chuyen_bay.hang_hang_khong',
            'chuyen_bay.tuyen_bay.san_bay_di',
            'chuyen_bay.tuyen_bay.san_bay_den',
            'hanh_khach'
        ]);

        return response()->json([
            'message' => 'Đặt vé thành công',
            'data' => [
                'dat_ve' => $datVe,
                'thong_tin_lien_he' => $request->thong_tin_lien_he,
                'thoi_gian_het_han' => $datVe->thoi_gian_het_han_giu_cho
            ]
        ], 201);
    }

    /**
     * Tính tổng giá vé
     */
    private function tinhTongGiaVe($chuyenBayDi, $chuyenBayVe, $hangVe, $hanhKhach)
    {
        $tongGia = 0;

        // Tính giá chuyến bay đi
        $giaVeDi = GiaVe::where('ma_chuyen_bay', $chuyenBayDi->id)
            ->where('hang_ve', $hangVe)
            ->where('ngay_bat_dau', '<=', now())
            ->where('ngay_ket_thuc', '>=', now())
            ->first();

        if ($giaVeDi) {
            $tongGia += $this->tinhGiaTheoHanhKhach($giaVeDi->gia, $hanhKhach);
        }

        // Tính giá chuyến bay về (nếu có)
        if ($chuyenBayVe) {
            $giaVeVe = GiaVe::where('ma_chuyen_bay', $chuyenBayVe->id)
                ->where('hang_ve', $hangVe)
                ->where('ngay_bat_dau', '<=', now())
                ->where('ngay_ket_thuc', '>=', now())
                ->first();

            if ($giaVeVe) {
                $tongGia += $this->tinhGiaTheoHanhKhach($giaVeVe->gia, $hanhKhach);
            }
        }

        return round($tongGia, 2);
    }

    /**
     * Tính giá theo loại hành khách
     */
    private function tinhGiaTheoHanhKhach($giaCoBan, $hanhKhach)
    {
        $tongGia = 0;

        foreach ($hanhKhach as $hk) {
            switch ($hk['loai_hanh_khach']) {
                case 'nguoi_lon':
                    $tongGia += $giaCoBan;
                    break;
                case 'tre_em':
                    $tongGia += $giaCoBan * 0.75; // 75% giá người lớn
                    break;
                case 'em_be':
                    $tongGia += $giaCoBan * 0.1; // 10% giá người lớn
                    break;
            }
        }

        return $tongGia;
    }

    /**
     * Tạo mã đặt vé
     */
    private function taoMaDatVe()
    {
        do {
            $maDatVe = strtoupper(Str::random(6));
        } while (DatVe::where('ma_dat_ve', $maDatVe)->exists());

        return $maDatVe;
    }

    /**
     * Lấy danh sách đặt vé của khách hàng
     */
    public function danhSachDatVe(Request $request)
    {
        $user = $request->user();
        
        $datVe = DatVe::where('ma_khach_hang', $user->id)
            ->with([
                'chuyen_bay.hang_hang_khong',
                'chuyen_bay.tuyen_bay.san_bay_di',
                'chuyen_bay.tuyen_bay.san_bay_den',
                'hanh_khach'
            ])
            ->orderBy('created_at', 'desc')
            ->paginate(10);

        return response()->json([
            'data' => $datVe->items(),
            'pagination' => [
                'current_page' => $datVe->currentPage(),
                'last_page' => $datVe->lastPage(),
                'per_page' => $datVe->perPage(),
                'total' => $datVe->total()
            ]
        ]);
    }

    /**
     * Lấy chi tiết đặt vé
     */
    public function chiTietDatVe(Request $request, $id)
    {
        $user = $request->user();
        $datVe = DatVe::where('id', $id)
            ->where('ma_khach_hang', $user->id)
            ->with([
                'khach_hang',
                'chuyen_bay.hang_hang_khong',
                'chuyen_bay.tuyen_bay.san_bay_di',
                'chuyen_bay.tuyen_bay.san_bay_den',
                'hanh_khach'
            ])
            ->first();

        if (!$datVe) {
            return response()->json([
                'message' => 'Không tìm thấy đặt vé'
            ], 404);
        }

        return response()->json([
            'data' => $datVe
        ]);
    }

    /**
     * Hủy đặt vé
     */
    public function huyDatVe(Request $request, $id)
    {
        $user = $request->user();
        $datVe = DatVe::where('id', $id)
            ->where('ma_khach_hang', $user->id)
            ->first();

        if (!$datVe) {
            return response()->json([
                'message' => 'Không tìm thấy đặt vé'
            ], 404);
        }

        if ($datVe->trang_thai === 'da_huy') {
            return response()->json([
                'message' => 'Đặt vé đã được hủy trước đó'
            ], 400);
        }

        if ($datVe->trang_thai === 'da_thanh_toan') {
            return response()->json([
                'message' => 'Không thể hủy vé đã thanh toán. Vui lòng liên hệ hỗ trợ.'
            ], 400);
        }

        $datVe->update(['trang_thai' => 'da_huy']);

        return response()->json([
            'message' => 'Hủy đặt vé thành công'
        ]);
    }

    /**
     * Thanh toán đặt vé
     */
    public function thanhToan(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'phuong_thuc_thanh_toan' => 'required|in:the_ngan_hang,vi_dien_tu,chuyen_khoan',
            'thong_tin_thanh_toan' => 'required|array'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Dữ liệu không hợp lệ',
                'errors' => $validator->errors()
            ], 422);
        }

        $user = $request->user();
        $datVe = DatVe::where('id', $id)
            ->where('ma_khach_hang', $user->id)
            ->first();

        if (!$datVe) {
            return response()->json([
                'message' => 'Không tìm thấy đặt vé'
            ], 404);
        }

        if ($datVe->trang_thai !== 'giu_cho') {
            return response()->json([
                'message' => 'Chỉ có thể thanh toán vé đang giữ chỗ'
            ], 400);
        }

        if ($datVe->thoi_gian_het_han_giu_cho < now()) {
            return response()->json([
                'message' => 'Thời gian giữ chỗ đã hết hạn'
            ], 400);
        }

        // TODO: Tích hợp với cổng thanh toán thực tế
        // Ở đây chỉ mô phỏng thanh toán thành công
        $datVe->update(['trang_thai' => 'da_thanh_toan']);

        return response()->json([
            'message' => 'Thanh toán thành công',
            'data' => [
                'ma_dat_ve' => $datVe->ma_dat_ve,
                'trang_thai' => 'da_thanh_toan',
                'tong_tien' => $datVe->tong_tien,
                'phuong_thuc_thanh_toan' => $request->phuong_thuc_thanh_toan
            ]
        ]);
    }
}

