package com.example.groupeak.data.remote.repository

import com.example.groupeak.data.local.SessionManager
import com.example.groupeak.data.remote.api.AuthApi
import com.example.groupeak.data.remote.dto.AuthResponse
import com.example.groupeak.data.remote.dto.LoginRequest
import com.example.groupeak.data.remote.dto.RegisterRequest
import com.example.groupeak.data.remote.errors.parseApiError
import com.example.groupeak.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val sessionManager: SessionManager,
) : AuthRepository {

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        position: String?,
        avatarUrl: String?,
    ): Result<AuthResponse> {
        return try {
            val resp = api.register(
                RegisterRequest(
                    fullName = fullName,
                    email = email,
                    password = password,
                    position = position,
                    avatarUrl = avatarUrl,
                )
            )

            sessionManager.saveToken(resp.token)
            sessionManager.updateUser(resp.user)

            Result.success(resp)
        } catch (e: Throwable) {
            Result.failure(parseApiError(e))
        }
    }

    override suspend fun login(
        email: String,
        password: String,
    ): Result<AuthResponse> {
        return try {
            val resp = api.login(
                LoginRequest(
                    email = email,
                    password = password,
                )
            )

            sessionManager.saveToken(resp.token)
            sessionManager.updateUser(resp.user)

            Result.success(resp)
        } catch (e: Throwable) {
            Result.failure(parseApiError(e))
        }
    }
}
