package com.example.groupeak.ui.screens.project_for_owner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.domain.repository.ProjectRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

import com.example.groupeak.domain.repository.TaskRepository
import com.example.groupeak.domain.repository.EventRepository
import com.example.groupeak.data.remote.dto.TaskDto
import com.example.groupeak.data.remote.dto.ProjectMemberDto
import com.example.groupeak.data.remote.dto.EventResponse

class ProjectForOwnerViewModel(
    private val projectId: Long,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    var uiState by mutableStateOf(
        ProjectForOwnerUiState(
            projectId = projectId,
            title = "Загрузка...",
            deadlineText = "",
            membersCount = 0,
            tasksCount = 0,
            eventsCount = 0,
            isOwner = true,
            currentTasksCount = 0,
            reviewTasksCount = 0,
            inactiveTasksCount = 0,
            currentTasks = emptyList(),
            reviewTasks = emptyList(),
            inactiveTasks = emptyList(),
            members = emptyList(),
            events = emptyList(),
            description = ""
        )
    )
        private set

    init {
        loadProjectDetails()
        loadTasks()
        loadMembers()
        loadEvents()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            val result = taskRepository.getProjectTasks(projectId)
            result.onSuccess { tasks ->
                val current = tasks.filter { it.status == "in_progress" || it.status == "todo" }.map { it.toUiModel() }
                val review = tasks.filter { it.status == "in_review" }.map { it.toUiModel() }
                val inactive = tasks.filter { it.status == "done" || it.status == "cancelled" }.map { it.toUiModel() }

                uiState = uiState.copy(
                    currentTasks = current,
                    reviewTasks = review,
                    inactiveTasks = inactive,
                    currentTasksCount = current.size,
                    reviewTasksCount = review.size,
                    inactiveTasksCount = inactive.size,
                    tasksMode = ProjectTasksMode.ALL_THREE
                )
            }
        }
    }

    private fun loadMembers() {
        viewModelScope.launch {
            val result = projectRepository.getProjectMembers(projectId)
            result.onSuccess { members ->
                uiState = uiState.copy(
                    members = members.map { it.toUiModel() }
                )
            }
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            val result = eventRepository.getMyEvents()
            result.onSuccess { events ->
                val projectEvents = events.filter { it.projectId == projectId }
                uiState = uiState.copy(
                    events = projectEvents.map { it.toUiModel() },
                    eventsCount = projectEvents.size
                )
            }
        }
    }

    private fun TaskDto.toUiModel(): ProjectTaskItemUi {
        return ProjectTaskItemUi(
            id = this.id,
            title = this.title,
            priority = when (this.priority) {
                "high" -> ProjectTaskPriorityUi.HIGH
                "medium" -> ProjectTaskPriorityUi.MEDIUM
                else -> ProjectTaskPriorityUi.LOW
            },
            deadline = formatDate(this.dueAt),
            visualType = if (this.status == "done") TaskVisualType.DONE else TaskVisualType.NONE
        )
    }

    private fun ProjectMemberDto.toUiModel(): ProjectMemberItemUi {
        return ProjectMemberItemUi(
            id = this.id,
            fullName = formatShortName(this.fullName),
            position = this.position,
            isOwner = this.role == "owner"
        )
    }

    private fun formatShortName(fullName: String): String {
        val parts = fullName.trim().split("\\s+".toRegex())
        if (parts.isEmpty()) return fullName
        if (parts.size == 1) return parts[0]
        // Формат с бэкенда: "Фамилия Имя Отчество" -> берем parts[1] (Имя) и первую букву parts[0] (Фамилия)
        return "${parts[1]} ${parts[0].first()}."
    }

    private fun EventResponse.toUiModel(): ProjectEventItemUi {
        return ProjectEventItemUi(
            id = this.id,
            title = this.title,
            date = formatDate(this.startAt),
            time = formatTime(this.startAt)
        )
    }

    private fun formatDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "Без дедлайна"
        return try {
            val cleanIso = isoString.take(19)
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = parser.parse(cleanIso) ?: return isoString
            val formatter = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            isoString
        }
    }

    private fun formatTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            val cleanIso = isoString.take(19)
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = parser.parse(cleanIso) ?: return ""
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    private fun loadProjectDetails() {
        viewModelScope.launch {
            val result = projectRepository.getProjectById(projectId)
            result.onSuccess { project ->
                uiState = uiState.copy(
                    title = project.projectName,
                    description = project.description ?: "Нет описания",
                    deadlineText = formatDate(project.deadlineAt),
                    membersCount = project.memberCount,
                    tasksCount = project.taskCount
                )
            }.onFailure {
                // Обработка ошибки
            }
        }
    }

    fun updateInviteEmail(email: String) {
        uiState = uiState.copy(inviteEmail = email)
    }

    fun updateInvitePosition(position: String) {
        uiState = uiState.copy(invitePosition = position)
    }

    fun sendInvite() {
        val email = uiState.inviteEmail
        val position = uiState.invitePosition
        if (email.isBlank() || position.isBlank()) return

        viewModelScope.launch {
            val result = projectRepository.inviteMember(
                projectId, 
                com.example.groupeak.data.remote.dto.InviteMemberRequest(email)
            )
            result.onSuccess {
                android.util.Log.e("INVITE_DEBUG", "INVITE_SUCCESS: Приглашение успешно отправлено!")
                uiState = uiState.copy(
                    inviteEmail = "",
                    invitePosition = ""
                )
            }.onFailure { error ->
                android.util.Log.e("INVITE_DEBUG", "INVITE_ERROR: Ошибка при отправке приглашения: ${error.message}")
            }
        }
    }

    fun sendInviteDirectly(email: String) {
        if (email.isBlank()) return

        viewModelScope.launch {
            android.util.Log.e("INVITE_DEBUG", "sendInviteDirectly: отправляю email='$email' для projectId=$projectId")
            val result = projectRepository.inviteMember(
                projectId, 
                com.example.groupeak.data.remote.dto.InviteMemberRequest(email)
            )
            result.onSuccess {
                android.util.Log.e("INVITE_DEBUG", "INVITE_SUCCESS: Приглашение успешно отправлено!")
            }.onFailure { error ->
                android.util.Log.e("INVITE_DEBUG", "INVITE_ERROR: Ошибка при отправке приглашения: ${error.message}")
            }
        }
    }

}
