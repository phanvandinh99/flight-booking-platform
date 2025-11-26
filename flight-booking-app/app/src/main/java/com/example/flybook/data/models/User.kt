package com.example.flybook.data.models

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val ten_day_du: String?,
    val email: String,
    val so_dien_thoai: String?,
    val vai_tro: String,
    @SerializedName("ma_hang_hang_khong")
    val hang_hang_khong_id: Int? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val ten_day_du: String,
    val email: String,
    val so_dien_thoai: String,
    val password: String,
    val password_confirmation: String
)

data class AuthResponse(
    val message: String,
    val user: User,
    val token: String
)

