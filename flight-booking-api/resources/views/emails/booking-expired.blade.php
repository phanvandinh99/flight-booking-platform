@component('mail::message')
# Thông báo hủy đặt vé

Xin chào {{ $tenDayDu ?? 'Khách hàng' }},

Chúng tôi rất tiếc phải thông báo rằng đặt vé của bạn đã bị hủy do hết hạn thanh toán.

## Thông tin đặt vé

**Mã đặt vé:** {{ $datVe->ma_dat_ve }}

**Chuyến bay:** {{ $datVe->chuyen_bay->ma_chuyen_bay ?? 'N/A' }}

**Hãng hàng không:** {{ $datVe->chuyen_bay->hang_hang_khong->ten_hang ?? 'N/A' }}

**Tổng tiền:** {{ number_format($datVe->tong_tien, 0, ',', '.') }} VNĐ

**Thời gian hết hạn thanh toán:** {{ $datVe->thoi_gian_het_han_giu_cho ? $datVe->thoi_gian_het_han_giu_cho->format('d/m/Y H:i') : 'N/A' }}

## Lý do hủy

Đặt vé của bạn đã hết hạn thanh toán và đã được tự động hủy theo quy định của hệ thống.

## Lưu ý

- Nếu bạn vẫn muốn đặt vé, vui lòng thực hiện đặt vé mới trên website của chúng tôi.
- Nếu bạn đã thanh toán nhưng nhận được thông báo này, vui lòng liên hệ với chúng tôi ngay lập tức.

@component('mail::button', ['url' => config('app.frontend_url', 'http://localhost:3000') . '/flights'])
Đặt vé mới
@endcomponent

Trân trọng,<br>
{{ config('app.name') }}
@endcomponent