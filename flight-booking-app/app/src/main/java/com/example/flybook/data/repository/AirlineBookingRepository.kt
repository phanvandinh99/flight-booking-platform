package com.example.flybook.data.repository

import com.example.flybook.data.api.AirlineApiService
import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.api.ErrorResponse
import com.example.flybook.data.api.UpdateBookingStatusRequest
import com.example.flybook.data.models.AirlineBooking
import com.example.flybook.data.models.AirlineFlight
import com.example.flybook.data.models.BookingStatistics
import com.google.gson.Gson
import retrofit2.HttpException

class AirlineBookingRepository {
    private val apiService: AirlineApiService = ApiClient.airlineApiService
    
    suspend fun getBookings(
        maChuyenBay: Int? = null,
        trangThai: String? = null,
        ngayDat: String? = null,
        maDatVe: String? = null
    ): Result<List<AirlineBooking>> {
        return try {
            val response = apiService.getBookings(maChuyenBay, trangThai, ngayDat, maDatVe)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMessage = parseErrorMessage(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = parseHttpException(e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBooking(id: Int): Result<AirlineBooking> {
        return try {
            val response = apiService.getBooking(id)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMessage = parseErrorMessage(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = parseHttpException(e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateBookingStatus(id: Int, trangThai: String): Result<AirlineBooking> {
        return try {
            val response = apiService.updateBookingStatus(id, UpdateBookingStatusRequest(trangThai))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMessage = parseErrorMessage(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = parseHttpException(e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBookingStatistics(tuNgay: String? = null, denNgay: String? = null): Result<BookingStatistics> {
        return try {
            val response = apiService.getBookingStatistics(tuNgay, denNgay)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMessage = parseErrorMessage(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = parseHttpException(e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFlightsForBookings(): Result<List<AirlineFlight>> {
        return try {
            val response = apiService.getFlightsForBookings()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMessage = parseErrorMessage(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = parseHttpException(e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseErrorMessage(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val gson = Gson()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message ?: "Thao tác thất bại"
            } else {
                "Thao tác thất bại"
            }
        } catch (e: Exception) {
            "Thao tác thất bại"
        }
    }
    
    private fun parseHttpException(e: HttpException): String {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody != null) {
                val gson = Gson()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message ?: "Thao tác thất bại"
            } else {
                "Thao tác thất bại"
            }
        } catch (ex: Exception) {
            "Thao tác thất bại"
        }
    }
}

