package com.example.groupeak.ui.screens.create_event

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.domain.repository.EventRepository
import com.example.groupeak.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class CreateEventViewModel(
    private val projectRepository: ProjectRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    // We maintain StateFlow for uiState because MainNavGraph uses collectAsState for this screen.
    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    private fun updateState(update: (CreateEventUiState) -> CreateEventUiState) {
        _uiState.value = update(_uiState.value).clearStatus()
    }

    fun onTitleChange(value: String) {
        updateState { it.copy(title = value, titleError = null) }
    }

    fun onProjectChange(projectName: String) {
        val selectedProject = _uiState.value.availableProjects.firstOrNull { it.name == projectName }
        
        _uiState.value = _uiState.value.copy(
            project = selectedProject?.name.orEmpty(),
            selectedProjectId = selectedProject?.id,
            projectError = null,
            selectedParticipantIds = emptyList(),
            participants = "",
            participantsError = null,
            availableMembers = emptyList(),
        ).clearStatus()

        selectedProject?.id?.let { loadProjectMembers(it) }
    }

    fun onDateChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(8)
        updateState { it.copy(date = digits, dateError = null) }
    }

    fun onTimeChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(4)
        updateState { it.copy(time = digits, timeError = null) }
    }

    fun onParticipantsChange(namesString: String) {
        // Find matching users based on the string of names returned from UI
        val namesList = namesString.split(", ").filter { it.isNotBlank() }
        val matchedIds = _uiState.value.availableMembers
            .filter { namesList.contains(it.fullName) }
            .map { it.userId }

        updateState { it.copy(
            participants = namesString,
            selectedParticipantIds = matchedIds,
            participantsError = null
        )}
    }

    fun onLinkChange(value: String) {
        updateState { it.copy(link = value) }
    }

    fun onDescriptionChange(value: String) {
        updateState { it.copy(description = value) }
    }

    fun onCreateClick(onSuccess: () -> Unit) {
        if (!validate()) return
        
        val currentState = _uiState.value
        val projectId = currentState.selectedProjectId ?: return
        val startAt = createBackendDateTime(currentState.date, currentState.time) ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = eventRepository.createEvent(
                projectId = projectId,
                title = currentState.title.trim(),
                meetingUrl = currentState.link.trim().takeIf { it.isNotEmpty() },
                description = currentState.description.trim().takeIf { it.isNotEmpty() },
                startAt = startAt,
                endAt = null,
                participantIds = currentState.selectedParticipantIds
            )
            
            _uiState.value = _uiState.value.copy(isLoading = false)
            
            result.onSuccess {
                onSuccess()
            }.onFailure { throwable ->
                handleBackendError(throwable.message.orEmpty())
            }
        }
    }

    private fun validate(): Boolean {
        var isValid = true
        var pTitleError: String? = null
        var pProjectError: String? = null
        var pDateError: String? = null
        var pTimeError: String? = null

        val currentState = _uiState.value

        if (currentState.title.isBlank()) {
            pTitleError = "Введите название мероприятия"
            isValid = false
        }

        if (currentState.selectedProjectId == null) {
            pProjectError = "error"
            isValid = false
            showStatusMessage("Выберите проект", isError = true)
        }

        if (currentState.date.filter { it.isDigit() }.length < 8) {
            pDateError = "Введите полную дату (ДД.ММ.ГГГГ)"
            isValid = false
        }

        if (currentState.time.filter { it.isDigit() }.length < 4) {
            pTimeError = "Введите полное время (ЧЧ:ММ)"
            isValid = false
        }

        if (isValid && createBackendDateTime(currentState.date, currentState.time) == null) {
            pDateError = "Некорректная дата или время"
            pTimeError = "Некорректная дата или время"
            isValid = false
            showStatusMessage("Некорректная дата или время", isError = true)
        }

        _uiState.value = _uiState.value.copy(
            titleError = pTitleError,
            projectError = pProjectError,
            dateError = pDateError,
            timeError = pTimeError
        )

        return isValid
    }

    private fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingProjects = true).clearStatus()
            
            projectRepository.getProjects()
                .onSuccess { projects ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingProjects = false,
                        availableProjects = projects.map {
                            CreateEventProjectOption(id = it.id, name = it.projectName)
                        }
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoadingProjects = false,
                        availableProjects = emptyList()
                    )
                    showStatusMessage("Не удалось загрузить проекты", isError = true)
                }
        }
    }

    private fun loadProjectMembers(projectId: Long) {
        viewModelScope.launch {
             _uiState.value = _uiState.value.copy(isLoadingMembers = true).clearStatus()

            projectRepository.getProjectMembers(projectId)
                .onSuccess { members ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingMembers = false,
                        availableMembers = members.map {
                            CreateEventMemberOption(
                                userId = it.userId,
                                fullName = formatDisplayName(it.fullName),
                                position = it.position.ifBlank { "Участник" }
                            )
                        }
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMembers = false,
                        availableMembers = emptyList()
                    )
                    showStatusMessage("Не удалось загрузить участников", isError = true)
                }
        }
    }
    
    private fun createBackendDateTime(dateDigits: String, timeDigits: String): String? {
        val d = dateDigits.filter { it.isDigit() }.take(8)
        val t = timeDigits.filter { it.isDigit() }.take(4)
        if (d.length < 8 || t.length < 4) return null

        val parser = SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault()).apply {
            isLenient = false
            // Parses local time
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val parsedDate = runCatching { parser.parse(d + t) }.getOrNull() ?: return null
        return formatter.format(parsedDate)
    }
    
    private fun handleBackendError(message: String) {
        showStatusMessage(
            message.ifBlank { "Не удалось создать мероприятие" },
            isError = true
        )
    }

    private fun showStatusMessage(message: String, isError: Boolean) {
        _uiState.value = _uiState.value.copy(
            statusMessage = message,
            statusMessageIsError = isError,
            statusMessagePulse = _uiState.value.statusMessagePulse + 1,
        )
    }

    private fun CreateEventUiState.clearStatus(): CreateEventUiState = copy(
        statusMessage = null,
        statusMessageIsError = false,
    )
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