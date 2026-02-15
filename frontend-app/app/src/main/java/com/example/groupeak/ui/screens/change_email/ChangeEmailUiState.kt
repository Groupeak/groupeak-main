package com.example.groupeak.ui.screens.change_email

data class ChangeEmailUiState(
    val currentEmail: String = "for@example.com",
    val newEmail: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false, // ← для анимации
    val newEmailError: String? = null,
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false
)