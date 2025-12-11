package com.example.groupeak.data.remote.api

import com.example.groupeak.data.remote.dto.AuthResponse
import com.example.groupeak.data.remote.dto.LoginRequest
import com.example.groupeak.data.remote.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): AuthResponse

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): AuthResponse
}
