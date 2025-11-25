package com.example.flybook.data.api

import com.example.flybook.data.models.AirlineAircraft
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

