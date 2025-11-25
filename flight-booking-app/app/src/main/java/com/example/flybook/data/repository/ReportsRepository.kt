package com.example.flybook.data.repository

import com.example.flybook.data.api.AdminApiService
import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.api.ErrorResponse
import com.example.flybook.data.models.MonthlyRevenue
import com.example.flybook.data.models.RevenueSummary
import com.example.flybook.data.models.TopAirline
import com.google.gson.Gson
import retrofit2.HttpException

class ReportsRepository {
    private val apiService: AdminApiService = ApiClient.adminApiService
    
    suspend fun getRevenueSummary(): Result<RevenueSummary> {
        return try {
            val response = apiService.getRevenueSummary()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tải tổng doanh thu"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tải tổng doanh thu: ${response.message()}"
                    }
                } else {
                    "Không thể tải tổng doanh thu: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = if (!errorBody.isNullOrEmpty()) {
                try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (parseException: Exception) {
                    e.message()
                }
            } else {
                e.message()
            }
            Result.failure(Exception(errorMessage ?: "Không thể tải tổng doanh thu"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải tổng doanh thu: ${e.message}"))
        }
    }
    
    suspend fun getMonthlyRevenue(tuNgay: String? = null, denNgay: String? = null): Result<List<MonthlyRevenue>> {
        return try {
            val response = apiService.getMonthlyRevenue(tuNgay, denNgay)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tải doanh thu theo tháng"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tải doanh thu theo tháng: ${response.message()}"
                    }
                } else {
                    "Không thể tải doanh thu theo tháng: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = if (!errorBody.isNullOrEmpty()) {
                try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (parseException: Exception) {
                    e.message()
                }
            } else {
                e.message()
            }
            Result.failure(Exception(errorMessage ?: "Không thể tải doanh thu theo tháng"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải doanh thu theo tháng: ${e.message}"))
        }
    }
    
    suspend fun getTopAirlines(limit: Int = 10): Result<List<TopAirline>> {
        return try {
            val response = apiService.getTopAirlines(limit)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tải top hãng hàng không"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tải top hãng hàng không: ${response.message()}"
                    }
                } else {
                    "Không thể tải top hãng hàng không: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = if (!errorBody.isNullOrEmpty()) {
                try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (parseException: Exception) {
                    e.message()
                }
            } else {
                e.message()
            }
            Result.failure(Exception(errorMessage ?: "Không thể tải top hãng hàng không"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải top hãng hàng không: ${e.message}"))
        }
    }
}

