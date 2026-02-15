package com.example.groupeak.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.data.local.SessionManager
import com.example.groupeak.domain.repository.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    private var backupBeforeEdit: ProfileUiState? = null

    init {
        viewModelScope.launch {
            sessionManager.user.collectLatest { user ->
                if (user == null) return@collectLatest
                if (uiState.isEditing) return@collectLatest

                uiState = uiState.copy(
                    fullName = user.fullName,
                    position = user.position.orEmpty(),
                    birthDate = backendBirthDateToDigits(user.birthDate),
                    about = user.about.orEmpty(),
                )
            }
        }

        refreshProfile()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            userRepository.getProfile()
        }
    }

    fun onEditClick() {
        if (uiState.isEditing) return
        backupBeforeEdit = uiState
        uiState = uiState.copy(isEditing = true)
    }

    fun onSaveClick() {
        val fullName = uiState.fullName.trim()
        if (fullName.isBlank()) return

        val birthDateForBackend = digitsToBackendBirthDate(uiState.birthDate) ?: return
        val aboutForBackend = uiState.about.let { if (it.isBlank()) "" else it.trim() }
        val positionForBackend = uiState.position.trim()

        viewModelScope.launch {
            val result = userRepository.updateProfile(
                fullName = fullName,
                birthDate = birthDateForBackend,
                about = aboutForBackend,
                position = positionForBackend,
                avatarUrl = null,
            )

            result.onSuccess { user ->
                backupBeforeEdit = null
                uiState = uiState.copy(
                    isEditing = false,
                    fullName = user.fullName,
                    position = user.position.orEmpty(),
                    birthDate = backendBirthDateToDigits(user.birthDate),
                    about = user.about.orEmpty(),
                )
            }
        }
    }

    fun onCancelEdit() {
        val backup = backupBeforeEdit
        uiState = (backup ?: uiState).copy(isEditing = false)
        backupBeforeEdit = null
    }

    fun onFullNameChange(value: String) {
        uiState = uiState.copy(fullName = value)
    }

    fun onBirthDateChange(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(8) // DDMMYYYY
        uiState = uiState.copy(birthDate = digits)
    }

    fun onPositionChange(value: String) {
        if (!uiState.isEditing) return
        uiState = uiState.copy(position = value)
    }

    fun onAboutChange(value: String) {
        uiState = uiState.copy(about = value)
    }

    private fun digitsToBackendBirthDate(digits: String): String? {
        val raw = digits.filter { it.isDigit() }.take(8)
        if (raw.isEmpty()) return "" // очистить дату на бэке
        if (raw.length < 8) return null
        val dd = raw.substring(0, 2)
        val mm = raw.substring(2, 4)
        val yyyy = raw.substring(4, 8)
        return "$dd.$mm.$yyyy"
    }

    private fun backendBirthDateToDigits(birthDate: String?): String {
        return birthDate
            ?.filter { it.isDigit() }
            ?.take(8)
            .orEmpty()
    }
}
