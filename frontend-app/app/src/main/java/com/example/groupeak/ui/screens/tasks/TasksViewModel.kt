package com.example.groupeak.ui.screens.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.groupeak.data.remote.dto.ProjectDto
import com.example.groupeak.data.remote.dto.ProjectMemberDto
import com.example.groupeak.domain.repository.ProjectRepository
import com.example.groupeak.domain.repository.TaskRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TasksViewModel(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    initialProjectId: Long? = null,
) : ViewModel() {

    var uiState by mutableStateOf(
        TasksUiState(
            isLoading = true,
            isProjectScoped = initialProjectId != null,
            scopedProjectId = initialProjectId,
            selectedProjectId = initialProjectId,
        )
    )
        private set

    private var projectMembersCache: Map<Long, List<ProjectMemberDto>> = emptyMap()

    init {
        loadProjectsAndTasks()
    }

    fun onTabSelected(tab: TasksTab) {
        uiState = uiState.copy(selectedTab = tab)
    }

    fun applyFilters(
        projectId: Long?,
        priority: TaskPriorityFilter?,
    ) {
        uiState = uiState.copy(
            selectedProjectId = projectId,
            selectedPriority = priority,
            statusMessage = null,
            statusMessageIsError = false,
        )
        loadTasks()
    }

    fun resetFilters() {
        uiState = uiState.copy(
            selectedProjectId = uiState.scopedProjectId,
            selectedPriority = null,
            statusMessage = null,
            statusMessageIsError = false,
        )
        loadTasks()
    }

    fun openTask(projectId: Long, taskId: Long) {
        uiState = uiState.copy(
            isTaskDetailsVisible = true,
            isLoadingTaskDetails = true,
            selectedTaskDetails = null,
            statusMessage = null,
            statusMessageIsError = false,
        )

        viewModelScope.launch {
            val taskResult = taskRepository.getTaskById(projectId, taskId)

            taskResult
                .onSuccess { task ->
                    val members = getProjectMembers(projectId)
                    val projectOwnerId = uiState.projects
                        .firstOrNull { it.id == projectId }
                        ?.ownerId

                    val participants = task.assigneeIds
                        .map { assigneeId ->
                            val member = members.firstOrNull { it.userId == assigneeId }
                            val isAuthor = member?.userId == projectOwnerId
                            val position = member?.position?.takeIf { it.isNotBlank() }

                            TaskParticipantDetailsUi(
                                name = member?.let { formatDisplayName(it.fullName) }
                                    ?: "Участник #$assigneeId",
                                role = when {
                                    isAuthor -> "Автор задачи"
                                    position != null -> position
                                    member != null -> member.role.toHumanReadableRole()
                                    else -> "Участник"
                                },
                            )
                        }
                        .sortedWith(
                            compareByDescending<TaskParticipantDetailsUi> { it.role == "Автор задачи" }
                                .thenBy { it.name }
                        )

                    uiState = uiState.copy(
                        isLoadingTaskDetails = false,
                        selectedTaskDetails = TaskDetailsUi(
                            task = task,
                            projectName = uiState.projectNamesById[projectId] ?: "Проект #$projectId",
                            participants = participants,
                        ),
                    )
                }
                .onFailure { throwable ->
                    uiState = uiState.copy(
                        isLoadingTaskDetails = false,
                        isTaskDetailsVisible = false,
                    )
                    showStatusMessage(
                        throwable.message ?: "Не удалось загрузить задачу",
                        isError = true,
                    )
                }
        }
    }

    fun closeTaskDetails() {
        uiState = uiState.copy(
            isTaskDetailsVisible = false,
            isLoadingTaskDetails = false,
            selectedTaskDetails = null,
        )
    }

    fun submitTaskForReview(projectId: Long, taskId: Long) {
        if (uiState.isSubmittingTaskId != null) return

        uiState = uiState.copy(
            isSubmittingTaskId = taskId,
            statusMessage = null,
            statusMessageIsError = false,
        )

        viewModelScope.launch {
            val result = taskRepository.submitForReview(projectId, taskId)

            result
                .onSuccess {
                    // Даем локальной exit-анимации в TaskScreen завершиться,
                    // чтобы быстрый ответ бэка не "съедал" перелет задачи в блок
                    // "На проверке".
                    delay(300)

                    uiState = uiState.copy(
                        isSubmittingTaskId = null,
                    )

                    closeTaskDetails()
                    showStatusMessage("Задача отправлена на проверку", isError = false)
                    loadTasks()
                }
                .onFailure { throwable ->
                    uiState = uiState.copy(
                        isSubmittingTaskId = null,
                    )

                    showStatusMessage(
                        throwable.message ?: "Не удалось отправить задачу на проверку",
                        isError = true,
                    )
                }
        }
    }

    fun refreshTasks(clearStatus: Boolean = false) {
        if (clearStatus) {
            uiState = uiState.copy(
                statusMessage = null,
                statusMessageIsError = false,
            )
        }
        loadTasks()
    }

    private fun loadProjectsAndTasks() {
        viewModelScope.launch {
            projectRepository.getProjects()
                .onSuccess { projects ->
                    applyProjects(projects)
                }
                .onFailure {
                    applyProjects(emptyList())
                }

            loadTasks()
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            val result = if (uiState.scopedProjectId != null) {
                taskRepository.getProjectTasks(projectId = uiState.scopedProjectId!!)
            } else {
                taskRepository.getTasks(
                    projectId = uiState.selectedProjectId,
                    priority = uiState.selectedPriority?.backendValue,
                )
            }

            result
                .onSuccess { tasks ->
                    val filteredTasks = if (uiState.scopedProjectId != null && uiState.selectedPriority != null) {
                        tasks.filter { it.priority == uiState.selectedPriority?.backendValue }
                    } else {
                        tasks
                    }

                    uiState = uiState.copy(
                        isLoading = false,
                        tasks = filteredTasks,
                    )
                }
                .onFailure { throwable ->
                    uiState = uiState.copy(
                        isLoading = false,
                        tasks = emptyList(),
                    )
                    showStatusMessage(
                        throwable.message ?: "Не удалось загрузить задачи",
                        isError = true,
                    )
                }
        }
    }

    private fun applyProjects(projects: List<ProjectDto>) {
        val projectNames = projects.associate { it.id to it.projectName }

        uiState = uiState.copy(
            projects = projects,
            projectNamesById = projectNames,
            scopedProjectName = uiState.scopedProjectId?.let { projectNames[it] },
        )
    }

    private suspend fun getProjectMembers(projectId: Long): List<ProjectMemberDto> {
        projectMembersCache[projectId]?.let { return it }

        return projectRepository.getProjectMembers(projectId)
            .getOrElse { emptyList() }
            .also { members ->
                projectMembersCache = projectMembersCache + (projectId to members)
            }
    }

    private fun showStatusMessage(message: String, isError: Boolean) {
        uiState = uiState.copy(
            statusMessage = message,
            statusMessageIsError = isError,
        )
    }
}

class TasksViewModelFactory(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val initialProjectId: Long? = null,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(
                taskRepository = taskRepository,
                projectRepository = projectRepository,
                initialProjectId = initialProjectId,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}

private fun formatDisplayName(fullName: String): String {
    val parts = fullName.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    if (parts.isEmpty()) return fullName
    if (parts.size == 1) return parts[0]

    val noPatronymic = parts.filterNot { isPatronymic(it) }
    val core = if (noPatronymic.size >= 2) noPatronymic else parts
    val first = core[0]
    val second = core[1]

    return when {
        looksLikeSurname(first) && !looksLikeSurname(second) -> "$second $first"
        !looksLikeSurname(first) && looksLikeSurname(second) -> "$first $second"
        else -> "$second $first"
    }
}

private fun isPatronymic(value: String): Boolean {
    val x = value.trim().lowercase()
    return x.endsWith("вич") || x.endsWith("вна") ||
            x.endsWith("ович") || x.endsWith("овна") ||
            x.endsWith("евич") || x.endsWith("евна") ||
            x.endsWith("ич") || x.endsWith("kyzy") ||
            x.endsWith("ogly") || x.endsWith("oglu")
}

private fun looksLikeSurname(value: String): Boolean {
    val x = value.trim().lowercase()
    val endings = listOf(
        "ов", "ев", "ин", "ын",
        "ский", "цкий", "ова", "ева", "ина",
        "енко", "ко", "ук", "юк", "ич",
        "ov", "ev", "in", "sky", "ski", "enko", "ko", "uk",
    )
    return endings.any { x.endsWith(it) }
}

private fun String.toHumanReadableRole(): String {
    return when (lowercase()) {
        "owner" -> "Автор задачи"
        "member" -> "Участник"
        else -> replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}