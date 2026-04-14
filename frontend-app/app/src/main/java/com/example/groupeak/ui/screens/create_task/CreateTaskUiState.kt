package com.example.groupeak.ui.screens.create_task

data class CreateTaskProjectOption(
    val id: Long,
    val name: String,
)

data class CreateTaskMemberOption(
    val userId: Long,
    val fullName: String,
    val position: String,
)

data class CreateTaskUiState(
    val title: String = "",
    val selectedProjectId: Long? = null,
    val project: String = "",
    val availableProjects: List<CreateTaskProjectOption> = emptyList(),

    val deadline: String = "", // DDMMYYYY

    val selectedParticipantIds: List<Long> = emptyList(),
    val participants: String = "",
    val availableMembers: List<CreateTaskMemberOption> = emptyList(),

    val description: String = "",
    val priority: String = "Средний",

    val isLoadingProjects: Boolean = false,
    val isLoadingMembers: Boolean = false,
    val isLoading: Boolean = false,

    val titleError: String? = null,
    val projectError: String? = null,
    val deadlineError: String? = null,
    val participantsError: String? = null,
    val descriptionError: String? = null,

    val statusMessage: String? = null,
    val statusMessageIsError: Boolean = false,
    val statusMessagePulse: Int = 0,
)