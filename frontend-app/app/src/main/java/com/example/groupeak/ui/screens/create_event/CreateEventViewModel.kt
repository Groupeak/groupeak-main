package com.example.groupeak.ui.screens.create_event

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CreateEventViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState = _uiState.asStateFlow()

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, titleError = null) }
    }

    fun onProjectChange(value: String) {
        _uiState.update { it.copy(project = value) }
    }

    fun onDateChange(value: String) {
        _uiState.update { it.copy(date = value, dateError = null) }
    }

    fun onTimeChange(value: String) {
        _uiState.update { it.copy(time = value, timeError = null) }
    }

    fun onParticipantsChange(value: String) {
        _uiState.update { it.copy(participants = value) }
    }

    fun onLinkChange(value: String) {
        _uiState.update { it.copy(link = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onCreateClick(onSuccess: () -> Unit) {
        if (validate()) {
            onSuccess()
        }
    }

    private fun validate(): Boolean {
        var isValid = true
        val currentState = _uiState.value

        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Введите название мероприятия") }
            isValid = false
        }

        if (currentState.date.isBlank()) {
            _uiState.update { it.copy(dateError = "Введите дату") }
            isValid = false
        }

        if (currentState.time.isBlank()) {
            _uiState.update { it.copy(timeError = "Введите время") }
            isValid = false
        }

        return isValid
    }
}