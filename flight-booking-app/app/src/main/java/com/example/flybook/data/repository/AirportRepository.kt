package com.example.flybook.data.repository

import com.example.flybook.data.api.AdminApiService
import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.api.ApiResponse
import com.example.flybook.data.api.CreateAirportRequest
import com.example.flybook.data.api.ErrorResponse
import com.example.flybook.data.api.UpdateAirportRequest
import com.example.flybook.data.models.Airport
import com.google.gson.Gson
import retrofit2.HttpException

class AirportRepository {
    private val apiService: AdminApiService = ApiClient.adminApiService
    
    suspend fun getAirports(): Result<List<Airport>> {
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
            Result.failure(e)
        }
    }
    
    suspend fun getAirport(id: Int): Result<Airport> {
        return try {
            val response = apiService.getAirport(id)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tải thông tin sân bay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tải thông tin sân bay: ${response.message()}"
                    }
                } else {
                    "Không thể tải thông tin sân bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể tải thông tin sân bay"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createAirport(
        maSanBay: String,
        tenSanBay: String,
        thanhPho: String,
        quocGia: String
    ): Result<Airport> {
        return try {
            val request = CreateAirportRequest(maSanBay, tenSanBay, thanhPho, quocGia)
            val response = apiService.createAirport(request)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tạo sân bay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tạo sân bay: ${response.message()}"
                    }
                } else {
                    "Không thể tạo sân bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể tạo sân bay"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateAirport(
        id: Int,
        maSanBay: String? = null,
        tenSanBay: String? = null,
        thanhPho: String? = null,
        quocGia: String? = null
    ): Result<Airport> {
        return try {
            val request = UpdateAirportRequest(maSanBay, tenSanBay, thanhPho, quocGia)
            val response = apiService.updateAirport(id, request)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể cập nhật sân bay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể cập nhật sân bay: ${response.message()}"
                    }
                } else {
                    "Không thể cập nhật sân bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể cập nhật sân bay"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteAirport(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteAirport(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể xóa sân bay: ${response.message()}"
                    }
                } else {
                    "Không thể xóa sân bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể xóa sân bay"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

