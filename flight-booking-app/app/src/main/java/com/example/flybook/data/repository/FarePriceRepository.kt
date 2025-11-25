package com.example.flybook.data.repository

import com.example.flybook.data.api.AirlineApiService
import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.api.CreateFarePriceRequest
import com.example.flybook.data.api.ErrorResponse
import com.example.flybook.data.api.UpdateFarePriceRequest
import com.example.flybook.data.models.AirlineFarePrice
import com.example.flybook.data.models.AirlineFlight
import com.google.gson.Gson
import retrofit2.HttpException

class FarePriceRepository {
    private val apiService: AirlineApiService = ApiClient.airlineApiService
    
    suspend fun getFarePrices(
        maChuyenBay: Int? = null,
        hangVe: String? = null,
        ngayBatDau: String? = null
    ): Result<List<AirlineFarePrice>> {
        return try {
            val response = apiService.getFarePrices(maChuyenBay, hangVe, ngayBatDau)
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
    
    suspend fun getFarePrice(id: Int): Result<AirlineFarePrice> {
        return try {
            val response = apiService.getFarePrice(id)
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
    
    suspend fun createFarePrice(request: CreateFarePriceRequest): Result<AirlineFarePrice> {
        return try {
            val response = apiService.createFarePrice(request)
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
    
    suspend fun updateFarePrice(id: Int, request: UpdateFarePriceRequest): Result<AirlineFarePrice> {
        return try {
            val response = apiService.updateFarePrice(id, request)
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
    
    suspend fun deleteFarePrice(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteFarePrice(id)
            if (response.isSuccessful) {
                Result.success(Unit)
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
    
    suspend fun getFlightsForPricing(): Result<List<AirlineFlight>> {
        return try {
            val response = apiService.getFlightsForPricing()
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

