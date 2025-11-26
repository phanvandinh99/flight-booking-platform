package com.example.flybook.data.models

import com.google.gson.annotations.JsonAdapter

@JsonAdapter(SeatDataDeserializer::class)
data class SeatData(
    val ma_chuyen_bay: Int,
    val so_do_ghe: List<SeatLayout>? = null,
    val tong_so_ghe: Int,
    val ghe_da_dat: List<String>,
    val ghe_giu_cho: List<String>,
    val gia_ve: List<FareClassPrice>
)

data class SeatLayout(
    val number: String,
    val row: Int,
    val letter: String
)

data class FareClassPrice(
    val hang_ve: String,
    val gia: Double
)

