@component('mail::message')
# Xác nhận đặt vé thành công

Xin chào {{ $thongTinLienHe['ten_day_du'] }},

Cảm ơn bạn đã đặt vé với chúng tôi! Đặt vé của bạn đã được xác nhận thành công.

## Thông tin đặt vé

**Mã đặt vé:** {{ $datVe->ma_dat_ve }}

**Trạng thái:** {{ $datVe->trang_thai === 'giu_cho' ? 'Đang giữ chỗ' : ($datVe->trang_thai === 'da_thanh_toan' ? 'Đã thanh toán' : 'Chờ thanh toán') }}

@if($datVe->trang_thai === 'giu_cho')
**Thời gian hết hạn giữ chỗ:** {{ $datVe->thoi_gian_het_han_giu_cho->format('d/m/Y H:i') }}
@endif

## Thông tin chuyến bay

**Mã chuyến bay:** {{ $datVe->chuyen_bay->ma_chuyen_bay }}

**Hãng hàng không:** {{ $datVe->chuyen_bay->hang_hang_khong->ten_hang ?? 'N/A' }}

**Tuyến bay:** {{ $datVe->chuyen_bay->tuyen_bay->san_bay_di->ten_san_bay ?? 'N/A' }} → {{ $datVe->chuyen_bay->tuyen_bay->san_bay_den->ten_san_bay ?? 'N/A' }}

**Giờ khởi hành:** {{ \Carbon\Carbon::parse($datVe->chuyen_bay->gio_khoi_hanh)->format('d/m/Y H:i') }}

**Giờ hạ cánh:** {{ \Carbon\Carbon::parse($datVe->chuyen_bay->gio_ha_canh)->format('d/m/Y H:i') }}

## Hành khách

@foreach($datVe->hanh_khach as $hanhKhach)
- **{{ $hanhKhach->ho_ten }}** ({{ $hanhKhach->loai_hanh_khach === 'nguoi_lon' ? 'Người lớn' : ($hanhKhach->loai_hanh_khach === 'tre_em' ? 'Trẻ em' : 'Em bé') }})
@if($hanhKhach->so_ghe)
- Ghế: {{ $hanhKhach->so_ghe }}
@endif
@endforeach

## Tổng tiền

**{{ number_format($datVe->tong_tien, 0, ',', '.') }} VNĐ**

@if($datVe->trang_thai === 'giu_cho')
@component('mail::button', ['url' => config('app.url') . '/booking/' . $datVe->id])
Thanh toán ngay
@endcomponent

**Lưu ý:** Vui lòng thanh toán trong vòng 15 phút để hoàn tất đặt vé. Sau thời gian này, chỗ ngồi sẽ được giải phóng.
@endif

Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi qua email hoặc hotline.

Trân trọng,<br>
{{ config('app.name') }}
@endcomponent