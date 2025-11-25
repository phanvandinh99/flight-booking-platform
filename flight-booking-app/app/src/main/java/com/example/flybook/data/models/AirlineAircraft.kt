package com.example.flybook.data.models

data class AirlineAircraft(
    val id: Int,
    val ma_hang_hang_khong: Int?,
    val loai_may_bay: String,
    val tong_so_ghe: Int,
    val so_do_ghe: Map<String, Any>? = null,
    val hang_hang_khong: Airline? = null
)

