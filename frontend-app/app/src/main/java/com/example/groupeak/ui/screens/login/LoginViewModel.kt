package com.example.groupeak.ui.screens.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.domain.repository.AuthRepository
import kotlinx.coroutines.launch
import com.example.groupeak.BuildConfig

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(
            email = value,
            errorMessage = null,
            emailError = null
        )
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(
            password = value,
            errorMessage = null,
            passwordError = null
        )
    }

    fun onLoginClick(onSuccess: () -> Unit) {
//        if (BuildConfig.DEV_BYPASS_AUTH) {
//            uiState = uiState.copy(
//                isLoading = false,
//                errorMessage = null,
//                emailError = null,
//                passwordError = null
//            )
//            onSuccess()
//            return
//        }

        val current = uiState

        // Очищаем старые ошибки
        var newState = current.copy(
            emailError = null,
            passwordError = null,
            errorMessage = null
        )

        // Локальная проверка
        if (current.email.isBlank()) {
            newState = newState.copy(emailError = "Введите email")
        }
        if (current.password.isBlank()) {
            newState = newState.copy(passwordError = "Введите пароль")
        }

        // Если есть ошибки валидации - показываем и выходим
        if (newState.emailError != null || newState.passwordError != null) {
            uiState = newState
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null,
                emailError = null,
                passwordError = null
            )

            val result = authRepository.login(
                email = current.email,
                password = current.password,
            )

            uiState = uiState.copy(isLoading = false)

            result.onSuccess { auth ->
                Log.d("Auth", "Login success, token = ${auth.token}")
                onSuccess()
            }.onFailure { e ->
                Log.e("Auth", "Login error", e)

                // Очищаем старые ошибки
                var errorState = uiState.copy(
                    emailError = null,
                    passwordError = null,
                    errorMessage = null
                )

                when {
                    e.message?.contains("invalid credentials", true) == true -> {
                        errorState = errorState.copy(errorMessage = "Неверный email или пароль")
                    }

                    e.message?.contains("invalid email format", true) == true -> {
                        errorState = errorState.copy(emailError = "Неверный формат email")
                    }

                    e.message?.contains("email domain is not supported", true) == true -> {
                        errorState = errorState.copy(emailError = "Домен email не поддерживается")
                    }

                    else -> {
                        errorState = errorState.copy(errorMessage = e.message ?: "Неизвестная ошибка")
                    }
                }

                uiState = errorState
            }
        }
    }
}
