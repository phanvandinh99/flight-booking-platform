@component('mail::message')
# Thông báo hoãn chuyến bay

Xin chào {{ $datVe->khach_hang->ten ?? 'Quý khách' }},

Chúng tôi rất tiếc phải thông báo rằng chuyến bay của bạn đã bị hoãn.

## Thông tin chuyến bay

**Mã chuyến bay:** {{ $chuyenBay->ma_chuyen_bay }}

**Tuyến bay:** {{ $chuyenBay->tuyen_bay->san_bay_di->ten_san_bay ?? 'N/A' }} → {{ $chuyenBay->tuyen_bay->san_bay_den->ten_san_bay ?? 'N/A' }}

**Mã đặt vé:** {{ $datVe->ma_dat_ve }}

## Thay đổi lịch trình

**Giờ khởi hành ban đầu:** {{ \Carbon\Carbon::parse($chuyenBay->getRawOriginal('gio_khoi_hanh') ?? $chuyenBay->gio_khoi_hanh)->format('d/m/Y H:i') }}

**Giờ khởi hành mới:** {{ \Carbon\Carbon::parse($thoiGianKhoiHanhMoi)->format('d/m/Y H:i') }}

@php
$thoiGianCu = \Carbon\Carbon::parse($chuyenBay->getRawOriginal('gio_khoi_hanh') ?? $chuyenBay->gio_khoi_hanh);
$thoiGianMoi = \Carbon\Carbon::parse($thoiGianKhoiHanhMoi);
$khoangThoiGian = $thoiGianCu->diffForHumans($thoiGianMoi, true);
@endphp

**Thời gian hoãn:** {{ $khoangThoiGian }}

## Hành khách

@foreach($datVe->hanh_khach as $hanhKhach)
- **{{ $hanhKhach->ho_ten }}**
@endforeach

## Lưu ý quan trọng

- Vui lòng đến sân bay theo giờ khởi hành mới
- Kiểm tra lại thông tin chuyến bay trước khi đi
- Nếu bạn không thể đi theo lịch trình mới, vui lòng liên hệ với chúng tôi để được hỗ trợ

@component('mail::button', ['url' => config('app.url') . '/booking/' . $datVe->id])
Xem chi tiết đặt vé
@endcomponent

Chúng tôi xin lỗi vì sự bất tiện này và cảm ơn sự kiên nhẫn của bạn.

Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi qua email hoặc hotline.

Trân trọng,<br>
{{ config('app.name') }}
@endcomponent