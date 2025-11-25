package com.example.flybook.data.api

import com.example.flybook.data.models.Airline
import com.example.flybook.data.models.Airport
import retrofit2.Response
import retrofit2.http.*

interface AdminApiService {
    // Airline Approval
    @GET("admin/airlines/pending")
    suspend fun getPendingAirlines(): Response<ApiResponse<List<Airline>>>
    
    @POST("admin/airlines/{id}/approve")
    suspend fun approveAirline(@Path("id") id: Int): Response<ApiResponse<Airline>>
    
    @POST("admin/airlines/{id}/reject")
    suspend fun rejectAirline(@Path("id") id: Int): Response<ApiResponse<Airline>>
    
    @POST("admin/airlines/{id}/activate")
    suspend fun activateAirline(@Path("id") id: Int): Response<ApiResponse<Airline>>
    
    @POST("admin/airlines/{id}/suspend")
    suspend fun suspendAirline(@Path("id") id: Int): Response<ApiResponse<Airline>>
    
    // Airport Management
    @GET("admin/airports")
    suspend fun getAirports(): Response<ApiResponse<List<Airport>>>
    
    @GET("admin/airports/{id}")
    suspend fun getAirport(@Path("id") id: Int): Response<ApiResponse<Airport>>
    
    @POST("admin/airports")
    suspend fun createAirport(@Body request: CreateAirportRequest): Response<ApiResponse<Airport>>
    
    @PUT("admin/airports/{id}")
    suspend fun updateAirport(@Path("id") id: Int, @Body request: UpdateAirportRequest): Response<ApiResponse<Airport>>
    
    @DELETE("admin/airports/{id}")
    suspend fun deleteAirport(@Path("id") id: Int): Response<ApiResponse<Unit>>
}

data class CreateAirportRequest(
    val ma_san_bay: String,
    val ten_san_bay: String,
    val thanh_pho: String,
    val quoc_gia: String
)

data class UpdateAirportRequest(
    val ma_san_bay: String? = null,
    val ten_san_bay: String? = null,
    val thanh_pho: String? = null,
    val quoc_gia: String? = null
)

