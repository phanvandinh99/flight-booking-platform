## Flight Booking API

API phục vụ nền tảng đặt vé máy bay (backend Laravel).

### 1) Tính năng chính
- Quản lý sân bay, tuyến bay, hãng hàng không, máy bay, chuyến bay, giá vé
- Tìm kiếm chuyến bay theo hành trình/ngày/hạng ghế, lọc và sắp xếp
- Đặt vé cho nhiều hành khách, giữ chỗ tạm thời, thanh toán (mock)
- Xác thực người dùng, phân quyền (Khách hàng, Đại diện hãng, Quản trị)

### 1.1) Luồng nghiệp vụ khớp với mã nguồn
- Public:
  - POST `/api/register` — đăng ký (vai trò: `khach_hang` hoặc `dai_dien_hang`)
  - POST `/api/login` — đăng nhập, nhận token Sanctum
  - GET `/api/airlines` — danh sách hãng hàng không
  - GET `/api/airlines/{id}` — chi tiết hãng hàng không
- Sau khi đăng nhập, gửi `Authorization: Bearer {token}`:
  - Auth: POST `/api/logout`, POST `/api/logout-all`, GET `/api/me`, PUT `/api/profile`
  - Khách hàng (`role: khach_hang`, prefix `/api/customer`):
    - Tìm kiếm: `POST /search/flights`, `GET /search/airports`, `GET /search/airlines`, `GET /search/flights/{id}`
    - Đặt vé: `POST /bookings`, `GET /bookings`, `GET /bookings/{id}`, `POST /bookings/{id}/payment`, `PUT /bookings/{id}/cancel`
  - Đại diện hãng (`role: dai_dien_hang`, prefix `/api/airline`):
    - Máy bay: `apiResource('aircrafts', ...)`
    - Chuyến bay: `apiResource('flights', ...)`, `GET /flights/routes/approved`
    - Giá vé: `apiResource('pricing', ...)`, `GET /pricing/flights`
    - Đặt vé: `apiResource('bookings', ...)`, `PUT /bookings/{id}/status`, `GET /bookings/statistics`, `GET /bookings/flights`
    - Báo cáo: `GET /reports/daily-revenue|weekly-revenue|monthly-revenue|flight-report|fare-class-report|overview`
- Ghi chú:
  - Sử dụng Laravel Sanctum cho xác thực token.
  - Middleware `role:` kiểm soát truy cập theo `vai_tro` (`khach_hang`, `dai_dien_hang`, `admin`).
  - Thanh toán hiện tại là mô phỏng (mock) trong `customer/bookings/{id}/payment`.

### 2) Yêu cầu hệ thống
- PHP 8.2+
- Composer
- SQLite (mặc định) hoặc MySQL/PostgreSQL (tùy cấu hình `.env`)

### 3) Cài đặt nhanh (local)
```bash
# 1) Cài đặt dependencies
composer install

# 2) Tạo file môi trường
cp .env.example .env

# 3) (Khuyến nghị) Dùng SQLite sẵn có
#   - Đảm bảo file database/database.sqlite tồn tại
#   - Trong .env:
#       DB_CONNECTION=sqlite
#       DB_DATABASE=./database/database.sqlite

# 4) Tạo APP_KEY
php artisan key:generate

# 5) Chạy migration + seed dữ liệu mẫu
php artisan migrate --seed

# 6) Khởi động server dev
php artisan serve
```

- Mặc định API base URL: `http://127.0.0.1:8000`

### 3.1) Cấu hình MySQL (bạn đang dùng MySQL)
1) Tạo database trống (VD: `flight_booking`)
2) Chỉnh `.env`:
```
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=flight_booking
DB_USERNAME=<mysql_user>
DB_PASSWORD=<mysql_password>
```
3) Chạy lại lệnh:
```bash
php artisan migrate:fresh --seed
```

### 4) Tài liệu API
- Tài liệu cho Khách hàng: `CUSTOMER_API_DOCUMENTATION.md`
- Tài liệu cho Hãng hàng không: `HANG_HANG_KHONG_API_DOCUMENTATION.md`
- Bộ sưu tập Postman: `docs/JSON-POSTMAN/Flight Booking Platform.postman_collection.json`

### 5) Cấu trúc thư mục (rút gọn)
- `app/Models`: Các model như `ChuyenBay`, `DatVe`, `GiaVe`, `HanhKhach`, ...
- `app/Http/Controllers/Api`: Controller cho xác thực, tìm kiếm, đặt vé, ...
- `routes/api.php`: Khai báo route API
- `database/migrations`: Lược đồ CSDL
- `database/seeders/FlightBookingSeeder.php`: Dữ liệu mẫu

### 6) Lưu ý môi trường
- Có thể chuyển sang MySQL/PostgreSQL bằng cách chỉnh `.env` và chạy lại `migrate --seed`
- Log: `storage/logs/laravel.log`

### 7) Kiểm thử nhanh
```bash
php artisan test
```

### 8) Bản quyền
Sử dụng nội bộ phục vụ bài tập/POC. Tuân thủ giấy phép của các thư viện bên thứ ba theo `composer.json`.

