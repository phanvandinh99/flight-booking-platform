package com.example.flybook.data.api

import com.example.flybook.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Authentication
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
    
    @GET("me")
    suspend fun getMe(): Response<MeResponse>
    
    @PUT("profile")
    suspend fun updateProfile(@Body user: User): Response<ApiResponse<User>>
    
    // Public - Search
    @GET("search/airports")
    suspend fun getAirports(): Response<ApiResponse<List<Airport>>>
    
    @GET("search/airlines")
    suspend fun getAirlines(): Response<ApiResponse<List<Airline>>>
    
    @POST("search/flights")
    suspend fun searchFlights(@Body request: FlightSearchRequest): Response<ApiResponse<FlightSearchResponse>>
    
    @GET("search/flights/{id}")
    suspend fun getFlightDetail(@Path("id") id: Int): Response<ApiResponse<Flight>>
    
    @GET("search/flights/today")
    suspend fun getTodayFlights(): Response<ApiResponse<List<Flight>>>
    
    @GET("search/flights/list")
    suspend fun getFlightList(@QueryMap params: Map<String, String>): Response<ApiResponse<List<Flight>>>
    
    @GET("search/flights/{id}/seats")
    suspend fun getFlightSeats(@Path("id") id: Int): Response<ApiResponse<SeatData>>
    
    // Customer - Bookings
    @POST("customer/bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): Response<ApiResponse<Booking>>
    
    @GET("customer/bookings")
    suspend fun getBookings(@QueryMap params: Map<String, String>): Response<ApiResponse<List<Booking>>>
    
    @GET("customer/bookings/{id}")
    suspend fun getBooking(@Path("id") id: Int): Response<ApiResponse<Booking>>
    
    @POST("customer/bookings/{id}/payment")
    suspend fun createPayment(@Path("id") id: Int, @Body request: PaymentRequest): Response<ApiResponse<PaymentResponse>>
    
    @POST("customer/bookings/{id}/payment/confirm")
    suspend fun confirmPayment(@Path("id") id: Int, @Body params: Map<String, String>): Response<ApiResponse<Booking>>
    
    @PUT("customer/bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: Int): Response<ApiResponse<Booking>>
}

data class ApiResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val success: Boolean = true
)

data class MeResponse(
    val user: User
)

data class ErrorResponse(
    val message: String? = null,
    val errors: Map<String, List<String>>? = null
)

