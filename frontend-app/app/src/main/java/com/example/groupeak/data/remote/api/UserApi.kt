package com.example.groupeak.data.remote.api

import com.example.groupeak.data.remote.dto.ChangeEmailRequest
import com.example.groupeak.data.remote.dto.ChangeEmailResponse
import com.example.groupeak.data.remote.dto.ChangePasswordRequest
import com.example.groupeak.data.remote.dto.ChangePasswordResponse
import com.example.groupeak.data.remote.dto.ProfileResponse
import com.example.groupeak.data.remote.dto.UpdateProfileRequest
import com.example.groupeak.data.remote.dto.UpdateProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApi {

    @GET("api/v1/user/profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("api/v1/user/profile")
    suspend fun updateProfile(
        @Body body: UpdateProfileRequest,
    ): UpdateProfileResponse

    @POST("api/v1/user/change-email")
    suspend fun changeEmail(
        @Body body: ChangeEmailRequest,
    ): ChangeEmailResponse

    @POST("api/v1/user/change-password")
    suspend fun changePassword(
        @Body body: ChangePasswordRequest,
    ): ChangePasswordResponse
}
