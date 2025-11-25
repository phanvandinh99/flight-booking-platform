package com.example.flybook.data.api

import com.example.flybook.data.models.AirlineAircraft
import com.example.flybook.data.models.AirlineFlight
import com.example.flybook.data.models.ApprovedRoute
import retrofit2.Response
import retrofit2.http.*

interface AirlineApiService {
    // Aircraft Management
    @GET("airline/aircrafts")
    suspend fun getAircrafts(): Response<ApiResponse<List<AirlineAircraft>>>
    
    @GET("airline/aircrafts/{id}")
    suspend fun getAircraft(@Path("id") id: Int): Response<ApiResponse<AirlineAircraft>>
    
    @POST("airline/aircrafts")
    suspend fun createAircraft(@Body request: CreateAircraftRequest): Response<ApiResponse<AirlineAircraft>>
    
    @PUT("airline/aircrafts/{id}")
    suspend fun updateAircraft(@Path("id") id: Int, @Body request: UpdateAircraftRequest): Response<ApiResponse<AirlineAircraft>>
    
    @DELETE("airline/aircrafts/{id}")
    suspend fun deleteAircraft(@Path("id") id: Int): Response<ApiResponse<Unit>>
    
    // Flight Management
    @GET("airline/flights")
    suspend fun getFlights(
        @Query("ngay_khoi_hanh") ngayKhoiHanh: String? = null,
        @Query("trang_thai") trangThai: String? = null,
        @Query("ma_tuyen_bay") maTuyenBay: Int? = null
    ): Response<ApiResponse<List<AirlineFlight>>>
    
    @GET("airline/flights/{id}")
    suspend fun getFlight(@Path("id") id: Int): Response<ApiResponse<AirlineFlight>>
    
    @POST("airline/flights")
    suspend fun createFlight(@Body request: CreateFlightRequest): Response<ApiResponse<AirlineFlight>>
    
    @PUT("airline/flights/{id}")
    suspend fun updateFlight(@Path("id") id: Int, @Body request: UpdateFlightRequest): Response<ApiResponse<AirlineFlight>>
    
    @DELETE("airline/flights/{id}")
    suspend fun deleteFlight(@Path("id") id: Int): Response<ApiResponse<Unit>>
    
    @GET("airline/flights/routes/approved")
    suspend fun getApprovedRoutes(): Response<ApiResponse<List<ApprovedRoute>>>
}

data class CreateAircraftRequest(
    val loai_may_bay: String,
    val tong_so_ghe: Int,
    val so_do_ghe: Map<String, Any>? = null
)

data class UpdateAircraftRequest(
    val loai_may_bay: String? = null,
    val tong_so_ghe: Int? = null,
    val so_do_ghe: Map<String, Any>? = null
)

data class CreateFlightRequest(
    val ma_may_bay: Int,
    val ma_chuyen_bay: String,
    val ma_tuyen_bay: Int,
    val gio_khoi_hanh: String,
    val gio_ha_canh: String,
    val tan_suat: String,
    val trang_thai: String? = "du_kien"
)

data class UpdateFlightRequest(
    val ma_may_bay: Int? = null,
    val ma_chuyen_bay: String? = null,
    val ma_tuyen_bay: Int? = null,
    val gio_khoi_hanh: String? = null,
    val gio_ha_canh: String? = null,
    val tan_suat: String? = null,
    val trang_thai: String? = null
)

