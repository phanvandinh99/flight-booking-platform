# Flight Booking Android App

Ứng dụng Android đặt vé máy bay được phát triển bằng Kotlin và Jetpack Compose.

## Tính năng

- ✅ Đăng nhập / Đăng ký
- ✅ Tìm kiếm chuyến bay (một chiều / khứ hồi)
- ✅ Xem danh sách chuyến bay
- ✅ Xem chi tiết chuyến bay
- ✅ Xem đặt vé của tôi
- 🚧 Đặt vé (đang phát triển)
- 🚧 Thanh toán (đang phát triển)

## Cấu trúc Project

```
app/src/main/java/com/example/flybook/
├── data/
│   ├── api/          # Retrofit API service
│   ├── models/        # Data models
│   └── repository/    # Repository pattern
├── navigation/        # Navigation graph
├── ui/
│   ├── components/   # Reusable components
│   ├── screens/      # Screen composables
│   └── theme/        # Material theme
└── util/             # Utilities (AuthManager, etc.)
```

## Cấu hình

### 1. API Base URL

Mặc định app sử dụng `http://10.0.2.2:8000/api/` cho Android Emulator (localhost).

Để thay đổi, sửa trong file `ApiClient.kt`:

```kotlin
private const val BASE_URL = "http://YOUR_IP:8000/api/"
```

**Lưu ý:** 
- Emulator: sử dụng `10.0.2.2` thay cho `localhost`
- Thiết bị thật: sử dụng IP máy tính của bạn (ví dụ: `192.168.1.100`)

### 2. Dependencies

App sử dụng:
- **Jetpack Compose** - UI framework
- **Navigation Compose** - Navigation
- **Retrofit** - HTTP client
- **Gson** - JSON parsing
- **DataStore** - Local storage
- **Coil** - Image loading

## Chạy ứng dụng

1. Mở project trong Android Studio
2. Sync Gradle files
3. Đảm bảo backend API đang chạy tại `http://localhost:8000`
4. Chạy app trên emulator hoặc thiết bị thật

## Kết nối với Backend

App kết nối với backend Laravel tại `flight-booking-api`. Đảm bảo:

1. Backend đang chạy và có thể truy cập được
2. CORS đã được cấu hình đúng trong backend
3. API base URL trong `ApiClient.kt` trỏ đúng đến backend

## Tính năng đang phát triển

- [ ] Hoàn thiện màn hình đặt vé
- [ ] Tích hợp thanh toán VNPay
- [ ] Quản lý thông tin hành khách
- [ ] Chọn ghế ngồi
- [ ] Push notifications
- [ ] Offline mode

## Lưu ý

- App hiện tại chỉ hỗ trợ chức năng cho khách hàng (customer)
- Chức năng admin và airline representative sẽ được phát triển sau
- Token authentication được lưu trong DataStore

