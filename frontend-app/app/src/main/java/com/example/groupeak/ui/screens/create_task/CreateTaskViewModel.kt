package com.example.groupeak.ui.screens.create_task

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.domain.repository.ProjectRepository
import com.example.groupeak.domain.repository.TaskRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class CreateTaskViewModel(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    var uiState by mutableStateOf(CreateTaskUiState())
        private set

    init {
        loadProjects()
    }

    fun onTitleChange(value: String) {
        uiState = uiState.copy(
            title = value,
            titleError = null,
        ).clearStatus()
    }

    fun onProjectSelected(projectId: Long) {
        val selectedProject = uiState.availableProjects.firstOrNull { it.id == projectId }

        uiState = uiState.copy(
            selectedProjectId = projectId,
            project = selectedProject?.name.orEmpty(),
            projectError = null,
            selectedParticipantIds = emptyList(),
            participants = "",
            participantsError = null,
            availableMembers = emptyList(),
        ).clearStatus()

        loadProjectMembers(projectId)
    }

    fun onDeadlineChange(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(8)
        uiState = uiState.copy(
            deadline = digits,
            deadlineError = null,
        ).clearStatus()
    }

    fun onToggleParticipant(userId: Long) {
        val current = uiState.selectedParticipantIds.toMutableList()

        if (current.contains(userId)) {
            current.remove(userId)
        } else {
            current.add(userId)
        }

        uiState = uiState.copy(
            selectedParticipantIds = current,
            participants = formatParticipants(current),
            participantsError = null,
        ).clearStatus()
    }

    fun onDescriptionChange(value: String) {
        uiState = uiState.copy(
            description = value,
            descriptionError = null,
        ).clearStatus()
    }

    fun onPriorityChange(priority: String) {
        uiState = uiState.copy(priority = priority).clearStatus()
    }

    fun onCreateClick(onSuccess: () -> Unit) {
        val title = uiState.title.trim()
        val description = uiState.description.trim().takeIf { it.isNotEmpty() }
        val projectId = uiState.selectedProjectId
        val dueDate = deadlineDigitsToBackend(uiState.deadline)
        val assigneeIds = uiState.selectedParticipantIds.map { it.toInt() }

        var hasError = false
        var pTitleError: String? = null
        var pProjectError: String? = null
        var pDeadlineError: String? = null
        var pParticipantsError: String? = null
        var pDescriptionError: String? = null

        if (title.isBlank()) {
            pTitleError = "error"
            hasError = true
        }

        if (projectId == null) {
            pProjectError = "error"
            hasError = true
        }

        if (uiState.deadline.isBlank() || dueDate == null) {
            pDeadlineError = "error"
            hasError = true
        }

        if (assigneeIds.isEmpty()) {
            pParticipantsError = "error"
            hasError = true
        }

        if (uiState.description.isBlank()) {
            pDescriptionError = "error"
            hasError = true
        }

        if (hasError) {
            uiState = uiState.copy(
                titleError = pTitleError,
                projectError = pProjectError,
                deadlineError = pDeadlineError,
                participantsError = pParticipantsError,
                descriptionError = pDescriptionError
            )

            val errorsCount = listOfNotNull(pTitleError, pProjectError, pDeadlineError, pParticipantsError, pDescriptionError).size
            val msg = if (errorsCount > 1) {
                "Заполните все поля"
            } else if (pTitleError != null) {
                "Введите название задачи"
            } else if (pProjectError != null) {
                "Выберите проект"
            } else if (pParticipantsError != null) {
                "Добавьте участников"
            } else if (pDescriptionError != null) {
                "Введите описание"
            } else {
                if (uiState.deadline.isBlank()) "Введите дату" else "Неверный формат даты"
            }

            showStatusMessage(msg, isError = true)
            return
        }

        if (projectId == null || dueDate == null) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            val result = taskRepository.createTask(
                projectId = projectId,
                taskName = title,
                taskDescription = description,
                dueDate = dueDate,
                priority = uiState.priority.toBackendPriority(),
                assigneeIds = assigneeIds,
            )

            uiState = uiState.copy(isLoading = false)

            result.onSuccess {
                onSuccess()
            }.onFailure { throwable ->
                handleBackendError(throwable.message.orEmpty())
            }
        }
    }

    private fun loadProjects() {
        viewModelScope.launch {
            Log.d("CreateTaskVM", "loadProjects started")

            uiState = uiState.copy(
                isLoadingProjects = true,
                statusMessage = null,
                statusMessageIsError = false,
            )

            projectRepository.getProjects()
                .onSuccess { projects ->
                    Log.d("CreateTaskVM", "projects loaded: count=${projects.size}, projects=$projects")

                    uiState = uiState.copy(
                        isLoadingProjects = false,
                        availableProjects = projects.map {
                            CreateTaskProjectOption(
                                id = it.id,
                                name = it.projectName,
                            )
                        },
                        statusMessage = null,
                        statusMessageIsError = false,
                    )
                }
                .onFailure { throwable ->
                    Log.e("CreateTaskVM", "loadProjects failed", throwable)

                    uiState = uiState.copy(
                        isLoadingProjects = false,
                        availableProjects = emptyList(),
                    )

                    showStatusMessage(
                        throwable.message ?: "Не удалось загрузить проекты",
                        isError = true,
                    )
                }
        }
    }

    private fun loadProjectMembers(projectId: Long) {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoadingMembers = true,
                statusMessage = null,
                statusMessageIsError = false,
            )

            projectRepository.getProjectMembers(projectId)
                .onSuccess { members ->
                    Log.d("CreateTaskVM", "members loaded: count=${members.size}, members=$members")

                    uiState = uiState.copy(
                        isLoadingMembers = false,
                        availableMembers = members.map {
                            CreateTaskMemberOption(
                                userId = it.userId,
                                fullName = formatDisplayName(it.fullName),
                                position = it.position,
                            )
                        },
                        statusMessage = null,
                        statusMessageIsError = false,
                    )
                }
                .onFailure { throwable ->
                    Log.e("CreateTaskVM", "loadProjectMembers failed", throwable)

                    uiState = uiState.copy(
                        isLoadingMembers = false,
                        availableMembers = emptyList(),
                    )

                    showStatusMessage(
                        throwable.message ?: "Не удалось загрузить участников",
                        isError = true,
                    )
                }
        }
    }

    private fun formatParticipants(selectedIds: List<Long>): String {
        val names = uiState.availableMembers
            .filter { selectedIds.contains(it.userId) }
            .map { it.fullName }

        return names.joinToString(", ")
    }

    private fun handleBackendError(message: String) {
        when {
            message.contains("task title is required", ignoreCase = true) -> {
                uiState = uiState.copy(titleError = "error")
                showStatusMessage("Введите название задачи", isError = true)
            }

            message.contains("invalid task priority", ignoreCase = true) -> {
                showStatusMessage("Неверный приоритет", isError = true)
            }

            message.contains("invalid assignee_ids", ignoreCase = true) -> {
                uiState = uiState.copy(participantsError = "error")
                showStatusMessage("Некорректные участники", isError = true)
            }

            message.contains("one or more assignees do not exist", ignoreCase = true) -> {
                uiState = uiState.copy(participantsError = "error")
                showStatusMessage("Некоторые участники не найдены", isError = true)
            }

            message.contains("one or more assignees have no access to this project", ignoreCase = true) -> {
                uiState = uiState.copy(participantsError = "error")
                showStatusMessage("Участники не состоят в проекте", isError = true)
            }

            message.contains("due date cannot be in the past", ignoreCase = true) -> {
                uiState = uiState.copy(deadlineError = "error")
                showStatusMessage("Дедлайн не может быть в прошлом", isError = true)
            }

            message.contains("user is not project owner", ignoreCase = true) -> {
                showStatusMessage("Создавать задачи может только владелец проекта", isError = true)
            }

            message.contains("unauthorized", ignoreCase = true) -> {
                showStatusMessage("Нужно заново войти в аккаунт", isError = true)
            }

            message.contains("text is too long", ignoreCase = true) -> {
                uiState = uiState.copy(descriptionError = "error")
                showStatusMessage("Описание слишком длинное", isError = true)
            }

            message.contains("internal server error", ignoreCase = true) -> {
                showStatusMessage("Сервер недоступен. Попробуйте позже", isError = true)
            }

            else -> {
                showStatusMessage(
                    message.ifBlank { "Не удалось создать задачу" },
                    isError = true,
                )
            }
        }
    }

    private fun showStatusMessage(message: String, isError: Boolean) {
        uiState = uiState.copy(
            statusMessage = message,
            statusMessageIsError = isError,
            statusMessagePulse = uiState.statusMessagePulse + 1,
        )
    }

    private fun deadlineDigitsToBackend(digits: String): String? {
        val raw = digits.filter { it.isDigit() }.take(8)
        if (raw.length < 8) return null

        val parser = SimpleDateFormat("ddMMyyyy", Locale.getDefault()).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val parsedDate = runCatching { parser.parse(raw) }.getOrNull() ?: return null
        return formatter.format(parsedDate)
    }

    private fun String.toBackendPriority(): String {
        return when (this) {
            "Низкий" -> "low"
            "Высокий" -> "high"
            else -> "medium"
        }
    }

    private fun CreateTaskUiState.clearStatus(): CreateTaskUiState = copy(
        statusMessage = null,
        statusMessageIsError = false,
    )
}

private fun formatDisplayName(fullName: String): String {
    val parts = fullName.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    if (parts.isEmpty()) return ""
    if (parts.size == 1) return parts[0]

    val noPatronymic = parts.filterNot { isPatronymic(it) }

    val core = when {
        noPatronymic.size >= 2 -> noPatronymic
        else -> parts
    }

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

    val ru = x.endsWith("вич") || x.endsWith("вна") ||
            x.endsWith("ович") || x.endsWith("овна") ||
            x.endsWith("евич") || x.endsWith("евна") ||
            x.endsWith("ич") || x.endsWith("кызы") || x.endsWith("оглы")

    val en = x.endsWith("vich") || x.endsWith("vna") ||
            x.endsWith("ovich") || x.endsWith("ovna") ||
            x.endsWith("evich") || x.endsWith("evna") ||
            x.endsWith("ich") || x.endsWith("kyzy") ||
            x.endsWith("ogly") || x.endsWith("oglu")

    return ru || en
}

private fun looksLikeSurname(value: String): Boolean {
    val x = value.trim().lowercase()

    val ruEndings = listOf(
        "ов", "ев", "ин", "ын",
        "ова", "ева", "ина", "ына",
        "ский", "цкий", "ской",
        "ская", "цкая",
        "енко", "ко", "ук", "юк", "як"
    )

    val enEndings = listOf(
        "ov", "ev", "in",
        "ova", "eva", "ina",
        "sky", "ski", "skiy", "skyy", "skii",
        "enko", "ko", "uk", "yuk", "yak"
    )

    return ruEndings.any { x.endsWith(it) } || enEndings.any { x.endsWith(it) }
}