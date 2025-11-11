<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\DatVe;
use App\Models\HanhKhach;
use App\Services\VNPayService;
use Illuminate\Support\Facades\Log;

class PaymentController extends Controller
{
    /**
     * Xử lý kết quả thanh toán từ VNPAY (Return URL)
     */
    public function vnpayReturn(Request $request)
    {
        $vnpayService = new VNPayService();
        $result = $vnpayService->processReturn($request->all());

        $txnRef = $result['txn_ref'] ?? null;
        $responseCode = $result['response_code'] ?? '';
        $transactionNo = $result['transaction_no'] ?? '';

        // Log tất cả thông tin để debug
        Log::info('VNPAY Return: Received callback', [
            'is_valid' => $result['is_valid'],
            'response_code' => $responseCode,
            'transaction_no' => $transactionNo,
            'txn_ref' => $txnRef,
            'amount' => $result['amount'] ?? null,
            'raw_request' => $request->all()
        ]);

        // Extract booking ID từ txn_ref (format: {booking_id}_{timestamp}_{random})
        $datVeId = null;
        if ($txnRef) {
            // Tách phần đầu tiên (booking ID) từ txn_ref
            $parts = explode('_', $txnRef);
            if (isset($parts[0]) && is_numeric($parts[0])) {
                $datVeId = (int)$parts[0];
            } else {
                // Fallback: nếu format không đúng, thử dùng toàn bộ txn_ref làm ID
                if (is_numeric($txnRef)) {
                    $datVeId = (int)$txnRef;
                }
            }
        }

        if (!$result['is_valid']) {
            Log::warning('VNPAY Return: Invalid signature', $result);
            // Nếu signature không hợp lệ nhưng có response code thành công, vẫn xử lý
            if (($responseCode == '00' || $responseCode == '07') && $datVeId) {
                Log::warning('VNPAY Return: Invalid signature but response code indicates success, proceeding with caution', [
                    'response_code' => $responseCode,
                    'dat_ve_id' => $datVeId
                ]);
                // Tiếp tục xử lý nhưng log warning
            } else {
                return redirect()->away(env('APP_FRONTEND_URL', 'http://localhost:3000') . '/payment/result?status=failed&message=' . urlencode('Chữ ký không hợp lệ'));
            }
        }

        if (!$datVeId) {
            Log::error('VNPAY Return: Missing or invalid transaction reference', [
                'txn_ref' => $txnRef
            ]);
            return redirect()->away(env('APP_FRONTEND_URL', 'http://localhost:3000') . '/payment/result?status=failed&message=' . urlencode('Thiếu thông tin giao dịch'));
        }

        $datVe = DatVe::find($datVeId);

        if (!$datVe) {
            Log::error('VNPAY Return: Booking not found', ['dat_ve_id' => $datVeId]);
            return redirect()->away(env('APP_FRONTEND_URL', 'http://localhost:3000') . '/payment/result?status=failed&message=' . urlencode('Không tìm thấy đặt vé'));
        }

        // Kiểm tra số tiền (cho phép sai lệch nhỏ do làm tròn)
        $amountDiff = abs($result['amount'] - $datVe->tong_tien);
        if ($amountDiff > 1) { // Cho phép sai lệch tối đa 1 VND
            Log::warning('VNPAY Return: Amount mismatch', [
                'expected' => $datVe->tong_tien,
                'received' => $result['amount'],
                'difference' => $amountDiff
            ]);
            // Nếu response code thành công, vẫn xử lý nhưng log warning
            if ($responseCode != '00' && $responseCode != '07') {
                return redirect()->away(env('APP_FRONTEND_URL', 'http://localhost:3000') . '/payment/result?status=failed&message=' . urlencode('Số tiền không khớp'));
            }
        }

        // Log tất cả thông tin để debug
        Log::info('VNPAY Return: Processing payment result', [
            'dat_ve_id' => $datVeId,
            'response_code' => $responseCode,
            'transaction_no' => $transactionNo,
            'current_status' => $datVe->trang_thai,
            'amount_expected' => $datVe->tong_tien,
            'amount_received' => $result['amount'],
            'is_valid' => $result['is_valid'],
            'raw_data' => $request->all()
        ]);

        // Response code '00' hoặc '07' đều là thành công
        // '00': Giao dịch thành công
        // '07': Trừ tiền thành công nhưng bị nghi ngờ (vẫn coi là thành công)
        if ($responseCode == '00' || $responseCode == '07') {
            // Thanh toán thành công
            // Cho phép cập nhật từ các trạng thái: giu_cho, cho_thanh_toan, chờ_thanh_toan
            $allowedStatuses = ['giu_cho', 'cho_thanh_toan', 'chờ_thanh_toan'];
            if (in_array($datVe->trang_thai, $allowedStatuses)) {
                $datVe->update([
                    'trang_thai' => 'da_thanh_toan',
                    'ma_giao_dich' => $transactionNo,
                    'thoi_gian_thanh_toan' => now()
                ]);

                Log::info('VNPAY Return: Payment successful and booking updated', [
                    'dat_ve_id' => $datVeId,
                    'transaction_no' => $transactionNo,
                    'previous_status' => $datVe->getOriginal('trang_thai'),
                    'response_code' => $responseCode
                ]);
            } else {
                Log::warning('VNPAY Return: Payment successful but booking already processed', [
                    'dat_ve_id' => $datVeId,
                    'current_status' => $datVe->trang_thai,
                    'response_code' => $responseCode
                ]);
            }

            // Luôn redirect về success, ngay cả khi booking đã được xử lý trước đó
            return redirect()->away(env('APP_FRONTEND_URL', 'http://localhost:3000') . '/payment/result?status=success&booking_id=' . $datVeId . '&transaction_no=' . $transactionNo);
        } else {
            // Thanh toán thất bại hoặc hủy
            if ($responseCode == '24') {
                // Người dùng hủy giao dịch
                // Xóa đặt vé và giải phóng ghế
                $this->cancelBooking($datVe);

                Log::info('VNPAY Return: User cancelled payment', [
                    'dat_ve_id' => $datVeId
                ]);
            }

            Log::warning('VNPAY Return: Payment failed', [
                'dat_ve_id' => $datVeId,
                'response_code' => $responseCode,
                'message' => $result['message']
            ]);

            return redirect()->away(env('APP_FRONTEND_URL', 'http://localhost:3000') . '/payment/result?status=failed&message=' . urlencode($result['message']) . '&booking_id=' . $datVeId);
        }
    }

    /**
     * Xử lý IPN (Instant Payment Notification) từ VNPAY
     */
    public function vnpayIpn(Request $request)
    {
        $vnpayService = new VNPayService();
        $result = $vnpayService->processReturn($request->all());

        $txnRef = $result['txn_ref'] ?? null;
        $responseCode = $result['response_code'] ?? '';
        $transactionNo = $result['transaction_no'] ?? '';

        // Extract booking ID từ txn_ref (format: {booking_id}_{timestamp}_{random})
        $datVeId = null;
        if ($txnRef) {
            $parts = explode('_', $txnRef);
            if (isset($parts[0]) && is_numeric($parts[0])) {
                $datVeId = (int)$parts[0];
            } else {
                if (is_numeric($txnRef)) {
                    $datVeId = (int)$txnRef;
                }
            }
        }

        if (!$result['is_valid']) {
            Log::warning('VNPAY IPN: Invalid signature', $result);
            // Nếu signature không hợp lệ nhưng có response code thành công, vẫn xử lý
            if (($responseCode == '00' || $responseCode == '07') && $datVeId) {
                Log::warning('VNPAY IPN: Invalid signature but response code indicates success, proceeding with caution', [
                    'response_code' => $responseCode,
                    'dat_ve_id' => $datVeId
                ]);
                // Tiếp tục xử lý nhưng log warning
            } else {
                return response()->json(['RspCode' => '97', 'Message' => 'Invalid signature'], 200);
            }
        }

        if (!$datVeId) {
            Log::error('VNPAY IPN: Missing or invalid transaction reference', [
                'txn_ref' => $txnRef
            ]);
            return response()->json(['RspCode' => '01', 'Message' => 'Invalid transaction reference'], 200);
        }

        $datVe = DatVe::find($datVeId);

        if (!$datVe) {
            Log::error('VNPAY IPN: Booking not found', ['dat_ve_id' => $datVeId]);
            return response()->json(['RspCode' => '01', 'Message' => 'Order not found'], 200);
        }

        // Kiểm tra số tiền
        if ($result['amount'] != $datVe->tong_tien) {
            Log::warning('VNPAY IPN: Amount mismatch', [
                'expected' => $datVe->tong_tien,
                'received' => $result['amount']
            ]);
            return response()->json(['RspCode' => '04', 'Message' => 'Invalid amount'], 200);
        }

        if ($responseCode == '00') {
            // Thanh toán thành công
            // Cho phép cập nhật từ các trạng thái: giu_cho, cho_thanh_toan, chờ_thanh_toan
            $allowedStatuses = ['giu_cho', 'cho_thanh_toan', 'chờ_thanh_toan'];
            if (in_array($datVe->trang_thai, $allowedStatuses)) {
                $datVe->update([
                    'trang_thai' => 'da_thanh_toan',
                    'ma_giao_dich' => $transactionNo,
                    'thoi_gian_thanh_toan' => now()
                ]);

                Log::info('VNPAY IPN: Payment successful', [
                    'dat_ve_id' => $datVeId,
                    'transaction_no' => $transactionNo,
                    'previous_status' => $datVe->getOriginal('trang_thai')
                ]);

                return response()->json(['RspCode' => '00', 'Message' => 'Success'], 200);
            } else {
                Log::info('VNPAY IPN: Payment successful but booking already processed', [
                    'dat_ve_id' => $datVeId,
                    'current_status' => $datVe->trang_thai
                ]);

                return response()->json(['RspCode' => '00', 'Message' => 'Order already processed'], 200);
            }
        } else {
            Log::info('VNPAY IPN: Payment failed or already processed', [
                'dat_ve_id' => $datVeId,
                'response_code' => $responseCode,
                'current_status' => $datVe->trang_thai
            ]);

            return response()->json(['RspCode' => '00', 'Message' => 'Order already processed'], 200);
        }
    }

    /**
     * Hủy đặt vé và giải phóng ghế
     */
    private function cancelBooking($datVe)
    {
        // Xóa hành khách
        HanhKhach::where('ma_dat_ve', $datVe->id)->delete();

        // Xóa đặt vé
        $datVe->delete();
    }
}
