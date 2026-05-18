package com.example.groupeak.ui.screens.tasks

import com.example.groupeak.data.remote.dto.ProjectDto
import com.example.groupeak.data.remote.dto.TaskDto

enum class TasksTab {
    ACTIVE,
    INACTIVE,
}

enum class TaskPriorityFilter(val backendValue: String, val title: String) {
    HIGH("high", "Высокий"),
    MEDIUM("medium", "Средний"),
    LOW("low", "Низкий");

    companion object {
        fun fromBackendValue(value: String?): TaskPriorityFilter? {
            return values().firstOrNull { it.backendValue == value }
        }
    }
}

data class TaskParticipantDetailsUi(
    val name: String,
    val role: String,
)

data class TaskDetailsUi(
    val task: TaskDto,
    val projectName: String,
    val participants: List<TaskParticipantDetailsUi>,
)

data class TasksUiState(
    val isLoading: Boolean = false,
    val selectedTab: TasksTab = TasksTab.ACTIVE,
    val selectedProjectId: Long? = null,
    val selectedPriority: TaskPriorityFilter? = null,
    val tasks: List<TaskDto> = emptyList(),
    val projects: List<ProjectDto> = emptyList(),
    val projectNamesById: Map<Long, String> = emptyMap(),
    val isProjectScoped: Boolean = false,
    val scopedProjectId: Long? = null,
    val scopedProjectName: String? = null,
    val isTaskDetailsVisible: Boolean = false,
    val isLoadingTaskDetails: Boolean = false,
    val selectedTaskDetails: TaskDetailsUi? = null,
    val isSubmittingTaskId: Long? = null,
    val taskToDeleteId: Long? = null,
    val isDeletingTaskId: Long? = null,
    val statusMessage: String? = null,
    val statusMessageIsError: Boolean = false,
)