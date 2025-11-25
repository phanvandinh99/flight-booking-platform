package com.example.flybook.data.models

data class AirlineFlight(
    val id: Int,
    val ma_hang_hang_khong: Int?,
    val ma_may_bay: Int?,
    val ma_chuyen_bay: String,
    val ma_tuyen_bay: Int?,
    val gio_khoi_hanh: String,
    val gio_ha_canh: String,
    val tan_suat: String,
    val trang_thai: String,
    val hang_hang_khong: Airline? = null,
    val may_bay: AirlineAircraft? = null,
    val tuyen_bay: Route? = null
)

data class ApprovedRoute(
    val id: Int,
    val san_bay_di: Airport?,
    val san_bay_den: Airport?,
    val khoang_cach: Double? = null,
    val thoi_gian_bay: Int? = null,
    val duoc_phe_duyet: Boolean? = null
)

