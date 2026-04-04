package com.example.groupeak.data.remote.repository

import com.example.groupeak.data.local.SessionManager
import com.example.groupeak.data.remote.api.UserApi
import com.example.groupeak.data.remote.dto.ChangeEmailRequest
import com.example.groupeak.data.remote.dto.ChangePasswordRequest
import com.example.groupeak.data.remote.dto.UpdateProfileRequest
import com.example.groupeak.data.remote.dto.UserDto
import com.example.groupeak.data.remote.errors.parseApiError
import com.example.groupeak.domain.repository.UserRepository

class UserRepositoryImpl(
    private val api: UserApi,
    private val sessionManager: SessionManager,
) : UserRepository {

    override suspend fun getProfile(): Result<UserDto> {
        return try {
            val resp = api.getProfile()
            sessionManager.updateUser(resp.user)
            Result.success(resp.user)
        } catch (e: Throwable) {
            Result.failure(parseApiError(e) {
                sessionManager.clear()
            })
        }
    }

    override suspend fun updateProfile(
        fullName: String?,
        birthDate: String?,
        about: String?,
        position: String?,
        avatarUrl: String?,
    ): Result<UserDto> {
        return try {
            val resp = api.updateProfile(
                UpdateProfileRequest(
                    fullName = fullName,
                    birthDate = birthDate,
                    about = about,
                    position = position,
                    avatarUrl = avatarUrl,
                )
            )

            sessionManager.saveToken(resp.token)
            sessionManager.updateUser(resp.user)

            Result.success(resp.user)
        } catch (e: Throwable) {
            Result.failure(parseApiError(e) {
                sessionManager.clear()
            })
        }
    }

    override suspend fun changeEmail(newEmail: String, password: String): Result<UserDto> {
        return try {
            val resp = api.changeEmail(ChangeEmailRequest(newEmail = newEmail, password = password))
            sessionManager.saveToken(resp.token)
            sessionManager.updateUser(resp.user)
            Result.success(resp.user)
        } catch (e: Throwable) {
            Result.failure(parseApiError(e) {
                sessionManager.clear()
            })
        }
    }

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        confirmNew: String,
    ): Result<UserDto> {
        return try {
            val resp = api.changePassword(
                ChangePasswordRequest(
                    oldPassword = oldPassword,
                    newPassword = newPassword,
                    confirmNew = confirmNew,
                )
            )
            sessionManager.saveToken(resp.token)
            sessionManager.updateUser(resp.user)
            Result.success(resp.user)
        } catch (e: Throwable) {
            Result.failure(parseApiError(e) {
                sessionManager.clear()
            })
        }
    }
}