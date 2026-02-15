package com.example.groupeak.ui.screens.change_email

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class ChangeEmailViewModel : ViewModel() {

    var uiState by mutableStateOf(ChangeEmailUiState())
        private set

    fun onNewEmailChange(value: String) {
        uiState = uiState.copy(newEmail = value, newEmailError = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, passwordError = null)
    }

    fun togglePasswordVisibility() {
        uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
    }

    fun onChangeClick() { // ← без onSuccess
        val state = uiState

        var hasError = false

        if (state.newEmail.isBlank()) {
            uiState = uiState.copy(newEmailError = "Введите новую почту")
            hasError = true
        } else if (!state.newEmail.contains("@")) {
            uiState = uiState.copy(newEmailError = "Неверный формат email")
            hasError = true
        }

        if (state.password.isBlank()) {
            uiState = uiState.copy(passwordError = "Введите пароль")
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            delay(800)
            uiState = uiState.copy(
                isLoading = false,
                isSuccess = true
            )
            delay(2000)
            uiState = uiState.copy(isSuccess = false)
        }
    }
}