package com.example.groupeak.ui.screens.create_event

data class CreateEventUiState(
    val title: String = "",
    val project: String = "",
    val selectedProjectId: Long? = null,
    val date: String = "",
    val time: String = "",
    
    val participants: String = "",
    val selectedParticipantIds: List<Long> = emptyList(),
    
    val link: String = "",
    val description: String = "",
    
    val titleError: String? = null,
    val projectError: String? = null,
    val dateError: String? = null,
    val timeError: String? = null,
    val participantsError: String? = null,
    val descriptionError: String? = null,

    val availableProjects: List<CreateEventProjectOption> = emptyList(),
    val availableMembers: List<CreateEventMemberOption> = emptyList(),

    val isLoadingProjects: Boolean = false,
    val isLoadingMembers: Boolean = false,
    val isLoading: Boolean = false,

    val statusMessage: String? = null,
    val statusMessageIsError: Boolean = false,
    val statusMessagePulse: Int = 0,
)

data class CreateEventProjectOption(
    val id: Long,
    val name: String,
)

data class CreateEventMemberOption(
    val userId: Long,
    val fullName: String,
    val position: String,
)