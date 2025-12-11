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
)

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun onFioChange(value: String) {
        uiState = uiState.copy(fio = value, errorMessage = null)
    }

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null, passwordsDoNotMatch = false)
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
                Log.e("Auth", "Register error", e)
                // TODO: сделать обработку ошибок с бэка
                uiState = uiState.copy(errorMessage = "Ошибка регистрации. Попробуйте ещё раз.")
            }
        }
    }
}
