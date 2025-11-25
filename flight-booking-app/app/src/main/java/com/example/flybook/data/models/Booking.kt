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
    val id: Int?,
    val ho_ten: String,
    val ngay_sinh: String?,
    val gioi_tinh: String?,
    val cmnd_cccd: String?,
    val so_dien_thoai: String?,
    val email: String?,
    val loai_hanh_khach: String = "nguoi_lon"
)

data class CreateBookingRequest(
    val chuyen_bay_id: Int,
    val chuyen_bay_ve_id: Int? = null,
    val hang_ve: String,
    val hang_ve_ve: String? = null,
    val hanh_khach: List<Passenger>
)

data class PaymentRequest(
    val bank_code: String? = null
)

data class PaymentResponse(
    val payment_url: String
)

