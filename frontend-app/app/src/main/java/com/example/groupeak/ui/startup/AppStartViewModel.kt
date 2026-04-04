package com.example.groupeak.ui.startup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.groupeak.data.local.SessionManager
import com.example.groupeak.domain.repository.UserRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface AppStartState {
    data object Loading : AppStartState
    data object Auth : AppStartState
    data object Main : AppStartState
}

class AppStartViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    var state by mutableStateOf<AppStartState>(AppStartState.Loading)
        private set

    init {
        bootstrap()
    }

    private fun bootstrap() {
        viewModelScope.launch {
            sessionManager.isRestored
                .filter { it }
                .first()

            val token = sessionManager.token.value

            if (token.isNullOrBlank()) {
                state = AppStartState.Auth
                return@launch
            }

            val result = userRepository.getProfile()
            result
                .onSuccess {
                    state = AppStartState.Main
                }
                .onFailure {
                    sessionManager.clear()
                    state = AppStartState.Auth
                }
        }
    }
}

class AppStartViewModelFactory(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppStartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppStartViewModel(userRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}