package com.example.groupeak.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.data.local.SessionManager
import com.example.groupeak.domain.repository.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    var uiState by mutableStateOf(
        HomeUiState(
            upcomingTasks = mockUpcomingTasks(),
            projects = mockProjects(),
        )
    )
        private set

    init {
        viewModelScope.launch {
            sessionManager.user.collectLatest { user ->
                if (user != null) {
                    uiState = uiState.copy(
                        userName = formatNameSurname(user.fullName),
                        email = user.email,
                        avatarUrl = user.avatarUrl,
                    )
                }
            }
        }

        refreshProfile()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            userRepository.getProfile()
        }
    }
}

private fun mockUpcomingTasks(): List<HomeTaskUi> = listOf(
    HomeTaskUi(
        title = "Дебаг кода",
        projectName = "SmartHome",
        deadline = "08.03.26",
    ),
    HomeTaskUi(
        title = "Проанализировать аудиторию",
        projectName = "BBwatch",
        deadline = "09.03.26",
    ),
    HomeTaskUi(
        title = "Подготовить отчет по проекту",
        projectName = "Fortnuts",
        deadline = "10.03.26",
    ),
)

private fun mockProjects(): List<HomeProjectUi> = listOf(
    HomeProjectUi(
        title = "SmartHome",
        completedCount = 13,
        overdueCount = 2,
        isOwner = true,
    ),
    HomeProjectUi(
        title = "Программа для Tahoe",
        completedCount = 5,
        overdueCount = 0,
    ),
    HomeProjectUi(
        title = "ManageGroup",
        completedCount = 0,
        overdueCount = 5,
    ),
    HomeProjectUi(
        title = "BBwatch",
        completedCount = 8,
        overdueCount = 1,
    ),
    HomeProjectUi(
        title = "Fortnuts",
        completedCount = 3,
        overdueCount = 4,
    ),
    HomeProjectUi(
        title = "Travel AI",
        completedCount = 11,
        overdueCount = 0,
    ),
    HomeProjectUi(
        title = "TaskFlow",
        completedCount = 2,
        overdueCount = 7,
    ),
)

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