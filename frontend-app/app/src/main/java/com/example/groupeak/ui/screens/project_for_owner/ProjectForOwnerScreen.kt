package com.example.groupeak.ui.screens.project_for_owner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.components.project.*
import com.example.groupeak.ui.theme.*
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
@Composable
fun ProjectForOwnerScreen(
    uiState: ProjectForOwnerUiState,
    onBack: () -> Unit,
    onNotificationsClick: () -> Unit,
    onTabSelected: (ProjectOwnerTab) -> Unit,
    onInfoClick: () -> Unit,
    onFilterClick: () -> Unit,
    onTaskClick: (Long) -> Unit = {},
    onMemberClick: (Long) -> Unit = {},
    onInviteMemberClick: () -> Unit = {},
    onDeleteTask: (Long) -> Unit = {},
    onDeleteMember: (Long) -> Unit = {},
    onDeleteEvent: (Long) -> Unit = {},
    onUpdateTaskDate: (Long, String) -> Unit = { _, _ -> },
    onInviteEmailChange: (String) -> Unit = {},
    onInvitePositionChange: (String) -> Unit = {},
    onSendInvite: () -> Unit = {},
    onNewDateChange: (String) -> Unit = {},
    hasNotifications: Boolean = true,
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    var showProjectDescriptionDialog by remember { mutableStateOf(false) }
    var appliedSelectedMemberIds by remember { mutableStateOf(setOf<Long>()) }
    var appliedSelectedPriorityName by remember { mutableStateOf<String?>(null) }
    var showInviteMemberDialogLocal by remember { mutableStateOf(false) }

    var showEditEventDialog by remember { mutableStateOf(false) }
    var showDeleteEventDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<ProjectEventItemUi?>(null) }
    var eventToDelete by remember { mutableStateOf<Long?>(null) }

    var showDeleteMemberDialogLocal by remember { mutableStateOf(false) }
    var memberToDeleteId by remember { mutableStateOf<Long?>(null) }

    // Состояния для новых диалогов из Figma
    var showApproveTaskDialog by remember { mutableStateOf(false) }
    var showRejectTaskDialog by remember { mutableStateOf(false) }
    var taskToProcessId by remember { mutableStateOf<Long?>(null) }

    val isFilterApplied = appliedSelectedMemberIds.isNotEmpty() || appliedSelectedPriorityName != null

    var taskToUpdateDateId by remember { mutableStateOf<Long?>(null) }
    var showUpdateTaskDateDialog by remember { mutableStateOf(false) }
    var showDeleteTaskDialog by remember { mutableStateOf(false) }
    var taskToDeleteId by remember { mutableStateOf<Long?>(null) }

    GroupeakBackground {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Box(modifier = Modifier.width(412.dp).height(908.dp)) {
                ProjectTopBar(
                    onBack = onBack,
                    onNotificationsClick = onNotificationsClick,
                    hasNotifications = hasNotifications
                )

                ProjectTitleBlock(
                    title = uiState.title,
                    deadlineText = uiState.deadlineText,
                    membersCount = uiState.membersCount,
                    tasksCount = uiState.tasksCount,
                    eventsCount = uiState.eventsCount,
                    onInfoClick = {
                        showProjectDescriptionDialog = true
                        onInfoClick()
                    }
                )

                Box(
                    modifier = Modifier
                        .offset(x = 12.dp, y = 204.dp)
                        .width(388.dp)
                        .height(
                            when (uiState.selectedTab) {
                                ProjectOwnerTab.TASKS -> 600.dp
                                ProjectOwnerTab.MEMBERS -> 590.dp  // ⬅️ Увеличили с 527.dp
                                ProjectOwnerTab.EVENTS -> 527.dp
                            }
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White),
                ) {
                    AnimatedProjectTabsHeader(selectedTab = uiState.selectedTab, onTabSelected = onTabSelected)

                    AnimatedVisibility(
                        visible = uiState.selectedTab == ProjectOwnerTab.TASKS,
                        enter = fadeIn(tween(180)) + slideInHorizontally(
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                            initialOffsetX = { it / 3 }  // Выезжает с правой трети экрана
                        ),
                        exit = fadeOut(tween(140)) + slideOutHorizontally(
                            animationSpec = tween(180, easing = FastOutSlowInEasing),
                            targetOffsetX = { it / 3 }   // Уезжает в правую треть
                        ),
                        modifier = Modifier.offset(x = 340.dp, y = 11.dp),
                    ) {
                        FilterButtonExact(onClick = { showFilterDialog = true }, isActive = isFilterApplied)
                    }

                    when (uiState.selectedTab) {
                        ProjectOwnerTab.TASKS -> ExactTasksBody(
                            uiState = uiState,
                            onTaskClick = onTaskClick,
                            onDeleteTask = { taskToDeleteId = it; showDeleteTaskDialog = true },
                            onUpdateDate = { taskToUpdateDateId = it; showUpdateTaskDateDialog = true },
                            onApproveTask = { taskToProcessId = it; showApproveTaskDialog = true },
                            onRejectTask = { taskToProcessId = it; showRejectTaskDialog = true }
                        )
                        ProjectOwnerTab.MEMBERS -> MembersBodySimple(
                            uiState = uiState,
                            onMemberClick = onMemberClick,
                            onInviteMemberClick = { showInviteMemberDialogLocal = true },
                            onDeleteMember = { memberToDeleteId = it; showDeleteMemberDialogLocal = true }
                        )
                        ProjectOwnerTab.EVENTS -> EventsBody(
                            events = uiState.events,
                            onEditEvent = { id -> eventToEdit = uiState.events.find { it.id == id }; showEditEventDialog = true },
                            onDeleteEvent = { eventToDelete = it; showDeleteEventDialog = true }
                        )
                    }
                }
            }
        }

        // --- Диалоги ---

        // Диалог удаления задачи (общий)
        if (showDeleteTaskDialog) {
            DeleteConfirmationDialog(
                title = "Удалить задачу?",
                onConfirm = { taskToDeleteId?.let(onDeleteTask); showDeleteTaskDialog = false },
                onDismiss = { showDeleteTaskDialog = false }
            )
        }

        // Диалог "Задача выполнена?" (на галочку)
        if (showApproveTaskDialog) {
            DeleteConfirmationDialog(
                title = "Задача выполнена?",
                onConfirm = { 
                    // Здесь вызывай метод ViewModel для подтверждения
                    showApproveTaskDialog = false 
                },
                onDismiss = { showApproveTaskDialog = false }
            )
        }

        // Диалог подтверждения отклонения (на крестик в секции "На проверке")
        if (showRejectTaskDialog) {
            DeleteConfirmationDialog(
                title = "Отклонить задачу?",
                onConfirm = { 
                    // Здесь вызывай метод ViewModel для отклонения
                    showRejectTaskDialog = false 
                },
                onDismiss = { showRejectTaskDialog = false }
            )
        }

        if (showInviteMemberDialogLocal) {
            InviteMemberDialog(
                email = uiState.inviteEmail,
                position = uiState.invitePosition,
                onEmailChange = onInviteEmailChange,
                onPositionChange = onInvitePositionChange,
                onSend = { onSendInvite(); showInviteMemberDialogLocal = false },
                onDismiss = { showInviteMemberDialogLocal = false }
            )
        }

        if (showUpdateTaskDateDialog) {
            UpdateDateDialog(
                newDate = uiState.newDate,
                dateError = uiState.dateError,
                onDateChange = onNewDateChange,
                onUpdate = { taskToUpdateDateId?.let { onUpdateTaskDate(it, uiState.newDate) }; showUpdateTaskDateDialog = false },
                onDismiss = { showUpdateTaskDateDialog = false }
            )
        }

        ProjectDescriptionDialog(
            visible = showProjectDescriptionDialog,
            projectName = uiState.title,
            deadline = uiState.deadlineText,
            description = uiState.description,
            onDismiss = { showProjectDescriptionDialog = false }
        )

        if (showEditEventDialog) {
            eventToEdit?.let { event ->
                ProjectEventEditDialog(
                    event = event,
                    projectName = uiState.title,
                    availableProjects = listOf("Программа для Tahoe", "Open Space"),
                    availableMembers = uiState.members.map { it.fullName to it.position },
                    onTitleChange = {}, onDateChange = {}, onTimeChange = {}, onLinkChange = {},
                    onDescriptionChange = {}, onParticipantsChange = {}, onProjectSelect = {},
                    onSave = { showEditEventDialog = false },
                    onDismiss = { showEditEventDialog = false }
                )
            }
        }

        if (showDeleteEventDialog) {
            DeleteConfirmationDialog(
                title = "Удалить мероприятие?",
                onConfirm = { eventToDelete?.let(onDeleteEvent); showDeleteEventDialog = false },
                onDismiss = { showDeleteEventDialog = false }
            )
        }

        if (showDeleteMemberDialogLocal) {
            DeleteConfirmationDialog(
                title = "Удалить участника?",
                onConfirm = { memberToDeleteId?.let(onDeleteMember); showDeleteMemberDialogLocal = false },
                onDismiss = { showDeleteMemberDialogLocal = false }
            )
        }

        ProjectFilterDialogHost(
            visible = showFilterDialog,
            members = uiState.members.map { com.example.groupeak.ui.screens.project.ProjectMemberItemUi(it.id, it.fullName, it.position, it.isOwner) },
            initialSelectedMemberIds = appliedSelectedMemberIds,
            onDismiss = { showFilterDialog = false },
            onApply = { ids, priority -> appliedSelectedMemberIds = ids; appliedSelectedPriorityName = priority?.name; showFilterDialog = false; onFilterClick() }
        )
    }
}
