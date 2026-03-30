package com.example.groupeak.ui.screens.home

data class HomeUiState(
    val userName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val hasNotifications: Boolean = true,
    val upcomingTasks: List<HomeTaskUi> = emptyList(),
    val projects: List<HomeProjectUi> = emptyList(),
)

data class HomeTaskUi(
    val title: String,
    val projectName: String,
    val deadline: String,
)

data class HomeProjectUi(
    val title: String,
    val completedCount: Int,
    val overdueCount: Int,
    val isOwner: Boolean = false,
)