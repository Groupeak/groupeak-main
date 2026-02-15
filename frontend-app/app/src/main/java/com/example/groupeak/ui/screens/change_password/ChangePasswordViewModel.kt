package com.example.groupeak.ui.screens.change_password

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.domain.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChangePasswordViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

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

        val state = uiState

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            val result = userRepository.changePassword(
                oldPassword = state.oldPassword,
                newPassword = state.newPassword,
                confirmNew = state.confirmPassword,
            )

            uiState = uiState.copy(isLoading = false)

            result.onSuccess {
                uiState = uiState.copy(
                    oldPassword = "",
                    newPassword = "",
                    confirmPassword = "",
                    isSuccess = true,
                    oldPasswordError = null,
                    newPasswordError = null,
                    confirmPasswordError = null,
                )
                delay(2000)
                uiState = uiState.copy(isSuccess = false)
            }.onFailure { e ->
                val msg = e.message.orEmpty()
                uiState = when {
                    msg.contains("invalid credentials", ignoreCase = true) -> {
                        uiState.copy(oldPasswordError = "Неверный старый пароль")
                    }
                    else -> {
                        uiState.copy(newPasswordError = msg.ifBlank { "Ошибка" })
                    }
                }
            }
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
