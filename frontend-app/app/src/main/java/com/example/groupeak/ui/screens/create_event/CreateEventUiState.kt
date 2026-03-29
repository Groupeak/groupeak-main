package com.example.groupeak.ui.screens.create_event

data class CreateEventUiState(
    val title: String = "",
    val project: String = "",
    val date: String = "",
    val time: String = "",
    val participants: String = "",
    val link: String = "",
    val description: String = "",
    val titleError: String? = null,
    val dateError: String? = null,
    val timeError: String? = null
)