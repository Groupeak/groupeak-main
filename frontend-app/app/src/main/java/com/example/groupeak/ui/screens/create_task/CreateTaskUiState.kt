package com.example.groupeak.ui.screens.create_task

data class CreateTaskUiState(
    val title: String = "",
    val project: String = "",
    val deadline: String = "", // DDMMYYYY
    val participants: String = "",
    val description: String = "",
    val priority: String = "Средний",

    // Ошибки
    val titleError: String? = null,
    val projectError: String? = null,
    val deadlineError: String? = null,
    val participantsError: String? = null,
    val descriptionError: String? = null
)