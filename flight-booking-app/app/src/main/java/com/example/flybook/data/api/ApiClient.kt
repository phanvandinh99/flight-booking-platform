package com.example.flybook.data.api

import android.util.Log
import com.example.flybook.data.models.Route
import com.example.flybook.data.models.RouteDeserializer
import com.example.flybook.data.models.AirlineAircraft
import com.example.flybook.data.models.AirlineAircraftDeserializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8000/api/" // Android emulator uses 10.0.2.2 for localhost
    // For real device, use your computer's IP: "http://192.168.x.x:8000/api/"
    
    private var token: String? = null
    
    fun setToken(newToken: String?) {
        token = newToken
        if (newToken != null) {
            Log.d("ApiClient", "Token set in ApiClient (length: ${newToken.length}, first 10 chars: ${newToken.take(10)}...)")
        } else {
            Log.d("ApiClient", "Token cleared from ApiClient")
        }
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Chỉ log headers và basic info để tránh vấn đề với response lớn
        level = HttpLoggingInterceptor.Level.HEADERS
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            
            token?.let {
                requestBuilder.header("Authorization", "Bearer $it")
                Log.d("ApiClient", "Adding Authorization header to request: ${original.url} (token length: ${it.length})")
            } ?: run {
                Log.w("ApiClient", "No token available for request: ${original.url}")
            }
            
            requestBuilder.header("Accept", "application/json")
            requestBuilder.header("Content-Type", "application/json")
            
            val request = requestBuilder.build()
            val response = chain.proceed(request)
            
            Log.d("ApiClient", "Response for ${request.url}: code=${response.code}")
            
            response
        }
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Route::class.java, RouteDeserializer())
        .registerTypeAdapter(AirlineAircraft::class.java, AirlineAircraftDeserializer())
        .setLenient()
        .create()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
    val adminApiService: AdminApiService = retrofit.create(AdminApiService::class.java)
    val airlineApiService: AirlineApiService = retrofit.create(AirlineApiService::class.java)
}

