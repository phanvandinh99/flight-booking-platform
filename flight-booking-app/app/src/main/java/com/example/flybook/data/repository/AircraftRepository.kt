package com.example.flybook.data.repository

import com.example.flybook.data.api.AirlineApiService
import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.api.CreateAircraftRequest
import com.example.flybook.data.api.ErrorResponse
import com.example.flybook.data.api.UpdateAircraftRequest
import com.example.flybook.data.models.AirlineAircraft
import com.google.gson.Gson
import retrofit2.HttpException

class AircraftRepository {
    private val apiService: AirlineApiService = ApiClient.airlineApiService
    
    suspend fun getAircrafts(): Result<List<AirlineAircraft>> {
        return try {
            val response = apiService.getAircrafts()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tải danh sách máy bay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tải danh sách máy bay: ${response.message()}"
                    }
                } else {
                    "Không thể tải danh sách máy bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể tải danh sách máy bay"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải danh sách máy bay: ${e.message}"))
        }
    }
    
    suspend fun createAircraft(loaiMayBay: String, tongSoGhe: Int, soDoGhe: Map<String, Any>? = null): Result<AirlineAircraft> {
        return try {
            val request = CreateAircraftRequest(loaiMayBay, tongSoGhe, soDoGhe)
            val response = apiService.createAircraft(request)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tạo máy bay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tạo máy bay: ${response.message()}"
                    }
                } else {
                    "Không thể tạo máy bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể tạo máy bay"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tạo máy bay: ${e.message}"))
        }
    }
    
    suspend fun updateAircraft(id: Int, loaiMayBay: String? = null, tongSoGhe: Int? = null, soDoGhe: Map<String, Any>? = null): Result<AirlineAircraft> {
        return try {
            val request = UpdateAircraftRequest(loaiMayBay, tongSoGhe, soDoGhe)
            val response = apiService.updateAircraft(id, request)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể cập nhật máy bay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể cập nhật máy bay: ${response.message()}"
                    }
                } else {
                    "Không thể cập nhật máy bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể cập nhật máy bay"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể cập nhật máy bay: ${e.message}"))
        }
    }
    
    suspend fun deleteAircraft(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteAircraft(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể xóa máy bay: ${response.message()}"
                    }
                } else {
                    "Không thể xóa máy bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể xóa máy bay"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể xóa máy bay: ${e.message}"))
        }
    }
}

