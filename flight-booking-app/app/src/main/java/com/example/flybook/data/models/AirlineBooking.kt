package com.example.flybook.data.models

data class AirlineBooking(
    val id: Int,
    val ma_dat_ve: String,
    val ma_khach_hang: Int?,
    val ma_chuyen_bay: Int,
    val tong_tien: Double,
    val trang_thai: String, // giu_cho, da_thanh_toan, da_huy
    val thoi_gian_het_han_giu_cho: String?,
    val ma_giao_dich: String?,
    val thoi_gian_thanh_toan: String?,
    val created_at: String,
    val updated_at: String,
    val khach_hang: User? = null,
    val chuyen_bay: AirlineFlight? = null,
    val hanh_khach: List<Passenger>? = null
)

data class BookingStatistics(
    val tong_so_dat_ve: Int,
    val da_thanh_toan: Int,
    val giu_cho: Int,
    val da_huy: Int,
    val tong_doanh_thu: Double,
    val doanh_thu_trung_binh: Double
)

