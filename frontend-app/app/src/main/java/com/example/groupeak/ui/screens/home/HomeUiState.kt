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
    val id: Long,
    val projectId: Long,
    val title: String,
    val projectName: String,
    val deadline: String,
)

data class HomeProjectUi(
    val id: Long,
    val title: String,
    val completedCount: Int,
    val overdueCount: Int,
    val isOwner: Boolean = false,
)

fun mockHomeUiState(): HomeUiState {
    return HomeUiState(
        userName = "Михаил",
        email = "mikhail@example.com",
        avatarUrl = null,
        hasNotifications = true,
        upcomingTasks = listOf(
            HomeTaskUi(
                id = 1L,
                projectId = 1L,
                title = "Подготовить отчет по работе",
                projectName = "Программа для Tahoe",
                deadline = "22.02.26",
            ),
            HomeTaskUi(
                id = 2L,
                projectId = 2L,
                title = "Проанализировать целевую аудиторию",
                projectName = "Open Space",
                deadline = "25.02.26",
            ),
            HomeTaskUi(
                id = 3L,
                projectId = 1L,
                title = "Проверить код на корректность по тестам",
                projectName = "Программа для Tahoe",
                deadline = "28.02.26",
            ),
        ),
        projects = listOf(
            HomeProjectUi(
                id = 1L,
                title = "Программа для Tahoe",
                completedCount = 8,
                overdueCount = 2,
                isOwner = true,
            ),
            HomeProjectUi(
                id = 2L,
                title = "Open Space",
                completedCount = 5,
                overdueCount = 1,
                isOwner = false,
            ),
            HomeProjectUi(
                id = 3L,
                title = "Marketplace",
                completedCount = 11,
                overdueCount = 0,
                isOwner = false,
            ),
        ),
    )
}