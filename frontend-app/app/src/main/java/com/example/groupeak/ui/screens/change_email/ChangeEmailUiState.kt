package com.example.groupeak.ui.screens.change_email

data class ChangeEmailUiState(
    val currentEmail: String = "",
    val newEmail: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val newEmailError: String? = null,
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false
)
