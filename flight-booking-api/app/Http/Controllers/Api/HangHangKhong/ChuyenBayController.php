<?php

namespace App\Http\Controllers\Api\HangHangKhong;

use App\Models\MayBay;
use App\Models\TuyenBay;
use App\Models\ChuyenBay;
use App\Http\Controllers\Controller;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

use Carbon\Carbon;

class ChuyenBayController extends Controller
{
    /**
     * Lấy danh sách chuyến bay của hãng
     */
    public function index(Request $request)
    {
        $user = $request->user();
        $query = ChuyenBay::where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->with([
                'hang_hang_khong',
                'may_bay',
                'tuyen_bay.san_bay_di',
                'tuyen_bay.san_bay_den'
            ]);

        // Filter theo ngày
        if ($request->has('ngay_khoi_hanh')) {
            $ngay = Carbon::parse($request->ngay_khoi_hanh);
            $query->whereDate('gio_khoi_hanh', $ngay);
        }

        // Filter theo trạng thái
        if ($request->has('trang_thai')) {
            $query->where('trang_thai', $request->trang_thai);
        }

        // Filter theo tuyến bay
        if ($request->has('ma_tuyen_bay')) {
            $query->where('ma_tuyen_bay', $request->ma_tuyen_bay);
        }

        $flights = $query->orderBy('gio_khoi_hanh', 'desc')->paginate(20);

        return response()->json([
            'data' => $flights->items(),
            'pagination' => [
                'current_page' => $flights->currentPage(),
                'last_page' => $flights->lastPage(),
                'per_page' => $flights->perPage(),
                'total' => $flights->total()
            ]
        ]);
    }

    /**
     * Lấy chi tiết chuyến bay
     */
    public function show(Request $request, $id)
    {
        $user = $request->user();
        $flight = ChuyenBay::where('id', $id)
            ->where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->with([
                'hang_hang_khong',
                'may_bay',
                'tuyen_bay.san_bay_di',
                'tuyen_bay.san_bay_den',
                'gia_ve'
            ])
            ->first();

        if (!$flight) {
            return response()->json([
                'message' => 'Không tìm thấy chuyến bay'
            ], 404);
        }

        return response()->json([
            'data' => $flight
        ]);
    }

    /**
     * Tạo chuyến bay mới
     */
    public function store(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'ma_may_bay' => 'required|exists:may_bay,id',
            'ma_chuyen_bay' => 'required|string|max:20',
            'ma_tuyen_bay' => 'required|exists:tuyen_bay,id',
            'gio_khoi_hanh' => 'required|date|after:now',
            'gio_ha_canh' => 'required|date|after:gio_khoi_hanh',
            'tan_suat' => 'required|string|in:hang_ngay,thu_2_thu_4,thu_3_thu_5,thu_4_thu_6,thu_5_thu_7,thu_6_cn,thu_7_cn,cn_thu_2',
            'trang_thai' => 'sometimes|in:du_kien,bi_huy,da_hoan_thanh'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Dữ liệu không hợp lệ',
                'errors' => $validator->errors()
            ], 422);
        }

        // Kiểm tra máy bay thuộc về hãng
        $aircraft = MayBay::where('id', $request->ma_may_bay)
            ->where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->first();

        if (!$aircraft) {
            return response()->json([
                'message' => 'Máy bay không thuộc về hãng của bạn'
            ], 400);
        }

        // Kiểm tra tuyến bay đã được phê duyệt
        $route = TuyenBay::where('id', $request->ma_tuyen_bay)
            ->where('duoc_phe_duyet', true)
            ->first();

        if (!$route) {
            return response()->json([
                'message' => 'Tuyến bay chưa được phê duyệt'
            ], 400);
        }

        $flight = ChuyenBay::create([
            'ma_hang_hang_khong' => $user->ma_hang_hang_khong,
            'ma_may_bay' => $request->ma_may_bay,
            'ma_chuyen_bay' => $request->ma_chuyen_bay,
            'ma_tuyen_bay' => $request->ma_tuyen_bay,
            'gio_khoi_hanh' => $request->gio_khoi_hanh,
            'gio_ha_canh' => $request->gio_ha_canh,
            'tan_suat' => $request->tan_suat,
            'trang_thai' => $request->trang_thai ?? 'du_kien'
        ]);

        return response()->json([
            'message' => 'Tạo chuyến bay thành công',
            'data' => $flight->load([
                'hang_hang_khong',
                'may_bay',
                'tuyen_bay.san_bay_di',
                'tuyen_bay.san_bay_den'
            ])
        ], 201);
    }

    /**
     * Cập nhật chuyến bay
     */
    public function update(Request $request, $id)
    {
        $user = $request->user();
        $flight = ChuyenBay::where('id', $id)
            ->where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->first();

        if (!$flight) {
            return response()->json([
                'message' => 'Không tìm thấy chuyến bay'
            ], 404);
        }

        $validator = Validator::make($request->all(), [
            'ma_may_bay' => 'sometimes|required|exists:may_bay,id',
            'ma_chuyen_bay' => 'sometimes|required|string|max:20',
            'ma_tuyen_bay' => 'sometimes|required|exists:tuyen_bay,id',
            'gio_khoi_hanh' => 'sometimes|required|date',
            'gio_ha_canh' => 'sometimes|required|date|after:gio_khoi_hanh',
            'tan_suat' => 'sometimes|required|string|in:hang_ngay,thu_2_thu_4,thu_3_thu_5,thu_4_thu_6,thu_5_thu_7,thu_6_cn,thu_7_cn,cn_thu_2',
            'trang_thai' => 'sometimes|in:du_kien,bi_huy,da_hoan_thanh'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Dữ liệu không hợp lệ',
                'errors' => $validator->errors()
            ], 422);
        }

        // Kiểm tra máy bay thuộc về hãng (nếu có cập nhật)
        if ($request->has('ma_may_bay')) {
            $aircraft = MayBay::where('id', $request->ma_may_bay)
                ->where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
                ->first();

            if (!$aircraft) {
                return response()->json([
                    'message' => 'Máy bay không thuộc về hãng của bạn'
                ], 400);
            }
        }

        $flight->update($request->only([
            'ma_may_bay',
            'ma_chuyen_bay',
            'ma_tuyen_bay',
            'gio_khoi_hanh',
            'gio_ha_canh',
            'tan_suat',
            'trang_thai'
        ]));

        return response()->json([
            'message' => 'Cập nhật chuyến bay thành công',
            'data' => $flight->load([
                'hang_hang_khong',
                'may_bay',
                'tuyen_bay.san_bay_di',
                'tuyen_bay.san_bay_den'
            ])
        ]);
    }

    /**
     * Xóa chuyến bay
     */
    public function destroy(Request $request, $id)
    {
        $user = $request->user();
        $flight = ChuyenBay::where('id', $id)
            ->where('ma_hang_hang_khong', $user->ma_hang_hang_khong)
            ->first();

        if (!$flight) {
            return response()->json([
                'message' => 'Không tìm thấy chuyến bay'
            ], 404);
        }

        // Kiểm tra xem chuyến bay có vé đã bán không
        if ($flight->dat_ve()->where('trang_thai', '!=', 'da_huy')->exists()) {
            return response()->json([
                'message' => 'Không thể xóa chuyến bay đã có vé được bán'
            ], 400);
        }

        $flight->delete();

        return response()->json([
            'message' => 'Xóa chuyến bay thành công'
        ]);
    }

    /**
     * Lấy danh sách tuyến bay đã được phê duyệt
     */
    public function getApprovedRoutes()
    {
        $routes = TuyenBay::where('duoc_phe_duyet', true)
            ->with(['san_bay_di', 'san_bay_den'])
            ->get();

        return response()->json([
            'data' => $routes
        ]);
    }
}
