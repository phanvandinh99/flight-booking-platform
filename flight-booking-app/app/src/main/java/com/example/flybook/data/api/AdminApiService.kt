package com.example.flybook.data.api

import com.example.flybook.data.models.Airline
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
}

