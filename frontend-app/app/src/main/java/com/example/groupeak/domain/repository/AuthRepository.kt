package com.example.groupeak.domain.repository

import com.example.groupeak.data.remote.dto.AuthResponse

interface AuthRepository {

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        position: String,
        avatarUrl: String? = null,
    ): Result<AuthResponse>

    suspend fun login(
        email: String,
        password: String,
    ): Result<AuthResponse>
}
