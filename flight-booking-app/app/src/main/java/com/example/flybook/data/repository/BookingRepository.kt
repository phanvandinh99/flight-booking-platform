package com.example.flybook.data.repository

import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.models.*

class BookingRepository {
    private val apiService = ApiClient.apiService
    
    suspend fun createBooking(request: CreateBookingRequest): Result<Booking> {
        return try {
            val response = apiService.createBooking(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to create booking"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBookings(params: Map<String, String> = emptyMap()): Result<List<Booking>> {
        return try {
            val response = apiService.getBookings(params)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to load bookings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBooking(id: Int): Result<Booking> {
        return try {
            val response = apiService.getBooking(id)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to load booking"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createPayment(bookingId: Int, bankCode: String? = null): Result<PaymentResponse> {
        return try {
            val response = apiService.createPayment(bookingId, PaymentRequest(bankCode))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to create payment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cancelBooking(id: Int): Result<Booking> {
        return try {
            val response = apiService.cancelBooking(id)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to cancel booking"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

