package com.example.flybook.data.models

data class RevenueSummary(
    val tong_doanh_thu: Double,
    val tong_dat_ve_da_thanh_toan: Int,
    val tong_dat_ve: Int,
    val doanh_thu_trung_binh: Double
)

data class MonthlyRevenue(
    val month: String,
    val revenue: Double,
    val orders: Int
)

data class TopAirline(
    val id: Int,
    val ten_hang: String,
    val ma_hang: String,
    val so_dat_ve: Int,
    val tong_doanh_thu: Double,
    val doanh_thu_trung_binh: Double
)

