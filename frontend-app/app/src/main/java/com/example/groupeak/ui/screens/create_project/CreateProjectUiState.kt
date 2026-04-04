package com.example.groupeak.ui.screens.create_project

data class CreateProjectUiState(
    val projectName: String = "",
    val deadline: String = "", // DDMMYYYY
    val description: String = "",
    val isLoading: Boolean = false,
    val projectNameError: String? = null,
    val deadlineError: String? = null,
    val statusMessage: String? = null,
    val statusMessageIsError: Boolean = false,
    val statusMessagePulse: Int = 0,
)