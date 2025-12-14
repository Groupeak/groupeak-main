package com.example.groupeak.data.remote.repository

import com.example.groupeak.data.remote.api.AuthApi
import com.example.groupeak.data.remote.dto.AuthResponse
import com.example.groupeak.data.remote.dto.LoginRequest
import com.example.groupeak.data.remote.dto.RegisterRequest
import com.example.groupeak.domain.repository.AuthRepository

import com.example.groupeak.data.remote.errors.parseApiError

class AuthRepositoryImpl(
    private val api: AuthApi,
) : AuthRepository {

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        position: String,
        avatarUrl: String?,
    ): Result<AuthResponse> {
        return try {
            Result.success(
                api.register(
                    RegisterRequest(
                        fullName = fullName,
                        email = email,
                        password = password,
                        position = position,
                        avatarUrl = avatarUrl,
                    )
                )
            )
        } catch (e: Throwable) {
            Result.failure(parseApiError(e))
        }
    }

    override suspend fun login(
        email: String,
        password: String,
    ): Result<AuthResponse> {
        return try {
            Result.success(
                api.login(
                    LoginRequest(
                        email = email,
                        password = password,
                    )
                )
            )
        } catch (e: Throwable) {
            Result.failure(parseApiError(e))
        }
    }
}

