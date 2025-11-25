# Hướng dẫn cấu hình VNPAY

## 1. Cấu hình trong file `.env`

Thêm các biến môi trường sau vào file `.env`:

```env
# VNPAY Configuration
VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_TMN_CODE=YOUR_TMN_CODE
VNPAY_HASH_SECRET=YOUR_HASH_SECRET
VNPAY_RETURN_URL=http://localhost:3000/payment/result
VNPAY_IPN_URL=http://localhost:8000/api/payment/vnpay/ipn

# Frontend URL (for redirects)
APP_FRONTEND_URL=http://localhost:3000
```

## 2. Lấy thông tin từ VNPAY

1. Đăng ký tài khoản tại [VNPAY](https://www.vnpayment.vn/)
2. Đăng nhập vào hệ thống và lấy:
   - **TMN Code**: Mã website của bạn
   - **Hash Secret**: Mã bảo mật để tạo chữ ký

## 3. Chạy migration

Chạy migration để thêm các cột cần thiết vào bảng `dat_ve`:

```bash
php artisan migrate
```

## 4. Cấu hình IPN URL trong VNPAY

1. Đăng nhập vào hệ thống VNPAY
2. Vào phần cấu hình website
3. Thêm IPN URL: `http://your-domain.com/api/payment/vnpay/ipn`
4. Lưu cấu hình

## 5. Test thanh toán

### Sandbox (Test)
- URL: `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`
- Sử dụng thẻ test từ VNPAY

### Production
- URL: `https://www.vnpayment.vn/paymentv2/vpcpay.html`
- Sử dụng thẻ thật

## 6. Lưu ý

- **Return URL**: URL mà VNPAY sẽ redirect về sau khi thanh toán
- **IPN URL**: URL mà VNPAY sẽ gọi để xác nhận thanh toán (Instant Payment Notification)
- **Hash Secret**: Phải giữ bí mật, không được commit lên Git
- **TMN Code**: Mã website của bạn trên VNPAY

## 7. Response Codes

- `00`: Giao dịch thành công
- `07`: Giao dịch bị nghi ngờ
- `09`: Thẻ/Tài khoản chưa đăng ký dịch vụ
- `10`: Xác thực thông tin không đúng quá 3 lần
- `11`: Đã hết hạn chờ thanh toán
- `12`: Thẻ/Tài khoản bị khóa
- `13`: Nhập sai mật khẩu OTP
- `24`: Người dùng hủy giao dịch
- `51`: Tài khoản không đủ số dư
- `65`: Vượt quá hạn mức giao dịch trong ngày
- `75`: Ngân hàng thanh toán đang bảo trì
- `79`: Nhập sai mật khẩu quá số lần quy định
- `99`: Lỗi không xác định

## 8. Xử lý lỗi

Nếu gặp lỗi, kiểm tra:
1. Cấu hình trong `.env` đã đúng chưa
2. Hash Secret có khớp với VNPAY không
3. IPN URL có thể truy cập được từ internet không (không dùng localhost)
4. Logs trong `storage/logs/laravel.log`

