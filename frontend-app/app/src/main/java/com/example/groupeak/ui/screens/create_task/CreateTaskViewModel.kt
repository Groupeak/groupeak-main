package com.example.groupeak.ui.screens.create_task

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CreateTaskViewModel : ViewModel() {

    var uiState by mutableStateOf(CreateTaskUiState())
        private set

    fun onTitleChange(value: String) {
        uiState = uiState.copy(title = value, titleError = null)
    }

    fun onProjectChange(value: String) {
        uiState = uiState.copy(project = value, projectError = null)
    }

    fun onDeadlineChange(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(8)
        uiState = uiState.copy(deadline = digits, deadlineError = null)
    }

    fun onParticipantsChange(value: String) {
        uiState = uiState.copy(participants = value, participantsError = null)
    }

    fun onDescriptionChange(value: String) {
        uiState = uiState.copy(description = value, descriptionError = null)
    }

    fun onPriorityChange(priority: String) {
        uiState = uiState.copy(priority = priority)
    }

    fun onCreateClick(onSuccess: () -> Unit) {
        var hasError = false

        if (uiState.title.isBlank()) {
            uiState = uiState.copy(titleError = "Введите название задачи")
            hasError = true
        }
        if (uiState.project.isBlank()) {
            uiState = uiState.copy(projectError = "Выберите проект")
            hasError = true
        }
        if (uiState.deadline.length < 8) {
            uiState = uiState.copy(deadlineError = "Неверный формат даты")
            hasError = true
        }
        if (uiState.participants.isBlank()) {
            uiState = uiState.copy(participantsError = "Добавьте участников")
            hasError = true
        }
        if (uiState.description.isBlank()) {
            uiState = uiState.copy(descriptionError = "Введите описание")
            hasError = true
        }

        if (!hasError) {
            onSuccess()
        }
    }
}