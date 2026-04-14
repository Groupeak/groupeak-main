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
        val fullName = uiState.fullName.normalizeSpaces()
        if (fullName.isBlank()) {
            uiState = uiState.copy(fullNameError = true)
            showStatusMessage("Имя не должно быть пустым", isError = true)
            return
        }

        if (!isValidFullName(fullName)) {
            uiState = uiState.copy(fullNameError = true)
            showStatusMessage("Некорректные данные", isError = true)
            return
        }

        val birthDateForBackend = digitsToBackendBirthDate(uiState.birthDate)
        if (birthDateForBackend == null) {
            uiState = uiState.copy(birthDateError = true)
            showStatusMessage("Неверный формат даты рождения", isError = true)
            return
        }

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
                showStatusMessage("Изменения сохранены", isError = false)
            }.onFailure { throwable ->
                val backendMessage = throwable.message?.trim()
                if (backendMessage == "invalid birth date, expected format dd.mm.yyyy" || backendMessage == "birth date must make age between 8 and 100 years") {
                    uiState = uiState.copy(birthDateError = true)
                }
                showStatusMessage(
                    mapProfileErrorToRussian(throwable.message),
                    isError = true,
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
        uiState = uiState.copy(fullName = value).clearStatus()
    }

    fun onBirthDateChange(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(8) // DDMMYYYY
        uiState = uiState.copy(birthDate = digits).clearStatus()
    }

    fun onPositionChange(value: String) {
        if (!uiState.isEditing) return
        uiState = uiState.copy(position = value).clearStatus()
    }

    fun onAboutChange(value: String) {
        uiState = uiState.copy(about = value).clearStatus()
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

    private fun showStatusMessage(message: String, isError: Boolean) {
        uiState = uiState.copy(
            statusMessage = message,
            statusMessageIsError = isError,
            statusMessagePulse = uiState.statusMessagePulse + 1,
        )
    }

    private fun mapProfileErrorToRussian(message: String?): String {
        return when (message?.trim()) {
            "invalid birth date, expected format dd.mm.yyyy" -> "Неверный формат даты рождения"
            "birth date must make age between 8 and 100 years" -> "Неверный формат даты рождения"
            "internal server error" -> "Сервер недоступен. Попробуйте позже"
            else -> "Сервер недоступен. Попробуйте позже"
        }
    }

    private fun isValidFullName(value: String): Boolean {
        val normalized = value.normalizeSpaces()
        if (normalized.isBlank()) return false

        val fullNameRegex = Regex("^[A-Za-zА-Яа-яЁё]+(?:[\\s-][A-Za-zА-Яа-яЁё]+)*$")
        return fullNameRegex.matches(normalized)
    }

    private fun String.normalizeSpaces(): String =
        trim().replace(Regex("\\s+"), " ")

    private fun ProfileUiState.clearStatus(): ProfileUiState = copy(
        statusMessage = null,
        statusMessageIsError = false,
        fullNameError = false,
        birthDateError = false,
    )
}