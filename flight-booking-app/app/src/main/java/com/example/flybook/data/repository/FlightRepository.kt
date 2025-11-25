package com.example.flybook.data.repository

import com.example.flybook.data.api.ApiClient
import com.example.flybook.data.models.*

class FlightRepository {
    private val apiService = ApiClient.apiService
    
    suspend fun getAirports(): Result<List<Airport>> {
        return try {
            val response = apiService.getAirports()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to load airports"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAirlines(): Result<List<Airline>> {
        return try {
            val response = apiService.getAirlines()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to load airlines"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchFlights(request: FlightSearchRequest): Result<FlightSearchResponse> {
        return try {
            val response = apiService.searchFlights(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to search flights"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFlightDetail(id: Int): Result<Flight> {
        return try {
            val response = apiService.getFlightDetail(id)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to load flight detail"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTodayFlights(): Result<List<Flight>> {
        return try {
            val response = apiService.getTodayFlights()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to load today flights"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

