package com.example.groupeak.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.data.remote.dto.EventResponse
import com.example.groupeak.data.remote.dto.ProjectMemberDto
import com.example.groupeak.domain.repository.EventRepository
import com.example.groupeak.domain.repository.ProjectRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class EventsViewModel(
    private val eventRepository: EventRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM, EE.", Locale("ru"))
    private val fullDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    init {
        refreshEvents()
    }

    fun refreshEvents() {
        viewModelScope.launch {
            eventRepository.getMyEvents().onSuccess { events ->
                val uniqueProjectIds = events.map { it.projectId }.distinct()

                // Fetch projects to get project names
                val projects = projectRepository.getProjects().getOrNull() ?: emptyList()
                val projectNamesById = projects.associate { it.id to it.projectName }

                // Fetch members for relevant projects
                val membersByProjectId = mutableMapOf<Long, List<ProjectMemberDto>>()
                
                val memberDeferreds = uniqueProjectIds.map { pid ->
                    async {
                        val members = projectRepository.getProjectMembers(pid).getOrNull() ?: emptyList()
                        pid to members
                    }
                }
                memberDeferreds.awaitAll().forEach { (pid, members) ->
                    membersByProjectId[pid] = members
                }

                val uiModels = events.map { event ->
                    val projectName = projectNamesById[event.projectId] ?: "Проект #${event.projectId}"
                    val projectMembers = membersByProjectId[event.projectId] ?: emptyList()
                    
                    val participants = (event.participants ?: emptyList()).map { userId ->
                        val member = projectMembers.find { it.userId == userId }
                        if (member != null) {
                            Participant(
                                name = formatDisplayName(member.fullName),
                                role = member.position.ifBlank { "Участник" }
                            )
                        } else {
                            Participant(
                                name = "Участник #$userId",
                                role = ""
                            )
                        }
                    }

                    val instant = parseApiInstant(event.startAt)
                    val zonedDateTime = instant?.atZone(ZoneId.systemDefault())

                    val formattedDate = if (zonedDateTime != null) {
                        val day = zonedDateTime.dayOfMonth
                        val monthList = listOf(
                            "Января", "Февраля", "Марта", "Апреля", "Мая", "Июня",
                            "Июля", "Августа", "Сентября", "Октября", "Ноября", "Декабря"
                        )
                        val month = monthList[zonedDateTime.monthValue - 1]
                        val dow = zonedDateTime.format(DateTimeFormatter.ofPattern("EE.", Locale("ru"))).lowercase()
                        "$day $month, $dow"
                    } else {
                        "Без даты"
                    }

                    EventUiModel(
                        id = event.id.toInt(),
                        time = zonedDateTime?.format(timeFormatter) ?: "",
                        title = event.title,
                        project = projectName,
                        date = formattedDate,
                        fullDate = zonedDateTime?.format(fullDateFormatter) ?: "",
                        participants = participants,
                        link = event.meetingUrl,
                        description = event.description
                    )
                }

                _uiState.update {
                    it.copy(events = uiModels.sortedByDescending { it.fullDate })
                }
            }
        }
    }

    private fun parseApiInstant(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        return runCatching { Instant.parse(value) }
            .getOrElse {
                runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
            }
    }

    fun selectEvent(event: EventUiModel) {
        _uiState.update {
            it.copy(selectedEvent = event)
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(selectedEvent = null)
        }
    }

    // Formats full name into "FirstName S." format
    private fun formatDisplayName(fullName: String): String {
        val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (parts.isEmpty()) return ""
        if (parts.size == 1) return parts[0]

        // Remove patronymic, keep only first name and surname
        val noPatronymic = parts.filterNot { isPatronymic(it) }
        val core = if (noPatronymic.size >= 2) noPatronymic else parts

        val first = core[0]
        val second = core[1]

        // Determine which is first name and which is surname
        val (firstName, surname) = if (looksLikeSurname(first) && !looksLikeSurname(second)) {
            second to first
        } else if (!looksLikeSurname(first) && looksLikeSurname(second)) {
            first to second
        } else {
            second to first
        }

        return "$firstName ${surname.first().uppercaseChar()}."
    }

    private fun isPatronymic(value: String): Boolean {
        val x = value.trim().lowercase()
        val ru = x.endsWith("вич") || x.endsWith("вна") || x.endsWith("ович") || x.endsWith("овна") || x.endsWith("евич") || x.endsWith("евна") || x.endsWith("ич") || x.endsWith("кызы") || x.endsWith("оглы")
        val en = x.endsWith("vich") || x.endsWith("vna") || x.endsWith("ovich") || x.endsWith("ovna") || x.endsWith("evich") || x.endsWith("evna") || x.endsWith("ich") || x.endsWith("kyzy") || x.endsWith("ogly") || x.endsWith("oglu")
        return ru || en
    }

    private fun looksLikeSurname(value: String): Boolean {
        val x = value.trim().lowercase()
        val ruEndings = listOf("ов", "ев", "ин", "ын", "ова", "ева", "ина", "ына", "ский", "цкий", "ской", "ская", "цкая", "енко", "ко", "ук", "юк", "як")
        val enEndings = listOf("ov", "ev", "in", "ova", "eva", "ina", "sky", "ski", "skiy", "skyy", "skii", "enko", "ko", "uk", "yuk", "yak")
        return ruEndings.any { x.endsWith(it) } || enEndings.any { x.endsWith(it) }
    }
}