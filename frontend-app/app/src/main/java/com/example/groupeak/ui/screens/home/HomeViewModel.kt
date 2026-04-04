package com.example.groupeak.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.data.local.SessionManager
import com.example.groupeak.data.remote.dto.ProjectDto
import com.example.groupeak.data.remote.dto.TaskDto
import com.example.groupeak.domain.repository.ProjectRepository
import com.example.groupeak.domain.repository.TaskRepository
import com.example.groupeak.domain.repository.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HomeViewModel(
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    var uiState by mutableStateOf(
        HomeUiState(
            upcomingTasks = emptyList(),
            projects = emptyList(),
        )
    )
        private set

    private var currentUserId: Long? = null
    private var lastObservedUserId: Long? = null

    private var projectsCache: List<ProjectDto> = emptyList()
    private var myTasksCache: List<TaskDto> = emptyList()
    private var nearestTasksCache: List<TaskDto> = emptyList()

    init {
        viewModelScope.launch {
            sessionManager.user.collectLatest { user ->
                currentUserId = user?.id

                if (user == null) {
                    lastObservedUserId = null
                    projectsCache = emptyList()
                    myTasksCache = emptyList()
                    nearestTasksCache = emptyList()

                    uiState = HomeUiState()
                    return@collectLatest
                }

                uiState = uiState.copy(
                    userName = formatNameSurname(user.fullName),
                    email = user.email,
                    avatarUrl = user.avatarUrl,
                )

                if (user.id != lastObservedUserId) {
                    lastObservedUserId = user.id
                    refreshHomeContent()
                }
            }
        }

        if (sessionManager.user.value == null) {
            refreshProfile()
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            userRepository.getProfile()
        }
    }

    fun refreshHomeContent() {
        refreshProjects()
        refreshMyTasks()
        refreshNearestTasks()
    }

    fun refreshProjects() {
        viewModelScope.launch {
            projectRepository.getProjects()
                .onSuccess { projects ->
                    projectsCache = projects
                    rebuildHomeState()
                }
                .onFailure {
                    projectsCache = emptyList()
                    rebuildHomeState()
                }
        }
    }

    fun refreshMyTasks() {
        viewModelScope.launch {
            taskRepository.getMyTasks()
                .onSuccess { tasks ->
                    myTasksCache = tasks
                    rebuildHomeState()
                }
                .onFailure {
                    myTasksCache = emptyList()
                    rebuildHomeState()
                }
        }
    }

    fun refreshNearestTasks() {
        viewModelScope.launch {
            taskRepository.getNearestTasks()
                .onSuccess { tasks ->
                    nearestTasksCache = tasks
                    rebuildHomeState()
                }
                .onFailure {
                    nearestTasksCache = emptyList()
                    rebuildHomeState()
                }
        }
    }

    private fun rebuildHomeState() {
        val ownerId = currentUserId ?: sessionManager.user.value?.id
        val projectNameById = projectsCache.associate { it.id to it.projectName }

        uiState = uiState.copy(
            upcomingTasks = nearestTasksCache.map { it.toHomeTaskUi(projectNameById) },
            projects = projectsCache.map { it.toHomeProjectUi(ownerId, myTasksCache) },
        )
    }
}

private fun TaskDto.toHomeTaskUi(projectNameById: Map<Long, String>): HomeTaskUi {
    return HomeTaskUi(
        title = title,
        projectName = projectNameById[projectId] ?: "Проект #$projectId",
        deadline = formatApiDateShort(dueAt),
    )
}

private fun ProjectDto.toHomeProjectUi(
    currentUserId: Long?,
    myTasks: List<TaskDto>,
): HomeProjectUi {
    val projectTasks = myTasks.filter { it.projectId == id }

    return HomeProjectUi(
        id = id,
        title = projectName,
        completedCount = projectTasks.count { it.status == "done" },
        overdueCount = projectTasks.count { it.isOverdue() },
        isOwner = ownerId == currentUserId,
    )
}

private fun TaskDto.isOverdue(): Boolean {
    if (status == "done") return false
    val dueInstant = parseApiInstant(dueAt) ?: return false
    return dueInstant.isBefore(Instant.now())
}

private val homeDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy")

private fun formatApiDateShort(value: String?): String {
    val instant = parseApiInstant(value) ?: return "Без дедлайна"
    return instant
        .atZone(ZoneId.systemDefault())
        .format(homeDateFormatter)
}

private fun parseApiInstant(value: String?): Instant? {
    if (value.isNullOrBlank()) return null

    return runCatching { Instant.parse(value) }
        .getOrElse {
            runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
        }
}

private fun formatNameSurname(fullName: String): String {
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

    val a = core[0]
    val b = core[1]

    return when {
        looksLikeSurname(a) && !looksLikeSurname(b) -> "$b $a"
        !looksLikeSurname(a) && looksLikeSurname(b) -> "$a $b"
        else -> "$b $a"
    }
}

private fun isPatronymic(s: String): Boolean {
    val x = s.trim().lowercase()

    val ru = x.endsWith("вич") || x.endsWith("вна") ||
            x.endsWith("ович") || x.endsWith("овна") ||
            x.endsWith("евич") || x.endsWith("евна") ||
            x.endsWith("ыныч") || x.endsWith("ич") ||
            x.endsWith("кызы") || x.endsWith("оглы")

    val en = x.endsWith("vich") || x.endsWith("vna") ||
            x.endsWith("ovich") || x.endsWith("ovna") ||
            x.endsWith("evich") || x.endsWith("evna") ||
            x.endsWith("ych") || x.endsWith("ich") ||
            x.endsWith("kyzy") || x.endsWith("ogly") || x.endsWith("oglu")

    return ru || en
}

private fun looksLikeSurname(s: String): Boolean {
    val x = s.trim().lowercase()

    val ruEndings = listOf(
        "ов", "ев", "ин", "ын",
        "ский", "цкий", "ской",
        "ова", "ева", "ина", "ына",
        "ская", "цкая",
        "енко", "ко", "ук", "юк", "як",
        "ич",
    )

    val enEndings = listOf(
        "ov", "ev", "in",
        "sky", "ski", "skiy", "skyy", "skii",
        "enko", "ko", "uk", "yuk", "yak",
    )

    return ruEndings.any { x.endsWith(it) } || enEndings.any { x.endsWith(it) }
}