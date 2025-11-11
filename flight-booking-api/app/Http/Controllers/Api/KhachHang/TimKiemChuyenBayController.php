<?php

namespace App\Http\Controllers\Api\KhachHang;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\ChuyenBay;
use App\Models\SanBay;
use App\Models\TuyenBay;
use App\Models\GiaVe;
use Illuminate\Support\Facades\Validator;
use Carbon\Carbon;

class TimKiemChuyenBayController extends Controller
{
    /**
     * Tìm kiếm chuyến bay
     */
    public function timKiem(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'san_bay_di' => 'required|string',
            'san_bay_den' => 'required|string',
            'ngay_khoi_hanh' => 'required|date|after_or_equal:today',
            'ngay_ve' => 'nullable|date|after:ngay_khoi_hanh',
            'loai_chuyen' => 'required|in:mot_chieu,khứ_hồi',
            'nguoi_lon' => 'required|integer|min:1|max:9',
            'tre_em' => 'integer|min:0|max:9',
            'em_be' => 'integer|min:0|max:9',
            'hang_ve' => 'nullable|in:pho_thong,thuong_gia,hang_nhat',
            'gia_tu' => 'nullable|numeric|min:0',
            'gia_den' => 'nullable|numeric|min:0|gte:gia_tu',
            'gio_khoi_hanh_tu' => 'nullable|date_format:H:i',
            'gio_khoi_hanh_den' => 'nullable|date_format:H:i',
            'hang_hang_khong' => 'nullable|array',
            'hang_hang_khong.*' => 'exists:hang_hang_khong,id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Dữ liệu không hợp lệ',
                'errors' => $validator->errors()
            ], 422);
        }

        // Validate số lượng hành khách
        $tongHanhKhach = $request->nguoi_lon + ($request->tre_em ?? 0) + ($request->em_be ?? 0);
        if ($tongHanhKhach > 9) {
            return response()->json([
                'message' => 'Tổng số hành khách không được vượt quá 9 người'
            ], 422);
        }

        // Validate loại chuyến
        if ($request->loai_chuyen === 'khứ_hồi' && !$request->ngay_ve) {
            return response()->json([
                'message' => 'Vui lòng chọn ngày về cho chuyến khứ hồi'
            ], 422);
        }

        // Tìm sân bay
        $sanBayDi = SanBay::where('ma_san_bay', $request->san_bay_di)->first();
        $sanBayDen = SanBay::where('ma_san_bay', $request->san_bay_den)->first();

        if (!$sanBayDi || !$sanBayDen) {
            return response()->json([
                'message' => 'Không tìm thấy sân bay'
            ], 404);
        }

        // Tìm tuyến bay
        $tuyenBay = TuyenBay::where('san_bay_di', $sanBayDi->id)
            ->where('san_bay_den', $sanBayDen->id)
            ->where('duoc_phe_duyet', true)
            ->first();

        if (!$tuyenBay) {
            return response()->json([
                'message' => 'Không có tuyến bay từ ' . $sanBayDi->ten_san_bay . ' đến ' . $sanBayDen->ten_san_bay
            ], 404);
        }

        // Tìm chuyến bay đi
        $chuyenBayDi = $this->timChuyenBay($tuyenBay, $request, 'di');

        // Tìm chuyến bay về (nếu là khứ hồi)
        $chuyenBayVe = null;
        if ($request->loai_chuyen === 'khứ_hồi') {
            $tuyenBayVe = TuyenBay::where('san_bay_di', $sanBayDen->id)
                ->where('san_bay_den', $sanBayDi->id)
                ->where('duoc_phe_duyet', true)
                ->first();

            if ($tuyenBayVe) {
                $requestVe = clone $request;
                $requestVe->ngay_khoi_hanh = $request->ngay_ve;
                $chuyenBayVe = $this->timChuyenBay($tuyenBayVe, $requestVe, 've');
            }
        }

        return response()->json([
            'data' => [
                'loai_chuyen' => $request->loai_chuyen,
                'san_bay_di' => $sanBayDi,
                'san_bay_den' => $sanBayDen,
                'ngay_khoi_hanh' => $request->ngay_khoi_hanh,
                'ngay_ve' => $request->ngay_ve,
                'hanh_khach' => [
                    'nguoi_lon' => $request->nguoi_lon,
                    'tre_em' => $request->tre_em ?? 0,
                    'em_be' => $request->em_be ?? 0,
                    'tong_so' => $tongHanhKhach
                ],
                'chuyen_bay_di' => $chuyenBayDi,
                'chuyen_bay_ve' => $chuyenBayVe
            ]
        ]);
    }

    /**
     * Tìm chuyến bay theo điều kiện
     */
    private function timChuyenBay($tuyenBay, $request, $loai)
    {
        $query = ChuyenBay::where('ma_tuyen_bay', $tuyenBay->id)
            ->where('trang_thai', 'du_kien')
            ->whereDate('gio_khoi_hanh', $request->ngay_khoi_hanh)
            ->with([
                'hang_hang_khong',
                'may_bay',
                'tuyen_bay.san_bay_di',
                'tuyen_bay.san_bay_den',
                'gia_ve' => function ($query) {
                    $query->where('ngay_bat_dau', '<=', now())
                        ->where('ngay_ket_thuc', '>=', now());
                }
            ]);

        // Filter theo hãng hàng không
        if ($request->hang_hang_khong) {
            $query->whereIn('ma_hang_hang_khong', $request->hang_hang_khong);
        }

        // Filter theo giờ khởi hành
        if ($request->gio_khoi_hanh_tu) {
            $query->whereTime('gio_khoi_hanh', '>=', $request->gio_khoi_hanh_tu);
        }
        if ($request->gio_khoi_hanh_den) {
            $query->whereTime('gio_khoi_hanh', '<=', $request->gio_khoi_hanh_den);
        }

        $chuyenBay = $query->orderBy('gio_khoi_hanh')->get();

        // Filter theo giá vé
        if ($request->gia_tu || $request->gia_den || $request->hang_ve) {
            $chuyenBay = $chuyenBay->filter(function ($chuyen) use ($request) {
                $giaVe = $chuyen->gia_ve->where('hang_ve', $request->hang_ve ?? 'pho_thong')->first();

                if (!$giaVe) return false;

                if ($request->gia_tu && $giaVe->gia < $request->gia_tu) return false;
                if ($request->gia_den && $giaVe->gia > $request->gia_den) return false;

                return true;
            });
        }

        // Tính tổng giá cho từng chuyến bay
        $chuyenBay = $chuyenBay->map(function ($chuyen) use ($request) {
            $giaVe = $chuyen->gia_ve->where('hang_ve', $request->hang_ve ?? 'pho_thong')->first();

            if ($giaVe) {
                $tongGia = $this->tinhTongGia($giaVe->gia, $request);
                $chuyen->tong_gia = $tongGia;
                $chuyen->gia_ve_hien_tai = $giaVe;
            }

            return $chuyen;
        });

        return $chuyenBay->values();
    }

    /**
     * Tính tổng giá vé
     */
    private function tinhTongGia($giaCoBan, $request)
    {
        $tongGia = 0;

        // Người lớn: giá đầy đủ
        $tongGia += $request->nguoi_lon * $giaCoBan;

        // Trẻ em: 75% giá người lớn
        if ($request->tre_em) {
            $tongGia += $request->tre_em * ($giaCoBan * 0.75);
        }

        // Em bé: 10% giá người lớn
        if ($request->em_be) {
            $tongGia += $request->em_be * ($giaCoBan * 0.1);
        }

        return round($tongGia, 2);
    }

    /**
     * Lấy danh sách sân bay
     */
    public function danhSachSanBay()
    {
        $sanBay = SanBay::select('id', 'ma_san_bay', 'ten_san_bay', 'thanh_pho', 'quoc_gia')
            ->orderBy('thanh_pho')
            ->get();

        return response()->json([
            'data' => $sanBay
        ]);
    }

    /**
     * Lấy danh sách hãng hàng không
     */
    public function danhSachHangHangKhong()
    {
        $hangHangKhong = \App\Models\HangHangKhong::where('trang_thai', 'hoat_dong')
            ->select('id', 'ten_hang', 'ma_hang', 'logo_url')
            ->orderBy('ten_hang')
            ->get();

        return response()->json([
            'data' => $hangHangKhong
        ]);
    }

    /**
     * Lấy chi tiết chuyến bay
     */
    public function chiTietChuyenBay($id)
    {
        $chuyenBay = ChuyenBay::with([
            'hang_hang_khong',
            'may_bay',
            'tuyen_bay.san_bay_di',
            'tuyen_bay.san_bay_den',
            'gia_ve' => function ($query) {
                $query->where('ngay_bat_dau', '<=', now())
                    ->where('ngay_ket_thuc', '>=', now());
            }
        ])->find($id);

        if (!$chuyenBay) {
            return response()->json([
                'message' => 'Không tìm thấy chuyến bay'
            ], 404);
        }

        return response()->json([
            'data' => $chuyenBay
        ]);
    }

    /**
     * Lấy danh sách chuyến bay từ hôm nay trở đi
     */
    public function chuyenBayHomNay()
    {
        $today = Carbon::today();

        $chuyenBay = ChuyenBay::where('trang_thai', 'du_kien')
            ->whereDate('gio_khoi_hanh', '>=', $today)
            ->with([
                'hang_hang_khong',
                'may_bay',
                'tuyen_bay.san_bay_di',
                'tuyen_bay.san_bay_den',
                'gia_ve' => function ($query) {
                    $query->where('ngay_bat_dau', '<=', now())
                        ->where('ngay_ket_thuc', '>=', now())
                        ->where('hang_ve', 'pho_thong')
                        ->orderBy('gia', 'asc')
                        ->limit(1);
                }
            ])
            ->orderBy('gio_khoi_hanh', 'asc')
            ->limit(12)
            ->get();

        return response()->json([
            'data' => $chuyenBay
        ]);
    }
}
