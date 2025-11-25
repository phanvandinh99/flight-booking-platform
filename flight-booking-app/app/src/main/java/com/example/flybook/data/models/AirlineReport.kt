package com.example.flybook.data.models

data class OverviewReport(
    val tong_so_chuyen_bay: Int,
    val tong_so_dat_ve: Int,
    val tong_doanh_thu: Double,
    val doanh_thu_trung_binh: Double,
    val ty_le_thanh_cong: Double? = null
)

data class DailyRevenue(
    val ngay: String, // YYYY-MM-DD
    val so_dat_ve: Int,
    val doanh_thu: Double
)

data class WeeklyRevenue(
    val tuan: Int? = null,
    val nam: Int? = null,
    val so_dat_ve: Int,
    val doanh_thu: Double
)

data class AirlineMonthlyRevenue(
    val thang: String? = null, // "YYYY-MM" or number
    val nam: Int? = null,
    val so_dat_ve: Int,
    val doanh_thu: Double
)

data class FlightReport(
    val id: Int? = null,
    val ma_chuyen_bay: String? = null,
    val tuyen_bay: Route? = null,
    val so_dat_ve: Int,
    val tong_doanh_thu: Double
)

data class FareClassReport(
    val hang_ve: String, // pho_thong, thuong_gia, hang_nhat
    val so_dat_ve: Int,
    val doanh_thu: Double,
    val gia_trung_binh: Double
)

