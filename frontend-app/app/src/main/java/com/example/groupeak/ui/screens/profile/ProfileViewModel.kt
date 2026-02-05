package com.example.groupeak.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    // Снимок состояния до входа в режим редактирования (чтобы можно было отменить изменения)
    private var backupBeforeEdit: ProfileUiState? = null

    fun onEditClick() {
        if (uiState.isEditing) return
        backupBeforeEdit = uiState
        uiState = uiState.copy(isEditing = true)
    }

    fun onSaveClick() {
        // TODO: здесь сохранение на бэк
        backupBeforeEdit = null
        uiState = uiState.copy(isEditing = false)
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
        uiState = uiState.copy(position = value)
    }

    fun onAboutChange(value: String) {
        uiState = uiState.copy(about = value)
    }
}
