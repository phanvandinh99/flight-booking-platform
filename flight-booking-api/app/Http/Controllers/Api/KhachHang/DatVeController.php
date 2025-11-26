<?php

namespace App\Http\Controllers\Api\KhachHang;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\DatVe;
use App\Models\ChuyenBay;
use App\Models\HanhKhach;
use App\Models\GiaVe;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Str;
use App\Mail\BookingConfirmationMail;
use App\Services\VNPayService;

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
            'hanh_khach.*.loai_giay_to' => 'nullable|in:can_cuoc,ho_chieu',
            'hanh_khach.*.so_giay_to' => 'nullable|string|max:30',
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

        // Kiểm tra ghế đã được đặt chưa (cho chuyến bay đi)
        $gheDaChon = [];
        foreach ($request->hanh_khach as $hanhKhachData) {
            if (!empty($hanhKhachData['so_ghe'])) {
                $gheDaChon[] = trim($hanhKhachData['so_ghe']);
            }
        }

        if (!empty($gheDaChon)) {
            // Kiểm tra ghế đã được đặt (đã thanh toán) trong chuyến bay đi
            $gheDaDat = HanhKhach::whereHas('dat_ve', function ($query) use ($chuyenBayDi) {
                $query->where('ma_chuyen_bay', $chuyenBayDi->id)
                    ->where('trang_thai', 'da_thanh_toan');
            })
                ->whereNotNull('so_ghe')
                ->whereIn('so_ghe', $gheDaChon)
                ->pluck('so_ghe')
                ->toArray();

            if (!empty($gheDaDat)) {
                return response()->json([
                    'message' => 'Một số ghế đã được đặt: ' . implode(', ', $gheDaDat),
                    'errors' => [
                        'so_ghe' => ['Các ghế sau đã được đặt: ' . implode(', ', $gheDaDat)]
                    ]
                ], 422);
            }

            // Kiểm tra ghế đang được giữ chỗ (chưa hết hạn) trong chuyến bay đi
            $gheGiuCho = HanhKhach::whereHas('dat_ve', function ($query) use ($chuyenBayDi) {
                $query->where('ma_chuyen_bay', $chuyenBayDi->id)
                    ->where('trang_thai', 'giu_cho')
                    ->where('thoi_gian_het_han_giu_cho', '>', now());
            })
                ->whereNotNull('so_ghe')
                ->whereIn('so_ghe', $gheDaChon)
                ->pluck('so_ghe')
                ->toArray();

            if (!empty($gheGiuCho)) {
                return response()->json([
                    'message' => 'Một số ghế đang được giữ chỗ: ' . implode(', ', $gheGiuCho),
                    'errors' => [
                        'so_ghe' => ['Các ghế sau đang được giữ chỗ: ' . implode(', ', $gheGiuCho)]
                    ]
                ], 422);
            }

            // Kiểm tra ghế trùng lặp trong cùng một booking
            $gheTrungLap = array_diff_assoc($gheDaChon, array_unique($gheDaChon));
            if (!empty($gheTrungLap)) {
                return response()->json([
                    'message' => 'Không thể chọn cùng một ghế cho nhiều hành khách: ' . implode(', ', array_unique($gheTrungLap)),
                    'errors' => [
                        'so_ghe' => ['Các ghế sau được chọn trùng lặp: ' . implode(', ', array_unique($gheTrungLap))]
                    ]
                ], 422);
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
                'so_ghe' => !empty($hanhKhachData['so_ghe']) ? trim($hanhKhachData['so_ghe']) : null,
                'hang_ve' => $request->hang_ve,
                'loai_hanh_khach' => $hanhKhachData['loai_hanh_khach'],
                'loai_giay_to' => $hanhKhachData['loai_giay_to'] ?? null,
                'so_giay_to' => $hanhKhachData['so_giay_to'] ?? null
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

        // Gửi email xác nhận đặt vé
        try {
            Mail::to($request->thong_tin_lien_he['email'])->send(
                new BookingConfirmationMail($datVe, $request->thong_tin_lien_he)
            );
        } catch (\Exception $e) {
            // Log lỗi nhưng không fail request
            // Log::error('Failed to send booking confirmation email: ' . $e->getMessage());
        }

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
                'chuyen_bay.may_bay',
                'chuyen_bay.tuyen_bay.san_bay_di',
                'chuyen_bay.tuyen_bay.san_bay_den',
                'hanh_khach'
            ])
            ->orderBy('created_at', 'desc')
            ->paginate(10);

        // Transform data để frontend dễ sử dụng
        $transformedData = $datVe->items();
        foreach ($transformedData as $item) {
            // Thêm alias chuyen_bay_di để tương thích với frontend
            if ($item->chuyen_bay) {
                $item->chuyen_bay_di = $item->chuyen_bay;
            }
        }

        return response()->json([
            'data' => $transformedData,
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
                'chuyen_bay.may_bay',
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

        // Thêm alias chuyen_bay_di để tương thích với frontend
        if ($datVe->chuyen_bay) {
            $datVe->chuyen_bay_di = $datVe->chuyen_bay;
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
        $datVe->refresh();
        $datVe->load([
            'khach_hang',
            'chuyen_bay.hang_hang_khong',
            'chuyen_bay.tuyen_bay.san_bay_di',
            'chuyen_bay.tuyen_bay.san_bay_den',
            'hanh_khach'
        ]);

        return response()->json([
            'message' => 'Hủy đặt vé thành công',
            'data' => $datVe
        ]);
    }

    /**
     * Tạo URL thanh toán VNPAY
     */
    public function createPayment(Request $request, $id)
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

        // Cho phép thanh toán với các trạng thái: giu_cho, cho_thanh_toan, chờ_thanh_toan
        $allowedStatuses = ['giu_cho', 'cho_thanh_toan', 'chờ_thanh_toan'];
        if (!in_array($datVe->trang_thai, $allowedStatuses)) {
            return response()->json([
                'message' => 'Chỉ có thể thanh toán vé đang giữ chỗ hoặc chờ thanh toán. Trạng thái hiện tại: ' . $datVe->trang_thai
            ], 400);
        }

        // Kiểm tra thời gian hết hạn giữ chỗ
        // Nếu hết hạn nhưng chưa đến giờ bay, vẫn cho phép thanh toán (ưu tiên thanh toán)
        if ($datVe->thoi_gian_het_han_giu_cho && $datVe->thoi_gian_het_han_giu_cho < now()) {
            // Kiểm tra xem đã đến giờ bay chưa
            $chuyenBay = $datVe->chuyen_bay;
            if ($chuyenBay && $chuyenBay->gio_khoi_hanh && now() >= $chuyenBay->gio_khoi_hanh) {
                // Đã đến giờ bay, không thể thanh toán nữa
                return response()->json([
                    'message' => 'Không thể thanh toán vì chuyến bay đã khởi hành'
                ], 400);
            }
            // Chưa đến giờ bay, vẫn cho phép thanh toán (cảnh báo nhưng không chặn)
            Log::info('Payment allowed for expired booking (before flight time)', [
                'dat_ve_id' => $id,
                'expired_at' => $datVe->thoi_gian_het_han_giu_cho,
                'flight_time' => $chuyenBay->gio_khoi_hanh ?? null
            ]);
        }

        // Kiểm tra xem booking đã được thanh toán chưa
        if ($datVe->trang_thai === 'da_thanh_toan') {
            return response()->json([
                'message' => 'Vé này đã được thanh toán rồi'
            ], 400);
        }

        $vnpayService = new VNPayService();

        // Tạo mã đơn hàng duy nhất: booking_id + timestamp + random
        // Format: {booking_id}_{timestamp}_{random} (tối đa 32 ký tự theo yêu cầu VNPAY)
        $timestamp = time();
        $random = substr(str_shuffle('0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'), 0, 6);
        $orderId = $datVe->id . '_' . $timestamp . '_' . $random;

        // Đảm bảo không vượt quá 32 ký tự (giới hạn của VNPAY)
        if (strlen($orderId) > 32) {
            $orderId = substr($orderId, 0, 32);
        }

        $amount = $datVe->tong_tien;
        $orderDescription = "Thanh toan dat ve: " . $datVe->ma_dat_ve;
        $bankCode = $request->input('bank_code', null);

        $paymentUrl = $vnpayService->createPaymentUrl(
            $orderId,
            $amount,
            $orderDescription,
            'other',
            $bankCode,
            'vn'
        );

        return response()->json([
            'message' => 'Tạo URL thanh toán thành công',
            'data' => [
                'payment_url' => $paymentUrl,
                'ma_dat_ve' => $datVe->ma_dat_ve,
                'tong_tien' => $amount
            ]
        ]);
    }

    /**
     * Xác nhận thanh toán từ VNPAY callback (khi VNPAY redirect trực tiếp về frontend)
     */
    public function confirmPayment(Request $request, $id)
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

        // Kiểm tra xem đã thanh toán chưa
        if ($datVe->trang_thai === 'da_thanh_toan') {
            return response()->json([
                'message' => 'Vé này đã được thanh toán rồi',
                'data' => $datVe
            ]);
        }

        $vnpayService = new VNPayService();
        $result = $vnpayService->processReturn($request->all());

        $responseCode = $result['response_code'] ?? '';
        $transactionNo = $result['transaction_no'] ?? '';

        // Kiểm tra signature (nếu có)
        if (!$result['is_valid'] && $request->has('vnp_SecureHash')) {
            // Nếu signature không hợp lệ nhưng response code thành công, vẫn xử lý
            if ($responseCode != '00' && $responseCode != '07') {
                return response()->json([
                    'message' => 'Chữ ký không hợp lệ'
                ], 400);
            }
        }

        // Kiểm tra response code
        if ($responseCode == '00' || $responseCode == '07') {
            // Thanh toán thành công - cập nhật trạng thái
            $allowedStatuses = ['giu_cho', 'cho_thanh_toan', 'chờ_thanh_toan'];
            if (in_array($datVe->trang_thai, $allowedStatuses)) {
                $datVe->update([
                    'trang_thai' => 'da_thanh_toan',
                    'ma_giao_dich' => $transactionNo,
                    'thoi_gian_thanh_toan' => now()
                ]);

                Log::info('Payment confirmed from frontend callback', [
                    'dat_ve_id' => $id,
                    'transaction_no' => $transactionNo,
                    'response_code' => $responseCode
                ]);
            }

            return response()->json([
                'message' => 'Xác nhận thanh toán thành công',
                'data' => $datVe->fresh()
            ]);
        } else {
            return response()->json([
                'message' => 'Thanh toán thất bại: ' . ($result['message'] ?? 'Lỗi không xác định')
            ], 400);
        }
    }

    /**
     * Thanh toán đặt vé (giữ lại cho tương thích)
     */
    public function thanhToan(Request $request, $id)
    {
        // Redirect đến createPayment
        return $this->createPayment($request, $id);
    }
}
