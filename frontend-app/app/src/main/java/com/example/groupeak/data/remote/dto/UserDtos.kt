package com.example.groupeak.data.remote.dto

import com.google.gson.annotations.SerializedName

// ----- Запросы -----

data class UpdateProfileRequest(
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    // backend ожидает dd.MM.yyyy; пустая строка => очистить дату
    @SerializedName("birth_date") val birthDate: String? = null,
    @SerializedName("position") val position: String? = null,
    // пустая строка => очистить поле
    @SerializedName("about") val about: String? = null,
)

data class ChangeEmailRequest(
    @SerializedName("new_email") val newEmail: String,
    @SerializedName("password") val password: String,
)

data class ChangePasswordRequest(
    @SerializedName("old_password") val oldPassword: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("confirm_new") val confirmNew: String,
)

// ----- Ответы -----

data class ProfileResponse(
    @SerializedName("user") val user: UserDto,
)

data class UpdateProfileResponse(
    @SerializedName("user") val user: UserDto,
    @SerializedName("token") val token: String,
)

data class ChangeEmailResponse(
    @SerializedName("user") val user: UserDto,
    @SerializedName("token") val token: String,
)

data class ChangePasswordResponse(
    @SerializedName("user") val user: UserDto,
    @SerializedName("token") val token: String,
)
