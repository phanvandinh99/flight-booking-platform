package com.example.flybook.data.models

data class AirlineFarePrice(
    val id: Int,
    val ma_chuyen_bay: Int,
    val hang_ve: String, // pho_thong, thuong_gia, hang_nhat
    val gia: Double,
    val hanh_ly_ky_gui: String,
    val chinh_sach_huy_ve: String? = null,
    val chinh_sach_doi_ve: String? = null,
    val ngay_bat_dau: String, // YYYY-MM-DD
    val ngay_ket_thuc: String, // YYYY-MM-DD
    val chuyen_bay: AirlineFlight? = null
)

