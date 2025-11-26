package com.example.flybook.data.repository

import android.util.Log
import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.api.ErrorResponse
import com.example.flybook.data.api.MeResponse
import com.example.flybook.data.models.*
import com.google.gson.Gson
import retrofit2.HttpException

class AuthRepository {
    private val apiService = ApiClient.apiService
    
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                ApiClient.setToken(authResponse.token)
                Result.success(authResponse)
            } else {
                // Parse error body để lấy message
                val errorMessage = try {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.message ?: "Đăng nhập thất bại"
                    } else {
                        "Đăng nhập thất bại"
                    }
                } catch (e: Exception) {
                    "Đăng nhập thất bại"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            // Xử lý HttpException
            val errorMessage = try {
                val errorBody = e.response()?.errorBody()?.string()
                if (errorBody != null) {
                    val gson = Gson()
                    val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                    errorResponse.message ?: "Đăng nhập thất bại"
                } else {
                    "Đăng nhập thất bại"
                }
            } catch (ex: Exception) {
                "Đăng nhập thất bại"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                ApiClient.setToken(authResponse.token)
                Result.success(authResponse)
            } else {
                // Parse error body để lấy message
                val errorMessage = try {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.message ?: "Đăng ký thất bại"
                    } else {
                        "Đăng ký thất bại"
                    }
                } catch (e: Exception) {
                    "Đăng ký thất bại"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            // Xử lý HttpException
            val errorMessage = try {
                val errorBody = e.response()?.errorBody()?.string()
                if (errorBody != null) {
                    val gson = Gson()
                    val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                    errorResponse.message ?: "Đăng ký thất bại"
                } else {
                    "Đăng ký thất bại"
                }
            } catch (ex: Exception) {
                "Đăng ký thất bại"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> {
        return try {
            val response = apiService.logout()
            if (response.isSuccessful) {
                ApiClient.setToken(null)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message() ?: "Logout failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMe(): Result<User> {
        return try {
            Log.d("AuthRepository", "Calling getMe() API...")
            val response = apiService.getMe()
            Log.d("AuthRepository", "getMe() response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val meResponse = response.body()!!
                Log.d("AuthRepository", "Response body: user=${meResponse.user}")
                
                if (meResponse.user != null) {
                    Log.d("AuthRepository", "User data loaded: ${meResponse.user.email}")
                    Result.success(meResponse.user)
                } else {
                    val errorMessage = "Không thể tải thông tin người dùng"
                    Log.e("AuthRepository", "API returned user=null: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Parse error body để lấy message
                val errorMessage = try {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AuthRepository", "Error response body: $errorBody")
                    if (errorBody != null) {
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.message ?: "Không thể tải thông tin người dùng"
                    } else {
                        "Không thể tải thông tin người dùng: ${response.code()}"
                    }
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Error parsing error body: ${e.message}", e)
                    "Không thể tải thông tin người dùng: ${response.code()}"
                }
                Log.e("AuthRepository", "getMe() failed with code ${response.code()}: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            // Xử lý HttpException
            Log.e("AuthRepository", "HttpException in getMe(): code=${e.code()}, message=${e.message}", e)
            val errorMessage = try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("AuthRepository", "HttpException error body: $errorBody")
                if (errorBody != null) {
                    val gson = Gson()
                    val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                    errorResponse.message ?: "Không thể tải thông tin người dùng"
                } else {
                    "Không thể tải thông tin người dùng: ${e.code()}"
                }
            } catch (ex: Exception) {
                Log.e("AuthRepository", "Error parsing HttpException error body: ${ex.message}", ex)
                "Không thể tải thông tin người dùng: ${e.code()}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception in getMe(): ${e.message}", e)
            Result.failure(Exception("Lỗi kết nối: ${e.message}"))
        }
    }
}

