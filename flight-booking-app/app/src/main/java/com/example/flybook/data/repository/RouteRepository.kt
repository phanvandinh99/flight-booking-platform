package com.example.flybook.data.repository

import com.example.flybook.data.api.AdminApiService
import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.api.ApiResponse
import com.example.flybook.data.api.CreateRouteRequest
import com.example.flybook.data.api.ErrorResponse
import com.example.flybook.data.api.UpdateRouteRequest
import com.example.flybook.data.models.Route
import com.google.gson.Gson
import retrofit2.HttpException

class RouteRepository {
    private val apiService: AdminApiService = ApiClient.adminApiService
    
    suspend fun getRoutes(): Result<List<Route>> {
        return try {
            val response = apiService.getRoutes()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tải danh sách tuyến bay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tải danh sách tuyến bay: ${response.message()}"
                    }
                } else {
                    "Không thể tải danh sách tuyến bay: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            // Handle JSON parsing errors (including "End of input")
            val errorMessage = "Lỗi parse dữ liệu: ${e.message}. Có thể do response quá lớn hoặc bị cắt."
            Result.failure(Exception(errorMessage))
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
            Result.failure(Exception(errorMessage ?: "Không thể tải danh sách tuyến bay"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải danh sách tuyến bay: ${e.message}"))
        }
    }
    
    suspend fun createRoute(
        sanBayDi: Int,
        sanBayDen: Int,
        duocPheDuyet: Boolean = false
    ): Result<Route> {
        return try {
            val request = CreateRouteRequest(sanBayDi, sanBayDen, duocPheDuyet)
            val response = apiService.createRoute(request)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể tạo tuyến bay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể tạo tuyến bay: ${response.message()}"
                    }
                } else {
                    "Không thể tạo tuyến bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể tạo tuyến bay"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateRoute(
        id: Int,
        sanBayDi: Int? = null,
        sanBayDen: Int? = null,
        duocPheDuyet: Boolean? = null
    ): Result<Route> {
        return try {
            val request = UpdateRouteRequest(sanBayDi, sanBayDen, duocPheDuyet)
            val response = apiService.updateRoute(id, request)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể cập nhật tuyến bay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể cập nhật tuyến bay: ${response.message()}"
                    }
                } else {
                    "Không thể cập nhật tuyến bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể cập nhật tuyến bay"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteRoute(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteRoute(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể xóa tuyến bay: ${response.message()}"
                    }
                } else {
                    "Không thể xóa tuyến bay: ${response.message()}"
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
            Result.failure(Exception(errorMessage ?: "Không thể xóa tuyến bay"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun approveRoute(id: Int): Result<Route> {
        return try {
            val response = apiService.approveRoute(id)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể phê duyệt tuyến bay"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể phê duyệt tuyến bay: ${response.message()}"
                    }
                } else {
                    "Không thể phê duyệt tuyến bay: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            // Handle JSON parsing errors
            val errorMessage = "Lỗi parse dữ liệu: ${e.message}"
            Result.failure(Exception(errorMessage))
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
            Result.failure(Exception(errorMessage ?: "Không thể phê duyệt tuyến bay"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể phê duyệt tuyến bay: ${e.message}"))
        }
    }
    
    suspend fun revokeRoute(id: Int): Result<Route> {
        return try {
            val response = apiService.revokeRoute(id)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Không thể thu hồi phê duyệt"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Không thể thu hồi phê duyệt: ${response.message()}"
                    }
                } else {
                    "Không thể thu hồi phê duyệt: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            // Handle JSON parsing errors
            val errorMessage = "Lỗi parse dữ liệu: ${e.message}"
            Result.failure(Exception(errorMessage))
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
            Result.failure(Exception(errorMessage ?: "Không thể thu hồi phê duyệt"))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể thu hồi phê duyệt: ${e.message}"))
        }
    }
}

