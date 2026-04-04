package com.example.groupeak.ui.screens.create_project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.domain.repository.ProjectRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class CreateProjectViewModel(
    private val projectRepository: ProjectRepository,
) : ViewModel() {

    var uiState by mutableStateOf(CreateProjectUiState())
        private set

    fun onProjectNameChange(value: String) {
        uiState = uiState.copy(
            projectName = value,
            projectNameError = null,
        ).clearStatus()
    }

    fun onDeadlineChange(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(8)
        uiState = uiState.copy(
            deadline = digits,
            deadlineError = null,
        ).clearStatus()
    }

    fun onDescriptionChange(value: String) {
        uiState = uiState.copy(description = value).clearStatus()
    }

    fun onCreateClick(onSuccess: () -> Unit) {
        val projectName = uiState.projectName.trim()
        val description = uiState.description.trim().takeIf { it.isNotEmpty() }

        var hasError = false

        if (projectName.isBlank()) {
            uiState = uiState.copy(projectNameError = "Введите название проекта")
            hasError = true
        }

        val deadlineForBackend = when {
            uiState.deadline.isBlank() -> null
            else -> deadlineDigitsToBackend(uiState.deadline)
        }

        if (uiState.deadline.isNotBlank() && deadlineForBackend == null) {
            uiState = uiState.copy(deadlineError = "Неверный формат даты")
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            val result = projectRepository.createProject(
                name = projectName,
                description = description,
                deadlineAt = deadlineForBackend,
            )

            uiState = uiState.copy(isLoading = false)

            result.onSuccess {
                onSuccess()
            }.onFailure { throwable ->
                handleBackendError(throwable.message.orEmpty())
            }
        }
    }

    private fun handleBackendError(message: String) {
        when {
            message.contains("project name is required", ignoreCase = true) -> {
                uiState = uiState.copy(projectNameError = "Введите название проекта")
            }
            message.contains("unauthorized", ignoreCase = true) -> {
                showStatusMessage("Нужно заново войти в аккаунт", isError = true)
            }
            message.contains("internal server error", ignoreCase = true) -> {
                showStatusMessage("Сервер недоступен. Попробуйте позже", isError = true)
            }
            else -> {
                showStatusMessage(
                    message.ifBlank { "Не удалось создать проект" },
                    isError = true,
                )
            }
        }
    }

    private fun showStatusMessage(message: String, isError: Boolean) {
        uiState = uiState.copy(
            statusMessage = message,
            statusMessageIsError = isError,
            statusMessagePulse = uiState.statusMessagePulse + 1,
        )
    }

    private fun deadlineDigitsToBackend(digits: String): String? {
        val raw = digits.filter { it.isDigit() }.take(8)
        if (raw.isEmpty()) return null
        if (raw.length < 8) return null

        val parser = SimpleDateFormat("ddMMyyyy", Locale.getDefault()).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val parsedDate = runCatching { parser.parse(raw) }.getOrNull() ?: return null
        return formatter.format(parsedDate)
    }

    private fun CreateProjectUiState.clearStatus(): CreateProjectUiState = copy(
        statusMessage = null,
        statusMessageIsError = false,
    )
}