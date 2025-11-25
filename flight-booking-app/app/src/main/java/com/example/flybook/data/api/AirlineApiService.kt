package com.example.flybook.data.api

import com.example.flybook.data.models.AirlineAircraft
import com.example.flybook.data.models.AirlineFlight
import com.example.flybook.data.models.AirlineFarePrice
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
    
    // Fare Price Management
    @GET("airline/pricing")
    suspend fun getFarePrices(
        @Query("ma_chuyen_bay") maChuyenBay: Int? = null,
        @Query("hang_ve") hangVe: String? = null,
        @Query("ngay_bat_dau") ngayBatDau: String? = null
    ): Response<ApiResponse<List<AirlineFarePrice>>>
    
    @GET("airline/pricing/{id}")
    suspend fun getFarePrice(@Path("id") id: Int): Response<ApiResponse<AirlineFarePrice>>
    
    @POST("airline/pricing")
    suspend fun createFarePrice(@Body request: CreateFarePriceRequest): Response<ApiResponse<AirlineFarePrice>>
    
    @PUT("airline/pricing/{id}")
    suspend fun updateFarePrice(@Path("id") id: Int, @Body request: UpdateFarePriceRequest): Response<ApiResponse<AirlineFarePrice>>
    
    @DELETE("airline/pricing/{id}")
    suspend fun deleteFarePrice(@Path("id") id: Int): Response<ApiResponse<Unit>>
    
    @GET("airline/pricing/flights")
    suspend fun getFlightsForPricing(): Response<ApiResponse<List<AirlineFlight>>>
    
    // Booking Management
    @GET("airline/bookings")
    suspend fun getBookings(
        @Query("ma_chuyen_bay") maChuyenBay: Int? = null,
        @Query("trang_thai") trangThai: String? = null,
        @Query("ngay_dat") ngayDat: String? = null,
        @Query("ma_dat_ve") maDatVe: String? = null
    ): Response<ApiResponse<List<com.example.flybook.data.models.AirlineBooking>>>
    
    @GET("airline/bookings/{id}")
    suspend fun getBooking(@Path("id") id: Int): Response<ApiResponse<com.example.flybook.data.models.AirlineBooking>>
    
    @PUT("airline/bookings/{id}/status")
    suspend fun updateBookingStatus(
        @Path("id") id: Int,
        @Body request: UpdateBookingStatusRequest
    ): Response<ApiResponse<com.example.flybook.data.models.AirlineBooking>>
    
    @GET("airline/bookings/statistics")
    suspend fun getBookingStatistics(
        @Query("tu_ngay") tuNgay: String? = null,
        @Query("den_ngay") denNgay: String? = null
    ): Response<ApiResponse<com.example.flybook.data.models.BookingStatistics>>
    
    @GET("airline/bookings/flights")
    suspend fun getFlightsForBookings(): Response<ApiResponse<List<AirlineFlight>>>

    // Reports
    @GET("airline/reports/overview")
    suspend fun getOverviewReport(
        @Query("tu_ngay") tuNgay: String? = null,
        @Query("den_ngay") denNgay: String? = null
    ): Response<ApiResponse<com.example.flybook.data.models.OverviewReport>>

    @GET("airline/reports/daily-revenue")
    suspend fun getDailyRevenue(
        @Query("tu_ngay") tuNgay: String? = null,
        @Query("den_ngay") denNgay: String? = null
    ): Response<ApiResponse<List<com.example.flybook.data.models.DailyRevenue>>>

    @GET("airline/reports/weekly-revenue")
    suspend fun getWeeklyRevenue(
        @Query("tu_ngay") tuNgay: String? = null,
        @Query("den_ngay") denNgay: String? = null
    ): Response<ApiResponse<List<com.example.flybook.data.models.WeeklyRevenue>>>

    @GET("airline/reports/monthly-revenue")
    suspend fun getMonthlyRevenue(
        @Query("tu_ngay") tuNgay: String? = null,
        @Query("den_ngay") denNgay: String? = null
    ): Response<ApiResponse<List<com.example.flybook.data.models.AirlineMonthlyRevenue>>>

    @GET("airline/reports/flight-report")
    suspend fun getFlightReport(
        @Query("tu_ngay") tuNgay: String? = null,
        @Query("den_ngay") denNgay: String? = null
    ): Response<ApiResponse<List<com.example.flybook.data.models.FlightReport>>>

    @GET("airline/reports/fare-class-report")
    suspend fun getFareClassReport(
        @Query("tu_ngay") tuNgay: String? = null,
        @Query("den_ngay") denNgay: String? = null
    ): Response<ApiResponse<List<com.example.flybook.data.models.FareClassReport>>>
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

data class CreateFarePriceRequest(
    val ma_chuyen_bay: Int,
    val hang_ve: String,
    val gia: Double,
    val hanh_ly_ky_gui: String,
    val chinh_sach_huy_ve: String? = null,
    val chinh_sach_doi_ve: String? = null,
    val ngay_bat_dau: String,
    val ngay_ket_thuc: String
)

data class UpdateFarePriceRequest(
    val ma_chuyen_bay: Int? = null,
    val hang_ve: String? = null,
    val gia: Double? = null,
    val hanh_ly_ky_gui: String? = null,
    val chinh_sach_huy_ve: String? = null,
    val chinh_sach_doi_ve: String? = null,
    val ngay_bat_dau: String? = null,
    val ngay_ket_thuc: String? = null
)

data class UpdateBookingStatusRequest(
    val trang_thai: String // giu_cho, da_thanh_toan, da_huy
)

