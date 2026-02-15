// app/src/main/java/com/example/groupeak/ui/screens/change_password/ChangePasswordViewModel.kt
package com.example.groupeak.ui.screens.change_password

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class ChangePasswordViewModel : ViewModel() {

    var uiState by mutableStateOf(ChangePasswordUiState())
        private set

    fun onOldPasswordChange(value: String) {
        uiState = uiState.copy(oldPassword = value, oldPasswordError = null)
    }

    fun onNewPasswordChange(value: String) {
        uiState = uiState.copy(newPassword = value, newPasswordError = null, confirmPasswordError = null)
    }

    fun onConfirmPasswordChange(value: String) {
        uiState = uiState.copy(confirmPassword = value, confirmPasswordError = null)
    }

    fun onChangeClick() {
        var hasError = false

        if (uiState.oldPassword.isBlank()) {
            uiState = uiState.copy(oldPasswordError = "Введите старый пароль")
            hasError = true
        }
        if (uiState.newPassword.isBlank()) {
            uiState = uiState.copy(newPasswordError = "Введите новый пароль")
            hasError = true
        }
        if (uiState.confirmPassword.isBlank()) {
            uiState = uiState.copy(confirmPasswordError = "Подтвердите пароль")
            hasError = true
        } else if (uiState.newPassword != uiState.confirmPassword) {
            uiState = uiState.copy(confirmPasswordError = "Пароли не совпадают")
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
            uiState = uiState.copy(isSuccess = false) // ← сброс после 2 сек
        }
    }

    fun toggleOldPasswordVisibility() {
        uiState = uiState.copy(isOldPasswordVisible = !uiState.isOldPasswordVisible)
    }

    fun toggleNewPasswordVisibility() {
        uiState = uiState.copy(isNewPasswordVisible = !uiState.isNewPasswordVisible)
    }

    fun toggleConfirmPasswordVisibility() {
        uiState = uiState.copy(isConfirmPasswordVisible = !uiState.isConfirmPasswordVisible)
    }
}