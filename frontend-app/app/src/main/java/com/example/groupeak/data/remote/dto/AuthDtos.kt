package com.example.groupeak.data.remote.dto

import com.google.gson.annotations.SerializedName

// ----- Запросы -----

data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    // backend, frontend, uiux, pm
    @SerializedName("position") val position: String?,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
)

// ----- Ответы -----

data class UserDto(
    @SerializedName("id") val id: Long,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("email") val email: String,
    @SerializedName("position") val position: String?,
    @SerializedName("birth_date") val birthDate: String?,
    @SerializedName("about") val about: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
)

data class AuthResponse(
    @SerializedName("user") val user: UserDto,
    @SerializedName("token") val token: String,
)

// ----- Ошибки -----
data class ErrorResponse(
    @SerializedName("error") val error: String,
)
