package com.example.groupeak.ui.screens.change_password

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.domain.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



data class ChangePasswordUiState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val oldPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isOldPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false
)

class ChangePasswordViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    var uiState by mutableStateOf(ChangePasswordUiState())
        private set

    fun onOldPasswordChange(value: String) {
        uiState = uiState.copy(oldPassword = value, oldPasswordError = null)
    }

    fun onNewPasswordChange(value: String) {
        uiState = uiState.copy(newPassword = value, newPasswordError = null)
    }

    fun onConfirmPasswordChange(value: String) {
        uiState = uiState.copy(confirmPassword = value, confirmPasswordError = null)
    }

    fun onChangeClick() {
        var hasError = false

        // === Клиентская валидация ===
        if (uiState.oldPassword.isBlank()) {
            uiState = uiState.copy(oldPasswordError = "Введите старый пароль")
            hasError = true
        }
        if (uiState.newPassword.isBlank()) {
            uiState = uiState.copy(newPasswordError = "Введите новый пароль")
            hasError = true
        } else if (uiState.newPassword.length < 8) {
            uiState = uiState.copy(newPasswordError = "Минимум 8 символов")
            hasError = true
        }
        if (uiState.confirmPassword.isBlank()) {
            uiState = uiState.copy(confirmPasswordError = "Заполните все поля")
            hasError = true
        } else if (uiState.newPassword != uiState.confirmPassword) {
            uiState = uiState.copy(confirmPasswordError = "Пароли не совпадают",newPasswordError = "Пароли не совпадают")
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
                handleBackendError(e.message.orEmpty())
            }
        }
    }

    /**
     * Обработка ошибок от бэкенда
     */
    private fun handleBackendError(message: String) {
        when {
            message.contains("invalid credentials", ignoreCase = true) -> {
                uiState = uiState.copy(oldPasswordError = "Неверный текущий пароль")
            }
            message.contains("password must be at least 8 characters long", ignoreCase = true) -> {
                uiState = uiState.copy(newPasswordError = "Минимум 8 символов")
            }
            message.contains("new password must not match old password", ignoreCase = true) -> {
                uiState = uiState.copy(newPasswordError = "Новый пароль должен отличаться")
            }
            message.contains("confirm_new does not match new_password", ignoreCase = true) -> {
                uiState = uiState.copy(
                    newPasswordError = "Пароли не совпадают",      // Текст ошибки (покажем под вторым полем)
                    confirmPasswordError = "Пароли не совпадают"   // Дублируем, чтобы подсветить и это поле
                )
            }
            message.contains("full_name, email and password are required", ignoreCase = true) -> {
                uiState = uiState.copy(newPasswordError = "Заполните все поля")
            }
            message.contains("internal server error", ignoreCase = true) -> {
                uiState = uiState.copy(newPasswordError = "Сервер недоступен. Попробуйте позже")
            }
            else -> {
                uiState = uiState.copy(newPasswordError = "Ошибка при смене пароля")
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