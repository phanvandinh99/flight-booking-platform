package com.example.flybook.data.models

data class Booking(
    val id: Int,
    val ma_dat_ve: String,
    val chuyen_bay: Flight?,
    val chuyen_bay_ve: Flight?,
    val tong_tien: Double,
    val trang_thai: String,
    val phuong_thuc_thanh_toan: String?,
    val ngay_dat: String,
    val hanh_khach: List<Passenger>?,
    val thoi_gian_het_han: String?
)

data class Passenger(
    val id: Int? = null,
    val ho_ten: String,
    val ngay_sinh: String? = null,
    val gioi_tinh: String? = null,
    val cmnd_cccd: String? = null,
    val so_dien_thoai: String? = null,
    val email: String? = null,
    val loai_hanh_khach: String = "nguoi_lon",
    // Fields for booking request
    val so_ho_chieu: String? = null,
    val loai_giay_to: String? = "ho_chieu", // ho_chieu, can_cuoc
    val so_giay_to: String? = null,
    val so_ghe: String? = null
)

data class ContactInfo(
    val email: String,
    val so_dien_thoai: String,
    val ten_day_du: String
)

data class CreateBookingRequest(
    val ma_chuyen_bay_di: Int,
    val ma_chuyen_bay_ve: Int? = null,
    val hang_ve: String,
    val hanh_khach: List<BookingPassenger>,
    val thong_tin_lien_he: ContactInfo
)

data class BookingPassenger(
    val ho_ten: String,
    val so_ho_chieu: String? = null,
    val loai_giay_to: String? = "ho_chieu",
    val so_giay_to: String? = null,
    val so_ghe: String? = null,
    val loai_hanh_khach: String = "nguoi_lon"
)

data class PaymentRequest(
    val bank_code: String? = null
)

data class PaymentResponse(
    val payment_url: String
)

