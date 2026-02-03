package com.example.groupeak.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    fun onEditClick() {
        if (uiState.isEditing) return
        uiState = uiState.copy(isEditing = true)
    }

    fun onSaveClick() {
        // TODO: здесь сохранение на бэк
        uiState = uiState.copy(isEditing = false)
    }

    fun onFullNameChange(value: String) {
        uiState = uiState.copy(fullName = value)
    }

    fun onBirthDateChange(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(8) // DDMMYYYY
        uiState = uiState.copy(birthDate = digits)
    }

    fun onPositionChange(value: String) {
        uiState = uiState.copy(position = value)
    }

    fun onAboutChange(value: String) {
        uiState = uiState.copy(about = value)
    }
}
