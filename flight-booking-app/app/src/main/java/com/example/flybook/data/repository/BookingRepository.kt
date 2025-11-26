package com.example.flybook.data.repository

import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.api.ErrorResponse
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
                // Parse error message từ response
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        com.google.gson.Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        null
                    }
                } else null
                Result.failure(Exception(errorMessage ?: response.message() ?: "Không thể tạo URL thanh toán"))
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = if (!errorBody.isNullOrEmpty()) {
                try {
                    com.google.gson.Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (ex: Exception) {
                    null
                }
            } else null
            Result.failure(Exception(errorMessage ?: e.message ?: "Không thể tạo URL thanh toán"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cancelBooking(id: Int): Result<Booking> {
        return try {
            val response = apiService.cancelBooking(id)
            if (response.isSuccessful) {
                // Backend có thể trả về data hoặc chỉ message
                val booking = response.body()?.data
                if (booking != null) {
                    Result.success(booking)
                } else {
                    // Nếu không có data, vẫn coi là thành công và reload từ server
                    Result.success(Booking(
                        id = id,
                        ma_dat_ve = "",
                        chuyen_bay = null,
                        chuyen_bay_ve = null,
                        tong_tien = 0.0,
                        trang_thai = "da_huy",
                        phuong_thuc_thanh_toan = null,
                        ngay_dat = "",
                        hanh_khach = null,
                        thoi_gian_het_han = null
                    ))
                }
            } else {
                // Parse error message từ response
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        com.google.gson.Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        null
                    }
                } else null
                Result.failure(Exception(errorMessage ?: response.message() ?: "Không thể hủy đặt vé"))
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = if (!errorBody.isNullOrEmpty()) {
                try {
                    com.google.gson.Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (ex: Exception) {
                    null
                }
            } else null
            Result.failure(Exception(errorMessage ?: e.message ?: "Không thể hủy đặt vé"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

