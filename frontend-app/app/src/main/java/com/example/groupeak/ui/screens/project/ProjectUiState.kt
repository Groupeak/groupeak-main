package com.example.groupeak.ui.screens.project

enum class ProjectTab {
    TASKS,
    MEMBERS,
}

enum class ProjectTaskPriorityUi(val title: String) {
    HIGH("Высокий приоритет"),
    MEDIUM("Средний приоритет"),
    LOW("Низкий приоритет"),
}

data class ProjectTaskItemUi(
    val id: Long,
    val title: String,
    val priority: ProjectTaskPriorityUi,
    val deadline: String,
)

data class ProjectMemberItemUi(
    val id: Long,
    val fullName: String,
    val position: String,
    val isOwner: Boolean = false,
)

data class ProjectUiState(
    val projectId: Long,
    val title: String,
    val deadlineText: String,
    val membersCount: Int,
    val tasksCount: Int,
    val isOwner: Boolean,
    val selectedTab: ProjectTab = ProjectTab.TASKS,
    val currentTasks: List<ProjectTaskItemUi> = emptyList(),
    val reviewTasks: List<ProjectTaskItemUi> = emptyList(),
    val members: List<ProjectMemberItemUi> = emptyList(),
)

fun mockProjectUiState(
    projectId: Long,
    isOwner: Boolean,
): ProjectUiState {
    val currentTasks = listOf(
        ProjectTaskItemUi(
            id = 1,
            title = "Подготовить отчет по работе",
            priority = ProjectTaskPriorityUi.HIGH,
            deadline = "22.02.26",
        ),
        ProjectTaskItemUi(
            id = 2,
            title = "Проанализировать целевую аудиторию",
            priority = ProjectTaskPriorityUi.MEDIUM,
            deadline = "25.02.26",
        ),
        ProjectTaskItemUi(
            id = 3,
            title = "Проверить код на корректность по тестам",
            priority = ProjectTaskPriorityUi.LOW,
            deadline = "28.02.26",
        ),
    )

    val reviewTasks = listOf(
        ProjectTaskItemUi(
            id = 4,
            title = "Сверить бухгалтерию за 4-ый квартал",
            priority = ProjectTaskPriorityUi.LOW,
            deadline = "08.03.26",
        ),
        ProjectTaskItemUi(
            id = 5,
            title = "Проверить код на корректность по тестам",
            priority = ProjectTaskPriorityUi.LOW,
            deadline = "15.03.26",
        ),
        ProjectTaskItemUi(
            id = 6,
            title = "Проанализировать целевую аудиторию",
            priority = ProjectTaskPriorityUi.LOW,
            deadline = "26.03.26",
        ),
    )

    val members = listOf(
        ProjectMemberItemUi(
            id = 101,
            fullName = "Михаил Д.",
            position = "Frontend",
            isOwner = true,
        ),
        ProjectMemberItemUi(
            id = 102,
            fullName = "Василий П.",
            position = "Backend",
        ),
        ProjectMemberItemUi(
            id = 103,
            fullName = "Андрей Р.",
            position = "Designer",
        ),
        ProjectMemberItemUi(
            id = 104,
            fullName = "Алексей В.",
            position = "PR",
        ),
        ProjectMemberItemUi(
            id = 105,
            fullName = "Юрий Х.",
            position = "Full-stack",
        ),
    )

    return ProjectUiState(
        projectId = projectId,
        title = if (projectId == 2L) "Open Space" else "Программа для Tahoe",
        deadlineText = "27.09.26",
        membersCount = members.size,
        tasksCount = currentTasks.size + reviewTasks.size,
        isOwner = isOwner,
        currentTasks = currentTasks,
        reviewTasks = reviewTasks,
        members = members,
    )
}