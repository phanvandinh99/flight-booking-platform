package com.example.flybook.data.repository

import com.example.flybook.data.api.AirlineApiService
import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.api.ErrorResponse
import com.example.flybook.data.models.*
import com.google.gson.Gson
import retrofit2.HttpException

class AirlineReportsRepository {
    private val apiService: AirlineApiService = ApiClient.airlineApiService

    suspend fun getOverviewReport(
        tuNgay: String? = null,
        denNgay: String? = null
    ): Result<OverviewReport> {
        return try {
            val response = apiService.getOverviewReport(tuNgay, denNgay)
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

    suspend fun getDailyRevenue(
        tuNgay: String? = null,
        denNgay: String? = null
    ): Result<List<DailyRevenue>> {
        return try {
            val response = apiService.getDailyRevenue(tuNgay, denNgay)
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

    suspend fun getWeeklyRevenue(
        tuNgay: String? = null,
        denNgay: String? = null
    ): Result<List<WeeklyRevenue>> {
        return try {
            val response = apiService.getWeeklyRevenue(tuNgay, denNgay)
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

    suspend fun getMonthlyRevenue(
        tuNgay: String? = null,
        denNgay: String? = null
    ): Result<List<AirlineMonthlyRevenue>> {
        return try {
            val response = apiService.getMonthlyRevenue(tuNgay, denNgay)
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

    suspend fun getFlightReport(
        tuNgay: String? = null,
        denNgay: String? = null
    ): Result<List<FlightReport>> {
        return try {
            val response = apiService.getFlightReport(tuNgay, denNgay)
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

    suspend fun getFareClassReport(
        tuNgay: String? = null,
        denNgay: String? = null
    ): Result<List<FareClassReport>> {
        return try {
            val response = apiService.getFareClassReport(tuNgay, denNgay)
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

