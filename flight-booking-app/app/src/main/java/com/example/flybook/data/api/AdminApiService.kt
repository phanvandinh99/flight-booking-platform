package com.example.flybook.data.api

import com.example.flybook.data.models.Airline
import com.example.flybook.data.models.Airport
import com.example.flybook.data.models.Route
import com.example.flybook.data.models.MonthlyRevenue
import com.example.flybook.data.models.RevenueSummary
import com.example.flybook.data.models.TopAirline
import com.example.flybook.data.models.SystemConfig
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
    
    // Route Management
    @GET("admin/routes")
    suspend fun getRoutes(): Response<ApiResponse<List<Route>>>
    
    @GET("admin/routes/{id}")
    suspend fun getRoute(@Path("id") id: Int): Response<ApiResponse<Route>>
    
    @POST("admin/routes")
    suspend fun createRoute(@Body request: CreateRouteRequest): Response<ApiResponse<Route>>
    
    @PUT("admin/routes/{id}")
    suspend fun updateRoute(@Path("id") id: Int, @Body request: UpdateRouteRequest): Response<ApiResponse<Route>>
    
    @DELETE("admin/routes/{id}")
    suspend fun deleteRoute(@Path("id") id: Int): Response<ApiResponse<Unit>>
    
    @POST("admin/routes/{id}/approve")
    suspend fun approveRoute(@Path("id") id: Int): Response<ApiResponse<Route>>
    
    @POST("admin/routes/{id}/revoke")
    suspend fun revokeRoute(@Path("id") id: Int): Response<ApiResponse<Route>>
    
    // Reports
    @GET("admin/reports/revenue/summary")
    suspend fun getRevenueSummary(): Response<ApiResponse<RevenueSummary>>
    
    @GET("admin/reports/revenue/monthly")
    suspend fun getMonthlyRevenue(
        @Query("tu_ngay") tuNgay: String? = null,
        @Query("den_ngay") denNgay: String? = null
    ): Response<ApiResponse<List<MonthlyRevenue>>>
    
    @GET("admin/reports/top-airlines")
    suspend fun getTopAirlines(@Query("limit") limit: Int = 10): Response<ApiResponse<List<TopAirline>>>
    
    // System Configuration
    @GET("admin/config")
    suspend fun getConfigs(): Response<ApiResponse<List<SystemConfig>>>
    
    @POST("admin/config")
    suspend fun createConfig(@Body request: CreateConfigRequest): Response<ApiResponse<SystemConfig>>
    
    @PUT("admin/config/{key}")
    suspend fun updateConfig(@Path("key") key: String, @Body request: UpdateConfigRequest): Response<ApiResponse<SystemConfig>>
    
    @DELETE("admin/config/{key}")
    suspend fun deleteConfig(@Path("key") key: String): Response<ApiResponse<Unit>>
}

data class CreateConfigRequest(
    val ten_cau_hinh: String,
    val gia_tri: String
)

data class UpdateConfigRequest(
    val gia_tri: String
)

data class CreateRouteRequest(
    val san_bay_di: Int,
    val san_bay_den: Int,
    val duoc_phe_duyet: Boolean? = false
)

data class UpdateRouteRequest(
    val san_bay_di: Int? = null,
    val san_bay_den: Int? = null,
    val duoc_phe_duyet: Boolean? = null
)

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

