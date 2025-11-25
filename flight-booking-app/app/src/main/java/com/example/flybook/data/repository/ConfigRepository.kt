package com.example.flybook.data.repository

import com.example.flybook.data.api.AdminApiService
import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.api.CreateConfigRequest
import com.example.flybook.data.api.ErrorResponse
import com.example.flybook.data.api.UpdateConfigRequest
import com.example.flybook.data.models.SystemConfig
import com.google.gson.Gson
import retrofit2.HttpException

class ConfigRepository {
    private val apiService: AdminApiService = ApiClient.adminApiService
    
    suspend fun getConfigs(): Result<List<SystemConfig>> {
        return try {
            val response = apiService.getConfigs()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tải danh sách cấu hình"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tải danh sách cấu hình: ${response.message()}"
                    }
                } else {
                    "Không thể tải danh sách cấu hình: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể tải danh sách cấu hình"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải danh sách cấu hình: ${e.message}"))
        }
    }
    
    suspend fun createConfig(tenCauHinh: String, giaTri: String): Result<SystemConfig> {
        return try {
            val request = CreateConfigRequest(tenCauHinh, giaTri)
            val response = apiService.createConfig(request)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tạo cấu hình"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tạo cấu hình: ${response.message()}"
                    }
                } else {
                    "Không thể tạo cấu hình: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể tạo cấu hình"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tạo cấu hình: ${e.message}"))
        }
    }
    
    suspend fun updateConfig(key: String, giaTri: String): Result<SystemConfig> {
        return try {
            val request = UpdateConfigRequest(giaTri)
            val response = apiService.updateConfig(key, request)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể cập nhật cấu hình"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể cập nhật cấu hình: ${response.message()}"
                    }
                } else {
                    "Không thể cập nhật cấu hình: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể cập nhật cấu hình"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể cập nhật cấu hình: ${e.message}"))
        }
    }
    
    suspend fun deleteConfig(key: String): Result<Unit> {
        return try {
            val response = apiService.deleteConfig(key)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể xóa cấu hình: ${response.message()}"
                    }
                } else {
                    "Không thể xóa cấu hình: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể xóa cấu hình"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể xóa cấu hình: ${e.message}"))
        }
    }
}

