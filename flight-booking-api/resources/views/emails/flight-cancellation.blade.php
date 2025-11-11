@component('mail::message')
# Thông báo hủy chuyến bay

Xin chào {{ $datVe->khach_hang->ten ?? 'Quý khách' }},

Chúng tôi rất tiếc phải thông báo rằng chuyến bay của bạn đã bị hủy.

## Thông tin chuyến bay bị hủy

**Mã chuyến bay:** {{ $chuyenBay->ma_chuyen_bay }}

**Tuyến bay:** {{ $chuyenBay->tuyen_bay->san_bay_di->ten_san_bay ?? 'N/A' }} → {{ $chuyenBay->tuyen_bay->san_bay_den->ten_san_bay ?? 'N/A' }}

**Mã đặt vé:** {{ $datVe->ma_dat_ve }}

**Giờ khởi hành dự kiến:** {{ \Carbon\Carbon::parse($chuyenBay->gio_khoi_hanh)->format('d/m/Y H:i') }}

## Lý do hủy chuyến

{{ $lyDo }}

## Hành khách

@foreach($datVe->hanh_khach as $hanhKhach)
- **{{ $hanhKhach->ho_ten }}**
@endforeach

## Tổng tiền đã thanh toán

**{{ number_format($datVe->tong_tien, 0, ',', '.') }} VNĐ**

## Quy trình hoàn tiền

Chúng tôi sẽ tự động hoàn tiền cho bạn trong vòng **7-10 ngày làm việc** kể từ ngày hủy chuyến bay.

Số tiền sẽ được hoàn về phương thức thanh toán ban đầu của bạn.

@component('mail::button', ['url' => config('app.url') . '/booking/' . $datVe->id])
Xem chi tiết đặt vé
@endcomponent

## Lựa chọn thay thế

Nếu bạn muốn đặt lại chuyến bay khác, vui lòng liên hệ với chúng tôi. Chúng tôi sẽ hỗ trợ bạn tìm chuyến bay phù hợp nhất.

Chúng tôi xin lỗi vì sự bất tiện này và cảm ơn sự hiểu biết của bạn.

Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi qua email hoặc hotline.

Trân trọng,<br>
{{ config('app.name') }}
@endcomponent