package com.example.flybook.data.repository

import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.api.ErrorResponse
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
            val response = apiService.getMe()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Get user failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

