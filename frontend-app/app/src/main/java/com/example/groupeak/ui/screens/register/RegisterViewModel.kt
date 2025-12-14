package com.example.groupeak.ui.screens.register

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.domain.repository.AuthRepository
import kotlinx.coroutines.launch

data class RegisterUiState(
    val fio: String = "",
    val email: String = "",
    val password: String = "",
    val secondPassword: String = "",
    val passwordsDoNotMatch: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Добавляем только эти поля
    val fioError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun onFioChange(value: String) {
        uiState = uiState.copy(fio = value, errorMessage = null, fioError = null)
    }

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null, emailError = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null, passwordsDoNotMatch = false, passwordError = null)
    }

    fun onSecondPasswordChange(value: String) {
        uiState = uiState.copy(secondPassword = value, errorMessage = null, passwordsDoNotMatch = false)
    }

    fun onRegisterClick(onSuccess: () -> Unit) {
        val current = uiState

        // Проверка совпадения паролей
        val passwordsDoNotMatch =
            current.password.isNotEmpty() &&
                    current.secondPassword.isNotEmpty() &&
                    current.password != current.secondPassword

        if (passwordsDoNotMatch) {
            uiState = current.copy(passwordsDoNotMatch = true)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            val result = authRepository.register(
                fullName = current.fio,
                email = current.email,
                password = current.password,
                position = "frontend",
                avatarUrl = null
            )

            uiState = uiState.copy(isLoading = false)

            result.onSuccess { auth ->
                Log.d("Auth", "Register success, token = ${auth.token}")
                onSuccess()
            }.onFailure { e ->
                // Очищаем старые ошибки
                var newState = current.copy(
                    fioError = null,
                    emailError = null,
                    passwordError = null,
                    errorMessage = null
                )

                when {
                    e.message?.contains("email already taken", true) == true -> {
                        newState = newState.copy(emailError = "Этот email уже используется")
                    }

                    e.message?.contains("invalid email format", true) == true -> {
                        newState = newState.copy(emailError = "Неверный формат email")
                    }

                    e.message?.contains("email domain is not supported", true) == true -> {
                        newState = newState.copy(emailError = "Домен email не поддерживается")
                    }

                    e.message?.contains("password must be at least", true) == true -> {
                        newState = newState.copy(passwordError = "Пароль должен быть не менее 8 символов")
                    }

                    e.message?.contains("full_name", true) == true -> {
                        // Проверяем что пустое
                        if (current.fio.isEmpty()) {
                            newState = newState.copy(fioError = "Введите ФИО")
                        }
                        if (current.email.isEmpty()) {
                            newState = newState.copy(emailError = "Введите email")
                        }
                        if (current.password.isEmpty()) {
                            newState = newState.copy(passwordError = "Введите пароль")
                        }
                    }

                    else -> {
                        newState = newState.copy(errorMessage = e.message ?: "Неизвестная ошибка")
                    }
                }

                uiState = newState
            }
        }
    }
}