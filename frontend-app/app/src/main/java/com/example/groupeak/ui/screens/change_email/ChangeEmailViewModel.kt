package com.example.groupeak.ui.screens.change_email

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.data.local.SessionManager
import com.example.groupeak.domain.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

class ChangeEmailViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    var uiState by mutableStateOf(ChangeEmailUiState())
        private set

    init {
        viewModelScope.launch {
            sessionManager.user.collectLatest { user ->
                uiState = uiState.copy(currentEmail = user?.email.orEmpty())
            }
        }
    }

    fun onNewEmailChange(value: String) {
        uiState = uiState.copy(newEmail = value, newEmailError = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, passwordError = null)
    }

    fun togglePasswordVisibility() {
        uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
    }

    fun onChangeClick() {
        val state = uiState
        var hasError = false

        // === Клиентская валидация ===
        if (state.newEmail.isBlank()) {
            uiState = uiState.copy(newEmailError = "Введите новый email")
            hasError = true
        } else if (!isValidEmail(state.newEmail)) {
            uiState = uiState.copy(newEmailError = "Введите корректный email")
            hasError = true
        }

        if (state.password.isBlank()) {
            uiState = uiState.copy(passwordError = "Введите пароль")
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            val result = userRepository.changeEmail(
                newEmail = state.newEmail.trim(),
                password = state.password,
            )

            uiState = uiState.copy(isLoading = false)

            result.onSuccess {
                uiState = uiState.copy(
                    newEmail = "",
                    password = "",
                    isSuccess = true,
                    newEmailError = null,
                    passwordError = null,
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
     * [backend] new_email is required -> Введите новый email.
     * [backend] password is required -> Введите пароль.
     * [backend] invalid credentials -> Неверный пароль.
     * [backend] new email must not match current email -> Новый email совпадает с текущим.
     * [backend] invalid email format -> Введите корректный email.
     * [backend] email domain is not supported -> Домен этого email не поддерживается.
     * [backend] email already taken -> Этот email уже используется.
     * [backend] internal server error -> Сервер недоступен. Попробуйте позже.
     */
    private fun handleBackendError(message: String) {
        when {
            // Введите новый email
            message.contains("new_email is required", ignoreCase = true) -> {
                uiState = uiState.copy(newEmailError = "Введите новый email")
            }
            // Введите пароль
            message.contains("password is required", ignoreCase = true) -> {
                uiState = uiState.copy(passwordError = "Введите пароль")
            }
            // Неверный пароль
            message.contains("invalid credentials", ignoreCase = true) -> {
                uiState = uiState.copy(passwordError = "Неверный пароль")
            }
            // Новый email совпадает с текущим
            message.contains("new email must not match current email", ignoreCase = true) -> {
                uiState = uiState.copy(newEmailError = "Новый email совпадает с текущим")
            }
            // Введите корректный email
            message.contains("invalid email format", ignoreCase = true) -> {
                uiState = uiState.copy(newEmailError = "Введите корректный email")
            }
            // Домен этого email не поддерживается
            message.contains("email domain is not supported", ignoreCase = true) -> {
                uiState = uiState.copy(newEmailError = "Домен этого email не поддерживается")
            }
            // Этот email уже используется
            message.contains("email already taken", ignoreCase = true) -> {
                uiState = uiState.copy(newEmailError = "Этот email уже используется")
            }
            // Сервер недоступен
            message.contains("internal server error", ignoreCase = true) -> {
                uiState = uiState.copy(newEmailError = "Сервер недоступен. Попробуйте позже")
            }
            // fallback для неизвестных ошибок
            else -> {
                uiState = uiState.copy(newEmailError = "Ошибка при смене email")
            }
        }
    }

    /**
     * Простая валидация email формата
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}