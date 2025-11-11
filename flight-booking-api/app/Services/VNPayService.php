<?php

namespace App\Services;

use Illuminate\Support\Facades\Log;

class VNPayService
{
    private $vnp_Url;
    private $vnp_TmnCode;
    private $vnp_HashSecret;
    private $vnp_ReturnUrl;

    public function __construct()
    {
        $this->vnp_Url = config('vnpay.url', 'https://sandbox.vnpayment.vn/paymentv2/vpcpay.html');
        $this->vnp_TmnCode = config('vnpay.tmn_code');
        $this->vnp_HashSecret = config('vnpay.hash_secret');
        // Return URL phải trỏ về backend API để xử lý, sau đó backend redirect về frontend
        $this->vnp_ReturnUrl = config('vnpay.return_url', env('APP_URL', 'http://localhost:8000') . '/api/payment/vnpay/return');
    }

    /**
     * Tạo URL thanh toán VNPAY
     */
    public function createPaymentUrl($orderId, $amount, $orderDescription, $orderType = 'other', $bankCode = null, $language = 'vn')
    {
        $vnp_TxnRef = $orderId; // Mã đơn hàng
        $vnp_OrderInfo = $orderDescription;
        $vnp_OrderType = $orderType;
        $vnp_Amount = $amount * 100; // VNPAY yêu cầu số tiền nhân 100
        $vnp_Locale = $language;
        $vnp_IpAddr = request()->ip();

        $inputData = array(
            "vnp_Version" => "2.1.0",
            "vnp_TmnCode" => $this->vnp_TmnCode,
            "vnp_Amount" => $vnp_Amount,
            "vnp_Command" => "pay",
            "vnp_CreateDate" => date('YmdHis'),
            "vnp_CurrCode" => "VND",
            "vnp_IpAddr" => $vnp_IpAddr,
            "vnp_Locale" => $vnp_Locale,
            "vnp_OrderInfo" => $vnp_OrderInfo,
            "vnp_OrderType" => $vnp_OrderType,
            "vnp_ReturnUrl" => $this->vnp_ReturnUrl,
            "vnp_TxnRef" => $vnp_TxnRef,
        );

        if ($bankCode !== null && $bankCode !== '') {
            $inputData['vnp_BankCode'] = $bankCode;
        }

        ksort($inputData);
        $query = "";
        $i = 0;
        $hashdata = "";
        foreach ($inputData as $key => $value) {
            if ($i == 1) {
                $hashdata .= '&' . urlencode($key) . "=" . urlencode($value);
            } else {
                $hashdata .= urlencode($key) . "=" . urlencode($value);
                $i = 1;
            }
            $query .= urlencode($key) . "=" . urlencode($value) . '&';
        }

        $vnp_Url = $this->vnp_Url . "?" . $query;
        if (isset($this->vnp_HashSecret)) {
            $vnpSecureHash = hash_hmac('sha512', $hashdata, $this->vnp_HashSecret);
            $vnp_Url .= 'vnp_SecureHash=' . $vnpSecureHash;
        }

        return $vnp_Url;
    }

    /**
     * Xác thực chữ ký từ VNPAY
     */
    public function verifySignature($inputData, $vnp_SecureHash)
    {
        $hashdata = "";
        ksort($inputData);
        $i = 0;
        foreach ($inputData as $key => $value) {
            if ($i == 1) {
                $hashdata .= '&' . urlencode($key) . "=" . urlencode($value);
            } else {
                $hashdata .= urlencode($key) . "=" . urlencode($value);
                $i = 1;
            }
        }

        $secureHash = hash_hmac('sha512', $hashdata, $this->vnp_HashSecret);
        return $secureHash === $vnp_SecureHash;
    }

    /**
     * Xử lý kết quả thanh toán từ VNPAY
     */
    public function processReturn($inputData)
    {
        $vnp_SecureHash = $inputData['vnp_SecureHash'] ?? '';
        unset($inputData['vnp_SecureHash']);

        $isValid = $this->verifySignature($inputData, $vnp_SecureHash);

        $responseCode = $inputData['vnp_ResponseCode'] ?? '';
        $transactionNo = $inputData['vnp_TransactionNo'] ?? '';
        $txnRef = $inputData['vnp_TxnRef'] ?? '';
        $amount = ($inputData['vnp_Amount'] ?? 0) / 100; // Chia 100 để lấy số tiền thực

        return [
            'is_valid' => $isValid,
            'response_code' => $responseCode,
            'transaction_no' => $transactionNo,
            'txn_ref' => $txnRef,
            'amount' => $amount,
            'message' => $this->getResponseMessage($responseCode),
            'raw_data' => $inputData
        ];
    }

    /**
     * Lấy thông báo từ response code
     */
    private function getResponseMessage($responseCode)
    {
        $messages = [
            '00' => 'Giao dịch thành công',
            '07' => 'Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).',
            '09' => 'Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking',
            '10' => 'Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần',
            '11' => 'Đã hết hạn chờ thanh toán. Xin vui lòng thực hiện lại giao dịch',
            '12' => 'Thẻ/Tài khoản bị khóa',
            '13' => 'Nhập sai mật khẩu xác thực giao dịch (OTP). Xin vui lòng thực hiện lại giao dịch',
            '51' => 'Tài khoản không đủ số dư để thực hiện giao dịch',
            '65' => 'Tài khoản đã vượt quá hạn mức giao dịch trong ngày',
            '75' => 'Ngân hàng thanh toán đang bảo trì',
            '79' => 'Nhập sai mật khẩu thanh toán quá số lần quy định. Xin vui lòng thực hiện lại giao dịch',
            '99' => 'Lỗi không xác định',
        ];

        return $messages[$responseCode] ?? 'Lỗi không xác định';
    }
}
