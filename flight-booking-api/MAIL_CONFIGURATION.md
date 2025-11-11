# Cấu hình Email

## Cấu hình Mail trong .env

Thêm các dòng sau vào file `.env` của bạn:

### Sử dụng Gmail (SMTP)

```env
MAIL_MAILER=smtp
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=vonhanh271@gmail.com
MAIL_PASSWORD=qiujfftrozfknmvd
MAIL_ENCRYPTION=tls
MAIL_FROM_ADDRESS=vonhanh271@gmail.com
MAIL_FROM_NAME="${APP_NAME}"
```

### Lưu ý khi sử dụng Gmail:

1. Bạn cần bật "App Passwords" trong tài khoản Google của bạn:
   - Vào https://myaccount.google.com/security
   - Bật 2-Step Verification
   - Tạo App Password tại https://myaccount.google.com/apppasswords
   - Sử dụng App Password thay vì mật khẩu thông thường

2. Hoặc sử dụng OAuth2 (phức tạp hơn nhưng an toàn hơn)

### Sử dụng Mailtrap (cho testing)

```env
MAIL_MAILER=smtp
MAIL_HOST=smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=your_mailtrap_username
MAIL_PASSWORD=your_mailtrap_password
MAIL_ENCRYPTION=tls
MAIL_FROM_ADDRESS=noreply@example.com
MAIL_FROM_NAME="${APP_NAME}"
```

### Sử dụng Mailgun (Production)

```env
MAIL_MAILER=mailgun
MAILGUN_DOMAIN=your_domain.mailgun.org
MAILGUN_SECRET=your_mailgun_secret
MAIL_FROM_ADDRESS=noreply@your_domain.com
MAIL_FROM_NAME="${APP_NAME}"
```

## Các loại email được gửi tự động:

1. **BookingConfirmationMail**: Gửi khi đặt vé thành công
2. **FlightDelayMail**: Gửi khi chuyến bay bị delay (thay đổi giờ khởi hành)
3. **FlightCancellationMail**: Gửi khi chuyến bay bị hủy

## Kiểm tra email có hoạt động:

Sau khi cấu hình, bạn có thể test bằng cách:

1. Đặt một vé mới - sẽ nhận email xác nhận
2. Cập nhật giờ khởi hành của chuyến bay - sẽ gửi email delay
3. Hủy chuyến bay - sẽ gửi email hủy chuyến

## Troubleshooting:

- Kiểm tra log: `storage/logs/laravel.log`
- Nếu email không gửi được, kiểm tra:
  - Cấu hình trong `.env` đúng chưa
  - Firewall có chặn port SMTP không
  - Credentials có đúng không
  - App Password (nếu dùng Gmail) có đúng không

