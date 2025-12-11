package com.example.groupeak.ui.screens.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.domain.repository.AuthRepository
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null)
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        val current = uiState

        if (current.email.isBlank() || current.password.isBlank()) {
            uiState = current.copy(errorMessage = "Введите email и пароль")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

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
                uiState = uiState.copy(errorMessage = "Не удалось войти. Проверьте данные.")
            }
        }
    }
}
