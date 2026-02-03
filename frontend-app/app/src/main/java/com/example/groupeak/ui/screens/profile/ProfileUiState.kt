package com.example.groupeak.ui.screens.profile

data class ProfileUiState(
    val fullName: String = "Иванов Иван Иванович",
    val birthDate: String = "",
    val position: String = "",
    val about: String = "",
    val hasNotifications: Boolean = true,
    val isEditing: Boolean = false
)
