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

enum class ProjectTasksMode {
    DEFAULT,
    FILTERED_CURRENT,
    REVIEW_INACTIVE,
    NO_REVIEW_INACTIVE,
    EMPTY,
    NO_CURRENT,
    ALL_THREE
}

data class ProjectTaskItemUi(
    val id: Long,
    val title: String,
    val priority: ProjectTaskPriorityUi,
    val deadline: String,
    val visualType: ProjectTaskVisualType = ProjectTaskVisualType.NONE,
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

    val currentTasksCount: Int,
    val reviewTasksCount: Int,
    val inactiveTasksCount: Int,

    val currentTasks: List<ProjectTaskItemUi>,
    val reviewTasks: List<ProjectTaskItemUi>,
    val inactiveTasks: List<ProjectTaskItemUi> = emptyList(),

    val members: List<ProjectMemberItemUi>,
    val description: String,
    val tasksMode: ProjectTasksMode = ProjectTasksMode.DEFAULT,
)

fun mockProjectUiState(
    projectId: Long,
    isOwner: Boolean,
    tasksMode: ProjectTasksMode = ProjectTasksMode.ALL_THREE,
    selectedTab: ProjectTab = ProjectTab.TASKS,
): ProjectUiState {
    val allCurrentTasks = listOf(
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
        ProjectTaskItemUi(
            id = 10,
            title = "Собрать обратную связь от пользователей",
            priority = ProjectTaskPriorityUi.MEDIUM,
            deadline = "03.03.26",
        ),
        ProjectTaskItemUi(
            id = 11,
            title = "Обновить документацию проекта",
            priority = ProjectTaskPriorityUi.LOW,
            deadline = "06.03.26",
        ),
    )

    val allReviewTasks = listOf(
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
        ProjectTaskItemUi(
            id = 12,
            title = "Проверить адаптивность интерфейса",
            priority = ProjectTaskPriorityUi.MEDIUM,
            deadline = "29.03.26",
        ),
    )

    val allInactiveTasks = listOf(
        ProjectTaskItemUi(
            id = 7,
            title = "Сверить бухгалтерию за 4-ый квартал",
            priority = ProjectTaskPriorityUi.LOW,
            deadline = "08.03.26",
            visualType = ProjectTaskVisualType.DONE,
        ),
        ProjectTaskItemUi(
            id = 8,
            title = "Проанализировать целевую аудиторию",
            priority = ProjectTaskPriorityUi.LOW,
            deadline = "25.04.26",
            visualType = ProjectTaskVisualType.ALARM_OFF,
        ),
        ProjectTaskItemUi(
            id = 9,
            title = "Проверить доступы в системе",
            priority = ProjectTaskPriorityUi.LOW,
            deadline = "25.04.26",
            visualType = ProjectTaskVisualType.DONE,
        ),
        ProjectTaskItemUi(
            id = 13,
            title = "Архивировать старые макеты",
            priority = ProjectTaskPriorityUi.MEDIUM,
            deadline = "30.04.26",
            visualType = ProjectTaskVisualType.ALARM_OFF,
        ),
        ProjectTaskItemUi(
            id = 14,
            title = "Закрыть тестовый релиз",
            priority = ProjectTaskPriorityUi.HIGH,
            deadline = "05.05.26",
            visualType = ProjectTaskVisualType.DONE,
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
        ProjectMemberItemUi(
            id = 106,
            fullName = "Юрий Х.",
            position = "Full-stack",
        ),
        ProjectMemberItemUi(
            id = 107,
            fullName = "Юрий Х.",
            position = "Full-stack",
        ),
        ProjectMemberItemUi(
            id = 108,
            fullName = "Юрий Х.",
            position = "Full-stack",
        ),
    )

    val currentTasksCount: Int
    val reviewTasksCount: Int
    val inactiveTasksCount: Int

    val currentTasks: List<ProjectTaskItemUi>
    val reviewTasks: List<ProjectTaskItemUi>
    val inactiveTasks: List<ProjectTaskItemUi>

    when (tasksMode) {
        ProjectTasksMode.DEFAULT -> {
            currentTasks = allCurrentTasks
            reviewTasks = allReviewTasks.take(3)
            inactiveTasks = emptyList()

            currentTasksCount = currentTasks.size
            reviewTasksCount = reviewTasks.size
            inactiveTasksCount = inactiveTasks.size
        }

        ProjectTasksMode.FILTERED_CURRENT -> {
            currentTasks = listOf(
                allCurrentTasks[0].copy(priority = ProjectTaskPriorityUi.HIGH),
                allCurrentTasks[1].copy(priority = ProjectTaskPriorityUi.HIGH),
                allCurrentTasks[2].copy(priority = ProjectTaskPriorityUi.HIGH),
                allCurrentTasks[3].copy(priority = ProjectTaskPriorityUi.HIGH),
            )
            reviewTasks = emptyList()
            inactiveTasks = emptyList()

            currentTasksCount = currentTasks.size
            reviewTasksCount = reviewTasks.size
            inactiveTasksCount = inactiveTasks.size
        }

        ProjectTasksMode.REVIEW_INACTIVE -> {
            currentTasks = emptyList()
            reviewTasks = allReviewTasks
            inactiveTasks = allInactiveTasks

            currentTasksCount = currentTasks.size
            reviewTasksCount = reviewTasks.size
            inactiveTasksCount = inactiveTasks.size
        }

        ProjectTasksMode.NO_REVIEW_INACTIVE -> {
            currentTasks = emptyList()
            reviewTasks = emptyList()
            inactiveTasks = allInactiveTasks

            currentTasksCount = currentTasks.size
            reviewTasksCount = reviewTasks.size
            inactiveTasksCount = inactiveTasks.size
        }

        ProjectTasksMode.EMPTY -> {
            currentTasks = emptyList()
            reviewTasks = emptyList()
            inactiveTasks = emptyList()

            currentTasksCount = 0
            reviewTasksCount = 0
            inactiveTasksCount = 0
        }

        ProjectTasksMode.NO_CURRENT -> {
            currentTasks = emptyList()
            reviewTasks = allReviewTasks
            inactiveTasks = emptyList()

            currentTasksCount = 0
            reviewTasksCount = reviewTasks.size
            inactiveTasksCount = inactiveTasks.size
        }

        ProjectTasksMode.ALL_THREE -> {
            currentTasks = allCurrentTasks
            reviewTasks = allReviewTasks
            inactiveTasks = allInactiveTasks

            currentTasksCount = currentTasks.size
            reviewTasksCount = reviewTasks.size
            inactiveTasksCount = inactiveTasks.size
            //currentTasks = allCurrentTasks
            //reviewTasks = emptyList()
           // inactiveTasks = emptyList()

            //currentTasksCount = 5
            //reviewTasksCount = 0
            //inactiveTasksCount = 0
        }
    }

    return ProjectUiState(
        projectId = projectId,
        title = if (projectId == 2L) "Open Space" else "Программа для Tahoe",
        deadlineText = "27.09.26",
        membersCount = members.size,
        tasksCount = currentTasksCount + reviewTasksCount + inactiveTasksCount,
        isOwner = isOwner,
        selectedTab = selectedTab,

        currentTasksCount = currentTasksCount,
        reviewTasksCount = reviewTasksCount,
        inactiveTasksCount = inactiveTasksCount,

        currentTasks = currentTasks,
        reviewTasks = reviewTasks,
        inactiveTasks = inactiveTasks,

        members = members,
        description = "Описание проекта для проверки экрана. Здесь можно написать любой текст проекта, чтобы проверить диалог описания.",
        tasksMode = tasksMode,
    )
}