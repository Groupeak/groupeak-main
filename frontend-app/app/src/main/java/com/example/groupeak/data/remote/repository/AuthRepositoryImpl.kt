package com.example.groupeak.data.remote.repository

import com.example.groupeak.data.remote.api.AuthApi
import com.example.groupeak.data.remote.dto.AuthResponse
import com.example.groupeak.data.remote.dto.LoginRequest
import com.example.groupeak.data.remote.dto.RegisterRequest
import com.example.groupeak.domain.repository.AuthRepository

object Positions {
    const val BACKEND = "backend"
    const val FRONTEND = "frontend"
    const val UIUX = "uiux"
    const val PM = "pm"
}

class AuthRepositoryImpl(
    private val api: AuthApi,
) : AuthRepository {

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        position: String,
        avatarUrl: String?,
    ): Result<AuthResponse> = runCatching {
        api.register(
            RegisterRequest(
                fullName = fullName,
                email = email,
                password = password,
                position = position,
                avatarUrl = avatarUrl,
            )
        )
    }

    override suspend fun login(
        email: String,
        password: String,
    ): Result<AuthResponse> = runCatching {
        api.login(
            LoginRequest(
                email = email,
                password = password,
            )
        )
    }
}
