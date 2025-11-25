package com.example.flybook.data.models

data class Flight(
    val id: Int,
    val ma_chuyen_bay: String,
    val tuyen_bay: Route?,
    val hang_hang_khong: Airline?,
    val may_bay: Aircraft?,
    val gio_khoi_hanh: String,
    val gio_ha_canh: String,
    val gia_ve: List<Price>?,
    val tong_gia: Double? = null,
    val so_ghe_trong: Int? = null
)

data class Route(
    val id: Int,
    val san_bay_di: Airport,
    val san_bay_den: Airport,
    val khoang_cach: Double?,
    val thoi_gian_bay: Int?
)

data class Aircraft(
    val id: Int,
    val loai_may_bay: String,
    val so_ghe: Int?,
    val hang_hang_khong_id: Int?
)

data class Price(
    val id: Int,
    val hang_ve: String,
    val gia: Double,
    val so_ghe_trong: Int?
)

data class FlightSearchRequest(
    val san_bay_di: String,
    val san_bay_den: String,
    val ngay_khoi_hanh: String,
    val loai_chuyen: String = "mot_chieu",
    val ngay_ve: String? = null,
    val nguoi_lon: Int = 1,
    val tre_em: Int = 0,
    val em_be: Int = 0,
    val hang_ve: String? = null,
    val gia_tu: Double? = null,
    val gia_den: Double? = null,
    val gio_khoi_hanh_tu: String? = null,
    val gio_khoi_hanh_den: String? = null,
    val hang_hang_khong: List<Int>? = null
)

data class FlightSearchResponse(
    val loai_chuyen: String,
    val san_bay_di: Airport,
    val san_bay_den: Airport,
    val ngay_khoi_hanh: String,
    val ngay_ve: String?,
    val hanh_khach: PassengerCount,
    val chuyen_bay_di: List<Flight>,
    val chuyen_bay_ve: List<Flight>?
)

data class PassengerCount(
    val tong_so: Int,
    val nguoi_lon: Int,
    val tre_em: Int,
    val em_be: Int
)

