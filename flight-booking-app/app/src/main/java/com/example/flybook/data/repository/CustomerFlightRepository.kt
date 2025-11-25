package com.example.flybook.data.repository

import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.api.ApiService
import com.example.flybook.data.api.ErrorResponse
import com.example.flybook.data.models.Flight
import com.example.flybook.data.models.FlightSearchRequest
import com.example.flybook.data.models.FlightSearchResponse
import com.google.gson.Gson
import retrofit2.HttpException

class CustomerFlightRepository {
    private val apiService: ApiService = ApiClient.apiService
    
    suspend fun getAirports(): Result<List<com.example.flybook.data.models.Airport>> {
        return try {
            val response = apiService.getAirports()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tải danh sách sân bay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tải danh sách sân bay: ${response.message()}"
                    }
                } else {
                    "Không thể tải danh sách sân bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể tải danh sách sân bay"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải danh sách sân bay: ${e.message}"))
        }
    }
    
    suspend fun getTodayFlights(): Result<List<Flight>> {
        return try {
            val response = apiService.getTodayFlights()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tải danh sách chuyến bay hôm nay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tải danh sách chuyến bay hôm nay: ${response.message()}"
                    }
                } else {
                    "Không thể tải danh sách chuyến bay hôm nay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể tải danh sách chuyến bay hôm nay"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải danh sách chuyến bay hôm nay: ${e.message}"))
        }
    }
    
    suspend fun searchFlights(request: FlightSearchRequest): Result<FlightSearchResponse> {
        return try {
            val response = apiService.searchFlights(request)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tìm kiếm chuyến bay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tìm kiếm chuyến bay: ${response.message()}"
                    }
                } else {
                    "Không thể tìm kiếm chuyến bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể tìm kiếm chuyến bay"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tìm kiếm chuyến bay: ${e.message}"))
        }
    }
    
    suspend fun getFlightDetail(id: Int): Result<Flight> {
        return try {
            val response = apiService.getFlightDetail(id)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tải chi tiết chuyến bay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tải chi tiết chuyến bay: ${response.message()}"
                    }
                } else {
                    "Không thể tải chi tiết chuyến bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể tải chi tiết chuyến bay"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải chi tiết chuyến bay: ${e.message}"))
        }
    }
}

