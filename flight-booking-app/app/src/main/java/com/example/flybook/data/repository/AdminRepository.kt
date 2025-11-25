package com.example.flybook.data.repository

import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.api.ErrorResponse
import com.example.flybook.data.models.Airline
import com.google.gson.Gson
import retrofit2.HttpException

class AdminRepository {
    private val adminApiService = ApiClient.adminApiService
    
    suspend fun getPendingAirlines(): Result<List<Airline>> {
        return try {
            val response = adminApiService.getPendingAirlines()
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
    
    suspend fun approveAirline(id: Int): Result<Airline> {
        return try {
            val response = adminApiService.approveAirline(id)
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
    
    suspend fun rejectAirline(id: Int): Result<Airline> {
        return try {
            val response = adminApiService.rejectAirline(id)
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
    
    suspend fun activateAirline(id: Int): Result<Airline> {
        return try {
            val response = adminApiService.activateAirline(id)
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
    
    suspend fun suspendAirline(id: Int): Result<Airline> {
        return try {
            val response = adminApiService.suspendAirline(id)
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

