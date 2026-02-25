package com.example.groupeak.domain.repository

import com.example.groupeak.data.remote.dto.UserDto

interface UserRepository {

    suspend fun getProfile(): Result<UserDto>

    suspend fun updateProfile(
        fullName: String?,
        birthDate: String?,
        about: String?,
        position: String?,
        avatarUrl: String? = null,
    ): Result<UserDto>

    suspend fun changeEmail(
        newEmail: String,
        password: String,
    ): Result<UserDto>

    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        confirmNew: String,
    ): Result<UserDto>
}
