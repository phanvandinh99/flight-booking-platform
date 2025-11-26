package com.example.flybook.data.repository

import android.util.Log
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
                    // Log flight data để debug
                    apiResponse.data.forEach { flight ->
                        Log.d("CustomerFlightRepository", "Flight ${flight.ma_chuyen_bay}: tuyen_bay=${flight.tuyen_bay?.id}, " +
                                "san_bay_di=${flight.tuyen_bay?.san_bay_di?.ma_san_bay}, " +
                                "san_bay_den=${flight.tuyen_bay?.san_bay_den?.ma_san_bay}")
                    }
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
                    val flight = apiResponse.data
                    Log.d("CustomerFlightRepository", "Flight detail ${flight.ma_chuyen_bay}: tuyen_bay=${flight.tuyen_bay?.id}, " +
                            "san_bay_di=${flight.tuyen_bay?.san_bay_di?.ma_san_bay}, " +
                            "san_bay_den=${flight.tuyen_bay?.san_bay_den?.ma_san_bay}")
                    Result.success(flight)
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
    
    suspend fun getFlightSeats(id: Int): Result<com.example.flybook.data.models.SeatData> {
        return try {
            android.util.Log.d("CustomerFlightRepository", "Calling getFlightSeats($id)...")
            val response = apiService.getFlightSeats(id)
            android.util.Log.d("CustomerFlightRepository", "getFlightSeats response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                android.util.Log.d("CustomerFlightRepository", "Response body: success=${apiResponse.success}, data=${apiResponse.data}")
                
                // Backend may return { "data": {...} } without success field
                if (apiResponse.data != null) {
                    android.util.Log.d("CustomerFlightRepository", "SeatData loaded: ma_chuyen_bay=${apiResponse.data.ma_chuyen_bay}, tong_so_ghe=${apiResponse.data.tong_so_ghe}")
                    Result.success(apiResponse.data)
                } else {
                    val errorMsg = apiResponse.message ?: "Không thể tải thông tin ghế"
                    android.util.Log.e("CustomerFlightRepository", "API returned data=null: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("CustomerFlightRepository", "Error response body: $errorBody")
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tải thông tin ghế: ${response.code()}"
                    }
                } else {
                    "Không thể tải thông tin ghế: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            android.util.Log.e("CustomerFlightRepository", "HttpException in getFlightSeats: code=${e.code()}, message=${e.message}", e)
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("CustomerFlightRepository", "HttpException error body: $errorBody")
            val errorMessage = if (!errorBody.isNullOrEmpty()) {
                try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (parseException: Exception) {
                    e.message()
                }
            } else {
                e.message()
            }
            Result.failure(Exception(errorMessage ?: "Không thể tải thông tin ghế"))
        } catch (e: Exception) {
            android.util.Log.e("CustomerFlightRepository", "Exception in getFlightSeats: ${e.message}", e)
            Result.failure(Exception("Không thể tải thông tin ghế: ${e.message}"))
        }
    }
}

