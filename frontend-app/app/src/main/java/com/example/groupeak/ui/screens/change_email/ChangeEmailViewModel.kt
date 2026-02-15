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
                val msg = e.message.orEmpty()
                uiState = when {
                    msg.contains("invalid credentials", ignoreCase = true) -> {
                        uiState.copy(passwordError = "Неверный пароль")
                    }
                    msg.contains("invalid email", ignoreCase = true) -> {
                        uiState.copy(newEmailError = "Неверный формат email")
                    }
                    msg.contains("already", ignoreCase = true) -> {
                        uiState.copy(newEmailError = "Этот email уже используется")
                    }
                    else -> {
                        uiState.copy(newEmailError = msg.ifBlank { "Ошибка" })
                    }
                }
            }
        }
    }
}
