package com.example.groupeak.ui.screens.project_for_owner

import androidx.compose.foundation.layout.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.components.PrimaryButton
import com.example.groupeak.ui.theme.OnestFontFamily
import androidx.compose.ui.unit.times
import com.example.groupeak.ui.components.project.ProjectDescriptionDialog
import com.example.groupeak.ui.theme.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.input.pointer.pointerInput
import com.example.groupeak.ui.components.project.ProjectFilterDialogHost


// VisualTransformation для даты (ДД.ММ.ГГГГ)
private object DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter { it.isDigit() }.take(8)
        val out = buildString {
            for (i in raw.indices) {
                append(raw[i])
                if (i == 1 || i == 3) append('.')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, raw.length)
                var t = o
                if (raw.length > 2 && o > 2) t += 1
                if (raw.length > 4 && o > 4) t += 1
                return t.coerceIn(0, out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceIn(0, out.length)
                var o = t
                if (raw.length > 2 && t > 2) o -= 1
                if (raw.length > 4 && t > 5) o -= 1
                return o.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

// VisualTransformation для времени (ЧЧ:ММ)
private object TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter { it.isDigit() }.take(4)
        val out = buildString {
            for (i in raw.indices) {
                append(raw[i])
                if (i == 1) append(':')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, raw.length)
                var t = o
                if (raw.length > 2 && o > 2) t += 1
                return t.coerceIn(0, out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceIn(0, out.length)
                var o = t
                if (raw.length > 2 && t > 2) o -= 1
                return o.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

// Валидация даты
private fun isValidDate(date: String): Boolean {
    val digits = date.filter { it.isDigit() }
    if (digits.length != 8) return false

    return try {
        val day = digits.substring(0, 2).toInt()
        val month = digits.substring(2, 4).toInt()
        val year = digits.substring(4, 8).toInt()

        day in 1..31 && month in 1..12 && year >= 2020 && year <= 2100
    } catch (e: Exception) {
        false
    }
}

// Валидация времени
private fun isValidTime(time: String): Boolean {
    val digits = time.filter { it.isDigit() }
    if (digits.length != 4) return false

    return try {
        val hours = digits.substring(0, 2).toInt()
        val minutes = digits.substring(2, 4).toInt()

        hours in 0..23 && minutes in 0..59
    } catch (e: Exception) {
        false
    }
}

private val HeaderBlue = Color(0xFF1876FF)
private val AccentBlue = Color(0xFF3988FE)
private val SliderBlue = Color(0xFF71AAFF)
private val BorderGray = Color(0xFFB0B3C7)
private val DotGray = Color(0xFFD9D9D9)
private val DarkText = Color(0xFF333B46)
private val ReviewGray = Color(0xFF969696)
private val EmptyStateGray = Color(0xFF70717A)
private val LowGreen = Color(0xFF26BC30)
private val MediumYellow = Color(0xFFCDC036)
private val HighRed = Color(0xFFFF0000)
private val DeleteRed = Color(0xFFFF0000)

private const val ANIM_DURATION = 320

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
    var selectedEventLocal by remember { mutableStateOf<ProjectEventItemUi?>(null) }

    var showEditEventDialog by remember { mutableStateOf(false) }
    var showDeleteEventDialog by remember { mutableStateOf(false) }
    var showUpdateEventDateDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<ProjectEventItemUi?>(null) }
    var eventToDelete by remember { mutableStateOf<Long?>(null) }
    var eventToUpdateDate by remember { mutableStateOf<Long?>(null) }

    var showDeleteMemberDialogLocal by remember { mutableStateOf(false) }
    var memberToDeleteId by remember { mutableStateOf<Long?>(null) }

    var showProjectSelectDialog by remember { mutableStateOf(false) }
    var showParticipantsSelectDialog by remember { mutableStateOf(false) }

    val appliedSelectedPriority = appliedSelectedPriorityName?.let {
        try { ProjectTaskPriorityUi.valueOf(it) } catch (e: Exception) { null }
    }

    val isFilterApplied = appliedSelectedMemberIds.isNotEmpty() || appliedSelectedPriority != null

    var taskToUpdateDateId by remember { mutableStateOf<Long?>(null) }
    var showUpdateTaskDateDialog by remember { mutableStateOf(false) }

    // Переменные для диалога удаления задачи
    var showDeleteTaskDialog by remember { mutableStateOf(false) }
    var taskToDeleteId by remember { mutableStateOf<Long?>(null) }

    GroupeakBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .width(412.dp)
                    .height(908.dp),
            ) {
                ProjectTopBar(
                    onBack = onBack,
                    onNotificationsClick = onNotificationsClick,
                    hasNotifications = hasNotifications,
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
                        .height(if (uiState.selectedTab == ProjectOwnerTab.TASKS) 600.dp else 527.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White),
                ) {
                    // Табы
                    AnimatedProjectTabsHeader(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = onTabSelected,
                    )

                    // Кнопка фильтра
                    AnimatedVisibility(
                        visible = uiState.selectedTab == ProjectOwnerTab.TASKS,
                        enter = fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.8f, animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f, animationSpec = tween(150)),
                        modifier = Modifier.offset(x = 340.dp, y = 11.dp),
                    ) {
                        FilterButtonExact(
                            onClick = { showFilterDialog = true },
                            isActive = isFilterApplied,
                        )
                    }

                    // Контент вкладок (без изменений)
                    if (uiState.selectedTab == ProjectOwnerTab.TASKS) {
                        ExactTasksBody(
                            uiState = uiState,
                            onTaskClick = onTaskClick,
                            onDeleteTask = { taskId ->
                                taskToDeleteId = taskId
                                showDeleteTaskDialog = true
                            },
                            onUpdateDate = { taskId ->
                                taskToUpdateDateId = taskId
                                showUpdateTaskDateDialog = true
                            },
                        )
                    } else if (uiState.selectedTab == ProjectOwnerTab.MEMBERS) {
                        MembersBodySimple(
                            uiState = uiState,
                            onMemberClick = onMemberClick,
                            onInviteMemberClick = { showInviteMemberDialogLocal = true },
                            onDeleteMember = { memberId ->
                                memberToDeleteId = memberId
                                showDeleteMemberDialogLocal = true
                            },
                        )
                    } else {
                        EventsBody(
                            events = uiState.events,
                            onEditEvent = { eventId ->
                                eventToEdit = uiState.events.find { it.id == eventId }
                                showEditEventDialog = true
                            },
                            onDeleteEvent = { eventId ->
                                eventToDelete = eventId
                                showDeleteEventDialog = true
                            },
                        )
                    }
                }
            }
        }


        // Диалог подтверждения удаления задачи
        if (showDeleteTaskDialog) {
            DeleteConfirmationDialog(
                title = "Удалить задачу?",
                onConfirm = {
                    taskToDeleteId?.let { id -> onDeleteTask(id) }
                    showDeleteTaskDialog = false
                    taskToDeleteId = null
                },
                onDismiss = {
                    showDeleteTaskDialog = false
                    taskToDeleteId = null
                }
            )
        }

        // Delete Member Dialog
        if (uiState.showDeleteMemberDialog) {
            DeleteConfirmationDialog(
                title = "Удалить участника?",
                onConfirm = {
                    uiState.memberToDelete?.let { onDeleteMember(it) }
                    uiState.copy(showDeleteMemberDialog = false, memberToDelete = null)
                },
                onDismiss = { uiState.copy(showDeleteMemberDialog = false, memberToDelete = null) }
            )
        }

        // Invite Member Dialog
        if (showInviteMemberDialogLocal) {
            InviteMemberDialog(
                email = uiState.inviteEmail,
                position = uiState.invitePosition,
                onEmailChange = onInviteEmailChange,
                onPositionChange = onInvitePositionChange,
                onSend = {
                    onSendInvite()
                    showInviteMemberDialogLocal = false
                },
                onDismiss = {
                    showInviteMemberDialogLocal = false
                }
            )
        }

        // Update Task Date Dialog
        if (showUpdateTaskDateDialog) {
            UpdateDateDialog(
                newDate = uiState.newDate,
                dateError = uiState.dateError,
                onDateChange = onNewDateChange,
                onUpdate = {
                    taskToUpdateDateId?.let { id ->
                        onUpdateTaskDate(id, uiState.newDate)
                    }
                    showUpdateTaskDateDialog = false
                    taskToUpdateDateId = null
                },
                onDismiss = {
                    showUpdateTaskDateDialog = false
                    taskToUpdateDateId = null
                }
            )
        }

        ProjectDescriptionDialog(
            visible = showProjectDescriptionDialog,
            projectName = uiState.title,
            deadline = uiState.deadlineText,
            description = uiState.description,
            onDismiss = { showProjectDescriptionDialog = false }
        )

        // Edit Event Dialog
        if (showEditEventDialog) {
            eventToEdit?.let { event ->
                ProjectEventEditDialog(
                    event = event,
                    projectName = uiState.title,
                    availableProjects = listOf("Программа для Tahoe", "Open Space"), // Заглушка - получи из ViewModel
                    availableMembers = listOf( // Заглушка - получи из ViewModel
                        "Михаил Д." to "Frontend",
                        "Василий П." to "Backend",
                        "Андрей Р." to "Designer"
                    ),
                    initialTitle = event.title,
                    initialDate = event.date,
                    initialTime = event.time,
                    initialLink = "",
                    initialDescription = "",
                    initialParticipants = "",
                    onTitleChange = { /* onEditEvent(event.id, it, event.date, event.time, desc) */ },
                    onDateChange = { /* onEditEvent(event.id, event.title, it, event.time, desc) */ },
                    onTimeChange = { /* onEditEvent(event.id, event.title, event.date, it, desc) */ },
                    onLinkChange = { },
                    onDescriptionChange = { },
                    onParticipantsChange = { },
                    onProjectSelect = { },
                    onSave = {
                        showEditEventDialog = false
                        eventToEdit = null
                    },
                    onDismiss = {
                        showEditEventDialog = false
                        eventToEdit = null
                    }
                )
            }
        }

        // Delete Event Dialog
        if (showDeleteEventDialog) {
            DeleteConfirmationDialog(
                title = "Удалить мероприятие?",
                onConfirm = {
                    eventToDelete?.let { onDeleteEvent(it) }
                    showDeleteEventDialog = false
                    eventToDelete = null
                },
                onDismiss = {
                    showDeleteEventDialog = false
                    eventToDelete = null
                }
            )
        }

        // Update Event Date Dialog
        if (showUpdateEventDateDialog) {
            UpdateDateDialog(
                newDate = uiState.newDate,
                dateError = uiState.dateError,
                onDateChange = onNewDateChange,
                onUpdate = {
                    eventToUpdateDate?.let { /* onUpdateEventDate(it, uiState.newDate) */ }
                    showUpdateEventDateDialog = false
                    eventToUpdateDate = null
                },
                onDismiss = {
                    showUpdateEventDateDialog = false
                    eventToUpdateDate = null
                }
            )
        }

        // Delete Member Dialog
        if (showDeleteMemberDialogLocal) {
            DeleteConfirmationDialog(
                title = "Удалить участника?",
                onConfirm = {
                    memberToDeleteId?.let { onDeleteMember(it) }
                    showDeleteMemberDialogLocal = false
                    memberToDeleteId = null
                },
                onDismiss = {
                    showDeleteMemberDialogLocal = false
                    memberToDeleteId = null
                }
            )
        }

        // Filter Dialog
        ProjectFilterDialogHost(
            visible = showFilterDialog,
            // Преобразуем участников в формат диалога
            members = uiState.members.map { member ->
                com.example.groupeak.ui.screens.project.ProjectMemberItemUi(
                    id = member.id,
                    fullName = member.fullName,
                    position = member.position,
                    isOwner = member.isOwner
                )
            },
            initialSelectedMemberIds = appliedSelectedMemberIds,
            initialPriority = null,
            onDismiss = { showFilterDialog = false },
            onApply = { memberIds, priority ->
                appliedSelectedMemberIds = memberIds
                // Сохраняем приоритет как строку (если выбран)
                appliedSelectedPriorityName = priority?.name
                showFilterDialog = false
                onFilterClick()
            }
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(237.dp)
                .height(104.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = HeaderBlue,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Кнопка "Да" - синяя
                    PrimaryButton(
                        modifier = Modifier
                            .width(91.dp)
                            .height(32.dp),
                        text = "Да",
                        onClick = onConfirm,
                        fontSize = 14.sp,
                    )

                    // Кнопка "Нет" - белая с обводкой
                    Button(
                        modifier = Modifier
                            .width(91.dp)
                            .height(32.dp),
                        onClick = onDismiss,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = BluePrimary300
                        ),
                        border = BorderStroke(1.dp, BluePrimary300)
                    ) {
                        Text(
                            text = "Нет",
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InviteMemberDialog(
    email: String,
    position: String,
    onEmailChange: (String) -> Unit,
    onPositionChange: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(348.dp)
                .height(309.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(top = 16.dp, start = 10.dp, end = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Приглашение",
                    fontSize = 20.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = HeaderBlue,
                )

                Spacer(modifier = Modifier.height(11.dp))

                // Email field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Почта",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = HeaderBlue,
                        modifier = Modifier.padding(start = 11.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .border(1.dp, BorderGray, RoundedCornerShape(18.dp))
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(horizontal = 16.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            color = DarkText,
                        ),
                        decorationBox = { inner ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_email),
                                    contentDescription = null,
                                    tint = HeaderBlue,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(Modifier.weight(1f)) {
                                    if (email.isEmpty()) {
                                        Text(
                                            text = "Введите почту участника",
                                            fontSize = 14.sp,
                                            fontFamily = OnestFontFamily,
                                            color = EmptyStateGray,
                                        )
                                    }
                                    inner()
                                }
                            }
                        },
                    )
                }

                Spacer(modifier = Modifier.height(11.dp))

                // Position field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Должность",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = HeaderBlue,
                        modifier = Modifier.padding(start = 11.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = position,
                        onValueChange = onPositionChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .border(1.dp, BorderGray, RoundedCornerShape(18.dp))
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(horizontal = 10.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            color = DarkText,
                        ),
                        decorationBox = { inner ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_role),
                                    contentDescription = null,
                                    tint = HeaderBlue,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(Modifier.weight(1f)) {
                                    if (position.isEmpty()) {
                                        Text(
                                            text = "Введите должность участника",
                                            fontSize = 14.sp,
                                            fontFamily = OnestFontFamily,
                                            color = EmptyStateGray,
                                        )
                                    }
                                    inner()
                                }
                            }
                        },
                    )
                }

                Spacer(modifier = Modifier.height(21.dp))

                PrimaryButton(
                    modifier = Modifier
                        .width(261.dp)
                        .height(32.dp),
                    text = "Отправить приглашение",
                    onClick = onSend,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun UpdateDateDialog(
    newDate: String,
    dateError: String?,
    onDateChange: (String) -> Unit,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(227.dp)
                .height(184.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back_arrow),
                        contentDescription = "Назад",
                        tint = HeaderBlue,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDismiss() },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Введите новую дату",
                        fontSize = 18.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = HeaderBlue,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                BasicTextField(
                    value = newDate,
                    onValueChange = onDateChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .border(
                            1.dp,
                            if (dateError != null) DeleteRed else BorderGray,
                            RoundedCornerShape(18.dp),
                        )
                        .background(Color.White, RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        color = DarkText,
                    ),
                    decorationBox = { inner ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_calendar),
                                contentDescription = null,
                                tint = HeaderBlue,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(Modifier.weight(1f)) {
                                if (newDate.isEmpty()) {
                                    Text(
                                        text = "ДД.ММ.ГГГГ",
                                        fontSize = 14.sp,
                                        fontFamily = OnestFontFamily,
                                        color = EmptyStateGray,
                                    )
                                }
                                inner()
                            }
                        }
                    },
                )

                if (dateError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateError,
                        fontSize = 12.sp,
                        fontFamily = OnestFontFamily,
                        color = DeleteRed,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    modifier = Modifier
                        .width(154.dp)
                        .height(32.dp),
                    text = "Обновить",
                    onClick = onUpdate,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun ProjectTopBar(
    onBack: () -> Unit,
    onNotificationsClick: () -> Unit,
    hasNotifications: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 55.dp, bottom = 58.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .size(28.dp)
                .clickable { onBack() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back_arrow),
                contentDescription = "Назад",
                tint = HeaderBlue,
                modifier = Modifier.size(28.dp),
            )
        }

        Text(
            text = "Проект",
            fontSize = 32.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = HeaderBlue,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 19.dp)
                .size(28.dp)
                .clickable { onNotificationsClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notif),
                contentDescription = "Уведомления",
                tint = HeaderBlue,
                modifier = Modifier.size(22.dp),
            )

            if (hasNotifications) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 1.dp, y = (-1).dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                )
            }
        }
    }
}

@Composable
private fun ProjectTitleBlock(
    title: String,
    deadlineText: String,
    membersCount: Int,
    tasksCount: Int,
    eventsCount: Int,
    onInfoClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = 121.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .widthIn(max = 360.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onInfoClick() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = "Информация о проекте",
                            modifier = Modifier.size(20.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    Text(
                        text = title,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 30.sp,
                        lineHeight = 38.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 330.dp),
                    )
                }
            }

            SummaryRow(
                deadlineText = deadlineText,
                membersCount = membersCount,
                tasksCount = tasksCount,
                eventsCount = eventsCount,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun SummaryRow(
    deadlineText: String,
    membersCount: Int,
    tasksCount: Int,
    eventsCount: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(330.dp)
            .height(17.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Срок сдачи: ")
                    withStyle(style = SpanStyle(color = AccentBlue)) {
                        append(deadlineText)
                    }
                },
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = Color.Black,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(DotGray),
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "$membersCount ",
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = AccentBlue,
                maxLines = 1,
            )

            Text(
                text = "Участников",
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = Black,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(DotGray),
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "$tasksCount ",
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = AccentBlue,
                maxLines = 1,
            )

            Text(
                text = "Задач",
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = Black,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
private fun AnimatedProjectTabsHeader(
    selectedTab: ProjectOwnerTab,
    onTabSelected: (ProjectOwnerTab) -> Unit,
) {
    val isTasks = selectedTab == ProjectOwnerTab.TASKS
    val tabIndex = selectedTab.ordinal // 0, 1, 2

    // Целевые значения для анимации
    val targetWidth = if (isTasks) 327.dp else 367.dp
    // Для "Задач": прижат влево с отступом 6.dp
    // Для остальных: центрирование в 388.dp -> (388 - 367) / 2 ≈ 10.5.dp
    val targetOffsetX = if (isTasks) 6.dp else 10.5.dp

    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = tween(durationMillis = ANIM_DURATION, easing = FastOutSlowInEasing),
        label = "tabs_width"
    )

    val animatedOffsetX by animateDpAsState(
        targetValue = targetOffsetX,
        animationSpec = tween(durationMillis = ANIM_DURATION, easing = FastOutSlowInEasing),
        label = "tabs_offset"
    )

    // Явно считаем ширину одной вкладки
    val tabWidth = animatedWidth / 3f
    // Позиция ползунка
    val sliderOffset = tabIndex * tabWidth

    val tasksColor by animateColorAsState(
        targetValue = if (tabIndex == 0) Color.White else DarkText,
        animationSpec = tween(durationMillis = 180), label = "color_tasks"
    )
    val membersColor by animateColorAsState(
        targetValue = if (tabIndex == 1) Color.White else DarkText,
        animationSpec = tween(durationMillis = 180), label = "color_members"
    )
    val eventsColor by animateColorAsState(
        targetValue = if (tabIndex == 2) Color.White else DarkText,
        animationSpec = tween(durationMillis = 180), label = "color_events"
    )

    Box(
        modifier = Modifier
            .offset(x = animatedOffsetX, y = 11.dp)
            .width(animatedWidth)
            .height(39.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(18.dp))
            .background(Color.White),
    ) {
        // Ползунок
        Box(
            modifier = Modifier
                .offset(x = sliderOffset)
                .width(tabWidth)
                .height(39.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SliderBlue),
        )

        // Вкладки через Row с равномерным распределением
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabItem("Задачи", tasksColor, tabWidth) { onTabSelected(ProjectOwnerTab.TASKS) }
            TabItem("Участники", membersColor, tabWidth) { onTabSelected(ProjectOwnerTab.MEMBERS) }
            TabItem("Мероприятия", eventsColor, tabWidth) { onTabSelected(ProjectOwnerTab.EVENTS) }
        }
    }
}

@Composable
private fun TabItem(text: String, color: Color, width: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = -0.4.sp,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FilterButtonExact(
    onClick: () -> Unit,
    isActive: Boolean,
) {
    Box(
        modifier = Modifier
            .width(42.dp)
            .height(39.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = BorderGray,
                shape = RoundedCornerShape(18.dp),
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(
                id = if (isActive) R.drawable.ic_filter_active else R.drawable.ic_filter,
            ),
            contentDescription = "Фильтр",
            modifier = Modifier.size(32.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun ExactTasksBody(
    uiState: ProjectForOwnerUiState,
    onTaskClick: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onUpdateDate: (Long) -> Unit,
) {
    val scrollState = rememberScrollState()
    var showAllCurrent by remember { mutableStateOf(false) }
    var showAllReview by remember { mutableStateOf(false) }
    var showAllInactive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 69.dp, start = 25.dp, end = 20.dp, bottom = 20.dp)
            .verticalScroll(scrollState),
    ) {
        when (uiState.tasksMode) {
            ProjectTasksMode.DEFAULT, ProjectTasksMode.ALL_THREE -> {
                TaskSectionOrEmpty(
                    title = "Текущие (${uiState.currentTasksCount})",
                    actionText = if (uiState.currentTasksCount > 3) {
                        if (showAllCurrent) "Свернуть" else "Все \"Текущие\""
                    } else null,
                    isExpanded = showAllCurrent,
                    onActionClick = if (uiState.currentTasksCount > 3) {
                        { showAllCurrent = !showAllCurrent }
                    } else null,
                    tasks = uiState.currentTasks,
                    muted = false,
                    emptyText = "Нет текущих задач",
                    onTaskClick = onTaskClick,
                    onDeleteTask = onDeleteTask,
                    onUpdateDate = onUpdateDate,
                    showDeleteButton = true,
                    showUpdateDateButton = false, // Для текущих задач не показываем update
                    isReviewSection = false,
                )

                Spacer(modifier = Modifier.height(18.dp))

                TaskSectionOrEmpty(
                    title = "На проверке (${uiState.reviewTasksCount})",
                    actionText = if (uiState.reviewTasksCount > 3) {
                        if (showAllReview) "Свернуть" else "Все \"На проверке\""
                    } else null,
                    isExpanded = showAllReview,
                    onActionClick = if (uiState.reviewTasksCount > 3) {
                        { showAllReview = !showAllReview }
                    } else null,
                    tasks = uiState.reviewTasks,
                    muted = true,
                    emptyText = "Нет задач на проверке",
                    onTaskClick = onTaskClick,
                    onDeleteTask = onDeleteTask,
                    onUpdateDate = onUpdateDate,
                    onApproveTask = { taskId -> /* Логика подтверждения */ },
                    onRejectTask = { taskId -> /* Логика отклонения */ },
                    showDeleteButton = true,
                    showUpdateDateButton = true, // Для задач на проверке
                    isReviewSection = true,
                )

                if (uiState.tasksMode == ProjectTasksMode.ALL_THREE && uiState.inactiveTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(18.dp))

                    TaskSectionOrEmpty(
                        title = "Неактивные (${uiState.inactiveTasksCount})",
                        actionText = if (uiState.inactiveTasksCount > 3) {
                            if (showAllInactive) "Свернуть" else "Все \"Неактивные\""
                        } else null,
                        isExpanded = showAllInactive,
                        onActionClick = if (uiState.inactiveTasksCount > 3) {
                            { showAllInactive = !showAllInactive }
                        } else null,
                        tasks = uiState.inactiveTasks,
                        muted = true,
                        emptyText = "Нет неактивных задач",
                        onTaskClick = onTaskClick,
                        onDeleteTask = onDeleteTask,
                        onUpdateDate = onUpdateDate,
                        showDeleteButton = true,
                        showUpdateDateButton = true, // Для неактивных задач
                        isReviewSection = false,
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(430.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyStateText(
                        text = "Нет задач",
                        fontSize = 36.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskSectionOrEmpty(
    title: String,
    tasks: List<ProjectTaskItemUi>,
    muted: Boolean,
    emptyText: String,
    onTaskClick: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onUpdateDate: (Long) -> Unit,
    onApproveTask: (Long) -> Unit = {},
    onRejectTask: (Long) -> Unit = {},
    showDeleteButton: Boolean = false,
    showUpdateDateButton: Boolean = false,
    actionText: String? = null,
    isExpanded: Boolean = false,
    onActionClick: (() -> Unit)? = null,
    emptyFontSize: TextUnit = 24.sp,
    emptySectionHeight: Dp = 220.dp,
    isReviewSection: Boolean = false,
) {
    TasksSectionHeader(
        title = title,
        actionText = actionText,
        isExpanded = isExpanded,
        onActionClick = onActionClick,
    )

    Spacer(modifier = Modifier.height(14.dp))

    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(emptySectionHeight),
            contentAlignment = Alignment.Center,
        ) {
            EmptyStateText(
                text = emptyText,
                fontSize = emptyFontSize,
            )
        }
    } else {
        AnimatedTaskRows(
            tasks = tasks,
            muted = muted,
            expanded = isExpanded,
            onTaskClick = onTaskClick,
            onDeleteTask = onDeleteTask,
            onUpdateDate = onUpdateDate,
            onApproveTask = onApproveTask,
            onRejectTask = onRejectTask,
            showDeleteButton = showDeleteButton,
            showUpdateDateButton = showUpdateDateButton,
            limit = 3,
            isReviewSection = isReviewSection,
        )
    }
}

@Composable
private fun TasksSectionHeader(
    title: String,
    actionText: String?,
    isExpanded: Boolean,
    onActionClick: (() -> Unit)?,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "project_section_arrow_rotation",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 20.sp,
            color = Color.Black,
        )

        Spacer(modifier = Modifier.weight(1f))

        if (actionText != null && onActionClick != null) {
            Row(
                modifier = Modifier.clickable { onActionClick() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = actionText,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = AccentBlue,
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_down_arrow),
                    contentDescription = actionText,
                    tint = AccentBlue,
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .size(24.dp)
                        .rotate(arrowRotation),
                )
            }
        }
    }
}

@Composable
private fun EmptyStateText(
    text: String,
    fontSize: TextUnit,
) {
    Text(
        text = text,
        fontFamily = OnestFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = fontSize,
        lineHeight = if (fontSize == 36.sp) 40.sp else 28.sp,
        color = EmptyStateGray,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DrawTaskRows(
    tasks: List<ProjectTaskItemUi>,
    muted: Boolean,
    onTaskClick: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onUpdateDate: (Long) -> Unit,
    onApproveTask: (Long) -> Unit = {},
    onRejectTask: (Long) -> Unit = {},
    showDeleteButton: Boolean,
    showUpdateDateButton: Boolean,
    isReviewSection: Boolean = false,
) {
    tasks.forEachIndexed { index, task ->
        ExactTaskRow(
            task = task,
            muted = muted,
            onClick = { onTaskClick(task.id) },
            onDeleteClick = { onDeleteTask(task.id) },
            onApproveClick = { onApproveTask(task.id) },
            onRejectClick = { onRejectTask(task.id) },
            onUpdateDateClick = { onUpdateDate(task.id) },
            showDeleteButton = showDeleteButton,
            showApproveButton = isReviewSection,
            showRejectButton = isReviewSection,
            showUpdateDateButton = !isReviewSection && showUpdateDateButton,
            isReviewSection = isReviewSection,
        )
        // ... разделители ...
        Spacer(modifier = Modifier.height(17.dp))
        HorizontalDivider(
            modifier = Modifier
                .padding(start = 25.dp)
                .width(284.dp),
            thickness = 1.dp,
            color = BorderGray,
        )
        if (index != tasks.lastIndex) Spacer(modifier = Modifier.height(17.dp))
    }
}

@Composable
private fun AnimatedTaskRows(
    tasks: List<ProjectTaskItemUi>,
    muted: Boolean,
    expanded: Boolean,
    onTaskClick: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onUpdateDate: (Long) -> Unit,
    onApproveTask: (Long) -> Unit = {},
    onRejectTask: (Long) -> Unit = {},
    showDeleteButton: Boolean,
    showUpdateDateButton: Boolean,
    limit: Int = 3,
    isReviewSection: Boolean = false,
) {
    val initialTasks = tasks.take(limit)
    val extraTasks = if (tasks.size > limit) tasks.drop(limit) else emptyList()

    DrawTaskRows(
        tasks = initialTasks,
        muted = muted,
        onTaskClick = onTaskClick,
        onDeleteTask = onDeleteTask,
        onUpdateDate = onUpdateDate,
        onApproveTask = onApproveTask,
        onRejectTask = onRejectTask,
        showDeleteButton = showDeleteButton,
        showUpdateDateButton = showUpdateDateButton,
        isReviewSection = isReviewSection, // ✅
    )

    AnimatedVisibility(
        visible = expanded && extraTasks.isNotEmpty(),
        enter = expandVertically(animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing), expandFrom = Alignment.Top) + fadeIn(animationSpec = tween(durationMillis = 220)),
        exit = shrinkVertically(animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing), shrinkTowards = Alignment.Top) + fadeOut(animationSpec = tween(durationMillis = 180)),
    ) {
        Column {
            if (initialTasks.isNotEmpty() && extraTasks.isNotEmpty()) Spacer(modifier = Modifier.height(17.dp))
            DrawTaskRows(
                tasks = extraTasks,
                muted = muted,
                onTaskClick = onTaskClick,
                onDeleteTask = onDeleteTask,
                onUpdateDate = onUpdateDate,
                onApproveTask = onApproveTask,
                onRejectTask = onRejectTask,
                showDeleteButton = showDeleteButton,
                showUpdateDateButton = showUpdateDateButton,
                isReviewSection = isReviewSection, // ✅
            )
        }
    }
}

@Composable
private fun ExactTaskRow(
    task: ProjectTaskItemUi,
    muted: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onApproveClick: () -> Unit = {},
    onRejectClick: () -> Unit = {},
    onUpdateDateClick: () -> Unit = {},
    showDeleteButton: Boolean,
    showApproveButton: Boolean = false,
    showRejectButton: Boolean = false,
    showUpdateDateButton: Boolean = false,
    isReviewSection: Boolean = false, // ✅ Новый параметр для отличия "На проверке" от "Неактивных"
) {
    val titleColor = if (muted) ReviewGray else Color.Black
    val metaColor = if (muted) ReviewGray else Color.Black
    val dotColor = if (muted) ReviewGray else DotGray
    val priorityColor = when {
        muted -> ReviewGray
        task.priority == ProjectTaskPriorityUi.HIGH -> HighRed
        task.priority == ProjectTaskPriorityUi.MEDIUM -> MediumYellow
        else -> LowGreen
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.priority.title,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = priorityColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = task.deadline,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = metaColor,
                )
            }
        }

        Row(
            modifier = Modifier.padding(
                end = if (isReviewSection) 6.dp else 13.dp
            )
        ) {
            if (isReviewSection) {
                // ЗАДАЧИ НА ПРОВЕРКЕ
                Icon(
                    painter = painterResource(id = R.drawable.ic_green_check),
                    contentDescription = "Подтвердить",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp).clickable { onApproveClick() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Отменить",
                    tint = DeleteRed,
                    modifier = Modifier.size(24.dp).clickable { onRejectClick() }
                )
            } else if (showUpdateDateButton) {
                // НЕАКТИВНЫЕ ЗАДАЧИ
                when (task.visualType) {
                    TaskVisualType.DONE -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_task_completed),
                            contentDescription = "Выполнено в срок",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    TaskVisualType.ALARM_OFF -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh),
                            contentDescription = "Обновить дату",
                            tint = AccentBlue,
                            modifier = Modifier.size(24.dp).clickable { onUpdateDateClick() }
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_task_not_completed),
                            contentDescription = "Просрочено",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    else -> {
                        if (showDeleteButton) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Удалить",
                                tint = DeleteRed,
                                modifier = Modifier.size(26.dp).clickable { onDeleteClick() }
                            )
                        }
                    }
                }
            } else {
                // ТЕКУЩИЕ ЗАДАЧИ
                if (showDeleteButton) {
                    var deleteClicked by remember { mutableStateOf(false) }

                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Удалить",
                        tint = DeleteRed,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDeleteClick() }
                    )
                }
            }
        }
    }
}

@Composable
private fun MembersBodySimple(
    uiState: ProjectForOwnerUiState,
    onMemberClick: (Long) -> Unit,
    onInviteMemberClick: () -> Unit,
    onDeleteMember: (Long) -> Unit,
) {
    val scrollState = rememberScrollState()
    val members = uiState.members

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 69.dp, bottom = 20.dp)
            .verticalScroll(scrollState),
    ) {
        MembersHeaderExact()

        Spacer(modifier = Modifier.height(34.dp))

        members.forEachIndexed { index, member ->
            MemberRowExact(
                member = member,
                onClick = { onMemberClick(member.id) },
                onDeleteClick = { onDeleteMember(member.id) },
                showDeleteButton = !member.isOwner,
            )

            Spacer(modifier = Modifier.height(22.dp))

            if (index != members.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 51.dp)
                        .width(289.dp),
                    thickness = 1.dp,
                    color = BorderGray,
                )

                Spacer(modifier = Modifier.height(22.dp))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .padding(start = 69.dp)
                .clickable { onInviteMemberClick() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add_member),
                contentDescription = "Пригласить участника",
                tint = AccentBlue,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Пригласить участника",
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                color = AccentBlue,
            )
        }
    }
}

@Composable
private fun MembersHeaderExact() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(25.dp),
    ) {
        Text(
            text = "Участник",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            color = HeaderBlue,
            textAlign = TextAlign.Start,
            modifier = Modifier.offset(x = 40.dp),
        )

        Text(
            text = "Должность",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            color = HeaderBlue,
            textAlign = TextAlign.Start,
            modifier = Modifier.offset(x = 240.dp),
        )
    }
}

@Composable
private fun MemberRowExact(
    member: ProjectMemberItemUi,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    showDeleteButton: Boolean,
) {
    val memberColumnCenterX = 90.dp
    val positionColumnCenterX = 295.dp

    val memberColumnWidth = 170.dp
    val positionColumnWidth = 120.dp

    val density = LocalDensity.current
    var textWidthPx by remember(member.fullName) { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = memberColumnCenterX - memberColumnWidth / 2)
                .width(memberColumnWidth)
                .height(26.dp),
        ) {
            Text(
                text = member.fullName,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                color = DarkText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                    textWidthPx = textLayoutResult.size.width
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 140.dp),
            )

            if (member.isOwner) {
                val crownOffset = with(density) { (textWidthPx / 2f).toDp() + 18.dp }

                Image(
                    painter = painterResource(id = R.drawable.crown),
                    contentDescription = "Владелец",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = crownOffset)
                        .size(24.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        Text(
            text = member.position,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            color = EmptyStateGray,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = positionColumnCenterX - positionColumnWidth / 2)
                .width(positionColumnWidth),
        )

        if (showDeleteButton) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = "Удалить",
                tint = DeleteRed,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-10).dp)
                    .size(26.dp)
                    .clickable { onDeleteClick() },
            )
        }
    }
}

@Composable
private fun EventsBody(
    events: List<ProjectEventItemUi>,
    onEventClick: (ProjectEventItemUi) -> Unit = {},
    onEditEvent: (Long) -> Unit = {},
    onDeleteEvent: (Long) -> Unit = {},
) {
    val scrollState = rememberScrollState()

    // Группируем мероприятия по датам
    val groupedEvents = events.groupBy { it.date }

    if (groupedEvents.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 69.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            EmptyStateText(
                text = "Нет мероприятий",
                fontSize = 36.sp,
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 71.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
            .verticalScroll(scrollState),
    ) {
        groupedEvents.forEach { (date, dateEvents) ->
            // Заголовок даты
            Text(
                text = date,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 20.sp,
                letterSpacing = (-0.4).sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Карточки мероприятий для этой даты
            dateEvents.forEachIndexed { index, event ->
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) },
                    onEdit = { onEditEvent(event.id) },
                    onDelete = { onDeleteEvent(event.id) },
                )

                if (index != dateEvents.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}

@Composable
private fun EventCard(
    event: ProjectEventItemUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    // Логика определения высоты: если название длинное (>35 символов), делаем карточку выше (89.dp)
    // Иначе оставляем стандартную (69.dp)
    val isLongTitle = event.title.length > 35
    val cardHeight = if (isLongTitle) 89.dp else 69.dp
    // Высота синей линии тоже меняется
    val lineHeight = if (isLongTitle) 59.dp else 40.dp

    Box(
        modifier = Modifier
            .fillMaxWidth() // Заполняем всю ширину контейнера
            .height(cardHeight)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .background(Color(0xFFFEFE), RoundedCornerShape(16.dp))
            // Убрал clickable с Box, чтобы клик обрабатывался только на названии или всей карточке по логике
            .clickable { onClick() }
            .padding(vertical = 15.dp, horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Время
            Text(
                text = event.time,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
                color = Color.Black,
                modifier = Modifier.width(48.dp),
            )

            // Вертикальная синяя линия
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(lineHeight)
                    .background(Color(0xFF3988FE), RoundedCornerShape(18.dp))
                    .padding(horizontal = 13.dp),
            )

            // Название мероприятия (занимает всё доступное место)
            Text(
                text = event.title,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = (-0.4).sp,
                color = Color(0xFF1876FF),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 15.dp, end = 8.dp),
            )

            // Иконка редактирования
            Icon(
                painter = painterResource(id = R.drawable.ic_edit),
                contentDescription = "Редактировать",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onEdit() },
            )

            Spacer(modifier = Modifier.width(12.dp)) // Чуть уменьшил отступ

            // Иконка удаления
            Icon(
                painter = painterResource(id = R.drawable.ic_trash_red),
                contentDescription = "Удалить",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDelete() },
            )
        }
    }
}

@Composable
private fun EventRow(event: ProjectEventItemUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
    ) {
        Text(
            text = event.title,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = event.date,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = AccentBlue,
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(DotGray),
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = event.time,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = DarkText,
            )
        }
    }
}

// Диалог редактирования мероприятия
@Composable
private fun ProjectEventEditDialog(
    event: ProjectEventItemUi,
    projectName: String,
    availableProjects: List<String> = listOf(projectName),
    availableMembers: List<Pair<String, String>> = emptyList(),
    initialTitle: String = event.title,
    initialDate: String = event.date,
    initialTime: String = event.time,
    initialLink: String = "",
    initialDescription: String = "",
    initialParticipants: String = "",
    onTitleChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onLinkChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onParticipantsChange: (String) -> Unit,
    onProjectSelect: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var date by remember { mutableStateOf(initialDate) }
    var time by remember { mutableStateOf(initialTime) }
    var link by remember { mutableStateOf(initialLink) }
    var description by remember { mutableStateOf(initialDescription) }
    var participants by remember { mutableStateOf(initialParticipants) }
    var selectedProject by remember { mutableStateOf(projectName) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var showProjectSelectDialog by remember { mutableStateOf(false) }
    var showParticipantsSelectDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(348.dp)
                .height(675.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                // ✅ ТОЧНО КАК В CREATESCREEN: top=20, left/right=10, bottom=0
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 20.dp, start = 10.dp, end = 10.dp)
                ) {
                    // Название
                    Text(
                        text = "Название",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = BluePrimary300,
                        modifier = Modifier.padding(start = 11.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            onTitleChange(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(start = 11.dp, end = 11.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            color = DarkText,
                        ),
                        decorationBox = { inner ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_task_name),
                                    contentDescription = null,
                                    tint = BluePrimary300,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (title.isEmpty()) {
                                        Text(
                                            text = "Название мероприятия",
                                            fontSize = 14.sp,
                                            fontFamily = OnestFontFamily,
                                            color = Gray650,
                                        )
                                    }
                                    inner()
                                }
                            }
                        },
                    )
                    Spacer(modifier = Modifier.height(11.dp))

                    // Проект
                    Text(
                        text = "Проект",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = BluePrimary300,
                        modifier = Modifier.padding(start = 11.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .clickable { showProjectSelectDialog = true }
                            .padding(start = 11.dp, end = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_folder),
                            contentDescription = null,
                            tint = BluePrimary300,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedProject,
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            color = Color(0xFF333B46),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_down_arrow),
                            contentDescription = "Выбрать проект",
                            tint = BluePrimary300,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(11.dp))

                    // Дата и время
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Дата",
                                fontSize = 14.sp,
                                fontFamily = OnestFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = if (dateError != null) Color.Red else BluePrimary300,
                                modifier = Modifier.padding(start = 11.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            BasicTextField(
                                value = date,
                                onValueChange = {
                                    val filtered = it.filter { c -> c.isDigit() || c == '.' }.take(10)
                                    date = filtered
                                    onDateChange(filtered)
                                    dateError = if (filtered.isNotEmpty() && !isValidDate(filtered)) {
                                        "Неверная дата"
                                    } else null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp)
                                    .border(
                                        1.dp,
                                        if (dateError != null) Color.Red else GroupeakLightGray,
                                        RoundedCornerShape(18.dp)
                                    )
                                    .background(Color.White, RoundedCornerShape(18.dp))
                                    .padding(start = 11.dp, end = 11.dp),
                                singleLine = true,
                                visualTransformation = DateVisualTransformation,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 14.sp,
                                    fontFamily = OnestFontFamily,
                                    color = DarkText,
                                ),
                                decorationBox = { inner ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_calendar),
                                            contentDescription = null,
                                            tint = BluePrimary300,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (date.isEmpty()) {
                                                Text(
                                                    text = "ДД.ММ.ГГГГ",
                                                    fontSize = 14.sp,
                                                    fontFamily = OnestFontFamily,
                                                    color = Gray650,
                                                )
                                            }
                                            inner()
                                        }
                                    }
                                },
                            )
                            if (dateError != null) {
                                Text(
                                    text = dateError!!,
                                    fontSize = 12.sp,
                                    color = Color.Red,
                                    modifier = Modifier.padding(top = 4.dp, start = 11.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Время",
                                fontSize = 14.sp,
                                fontFamily = OnestFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = if (timeError != null) Color.Red else BluePrimary300,
                                modifier = Modifier.padding(start = 11.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            BasicTextField(
                                value = time,
                                onValueChange = {
                                    val filtered = it.filter { c -> c.isDigit() || c == ':' }.take(5)
                                    time = filtered
                                    onTimeChange(filtered)
                                    timeError = if (filtered.isNotEmpty() && !isValidTime(filtered)) {
                                        "Неверное время"
                                    } else null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp)
                                    .border(
                                        1.dp,
                                        if (timeError != null) Color.Red else GroupeakLightGray,
                                        RoundedCornerShape(18.dp)
                                    )
                                    .background(Color.White, RoundedCornerShape(18.dp))
                                    .padding(start = 11.dp, end = 11.dp),
                                singleLine = true,
                                visualTransformation = TimeVisualTransformation,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 14.sp,
                                    fontFamily = OnestFontFamily,
                                    color = DarkText,
                                ),
                                decorationBox = { inner ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_clock),
                                            contentDescription = null,
                                            tint = BluePrimary300,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (time.isEmpty()) {
                                                Text(
                                                    text = "ЧЧ:ММ",
                                                    fontSize = 14.sp,
                                                    fontFamily = OnestFontFamily,
                                                    color = Gray650,
                                                )
                                            }
                                            inner()
                                        }
                                    }
                                },
                            )
                            if (timeError != null) {
                                Text(
                                    text = timeError!!,
                                    fontSize = 12.sp,
                                    color = Color.Red,
                                    modifier = Modifier.padding(top = 4.dp, start = 11.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(11.dp))

                    // Участники
                    Text(
                        text = "Участники",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = BluePrimary300,
                        modifier = Modifier.padding(start = 11.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .clickable { showParticipantsSelectDialog = true }
                            .padding(start = 11.dp, end = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_members),
                            contentDescription = null,
                            tint = BluePrimary300,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val displayText = if (participants.isEmpty()) {
                            "Выберите участников"
                        } else {
                            val list = participants.split(", ").filter { it.isNotBlank() }
                            if (list.size <= 2) participants
                            else "${list[0]}, ${list[1]} и еще ${list.size - 2}"
                        }
                        Text(
                            text = displayText,
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            color = if (participants.isEmpty()) Gray650 else Color(0xFF333B46),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_down_arrow),
                            contentDescription = "Выбрать участников",
                            tint = BluePrimary300,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(11.dp))

                    // Ссылка
                    Text(
                        text = "Ссылка",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = BluePrimary300,
                        modifier = Modifier.padding(start = 11.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = link,
                        onValueChange = {
                            link = it
                            onLinkChange(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(start = 16.dp, end = 16.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            color = DarkText,
                        ),
                        decorationBox = { inner ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_link),
                                    contentDescription = null,
                                    tint = BluePrimary300,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (link.isEmpty()) {
                                        Text(
                                            text = "Ссылка на собрание",
                                            fontSize = 14.sp,
                                            fontFamily = OnestFontFamily,
                                            color = Gray650,
                                        )
                                    }
                                    inner()
                                }
                            }
                        },
                    )
                    Spacer(modifier = Modifier.height(11.dp))

                    // Описание (оригинальная высота 117.dp)
                    Text(
                        text = "Описание",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = BluePrimary300,
                        modifier = Modifier.padding(start = 11.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(117.dp)
                            .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(start = 11.dp, end = 5.dp, top = 6.dp)
                    ) {
                        BasicTextField(
                            value = description,
                            onValueChange = {
                                description = it
                                onDescriptionChange(it)
                            },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                fontFamily = OnestFontFamily,
                                color = Color(0xFF333B46),
                                lineHeight = 16.sp
                            ),
                            decorationBox = { inner ->
                                if (description.isEmpty()) {
                                    Text(
                                        text = "Описание мероприятия",
                                        fontSize = 14.sp,
                                        fontFamily = OnestFontFamily,
                                        color = Gray650,
                                    )
                                }
                                inner()
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))

                    // ✅ Кнопка Сохранить (точно как в CreateEventScreen)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp), // ✅ Отступ снизу у обёртки
                        contentAlignment = Alignment.Center
                    ) {
                        PrimaryButton(
                            modifier = Modifier
                                .width(154.dp)
                                .height(32.dp),
                            text = "Сохранить",
                            onClick = {
                                if (!isValidDate(date)) {
                                    dateError = "Неверная дата"
                                    return@PrimaryButton
                                }
                                if (!isValidTime(time)) {
                                    timeError = "Неверное время"
                                    return@PrimaryButton
                                }
                                onSave()
                            },
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Диалог выбора проекта
    if (showProjectSelectDialog) {
        Dialog(
            onDismissRequest = { showProjectSelectDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.width(348.dp).height(495.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Выбор проекта",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = OnestFontFamily,
                            color = BluePrimary300,
                            modifier = Modifier.padding(bottom = 15.dp).align(Alignment.CenterHorizontally)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().height(380.dp)
                        ) {
                            itemsIndexed(availableProjects) { index, project ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedProject = project
                                            onProjectSelect(project)
                                            showProjectSelectDialog = false
                                        }
                                        .padding(vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = project,
                                        fontSize = 20.sp,
                                        fontFamily = OnestFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        color = GroupeakDarkBlue,
                                        modifier = Modifier.width(220.dp).padding(start = 9.dp)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        painter = painterResource(
                                            id = if (selectedProject == project)
                                                R.drawable.ic_selected_circle
                                            else
                                                R.drawable.ic_circle
                                        ),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                if (index != availableProjects.lastIndex) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFB0B3C7))
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(23.dp))
                    }
                }
            }
        }
    }

    // Диалог выбора участников
    if (showParticipantsSelectDialog) {
        Dialog(
            onDismissRequest = { showParticipantsSelectDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.width(348.dp).height(495.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Выбор участников",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = OnestFontFamily,
                            color = BluePrimary300,
                            modifier = Modifier.padding(bottom = 15.dp).align(Alignment.CenterHorizontally)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().height(380.dp)
                        ) {
                            itemsIndexed(availableMembers) { index, (name, role) ->
                                val isSelected = participants.contains(name, ignoreCase = true)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val currentList = participants.split(", ").filter { it.isNotBlank() }.toMutableList()
                                            if (isSelected) {
                                                currentList.removeAll { it.equals(name, ignoreCase = true) }
                                            } else {
                                                currentList.add(name)
                                            }
                                            val newParticipants = currentList.joinToString(", ")
                                            participants = newParticipants
                                            onParticipantsChange(newParticipants)
                                        }
                                        .padding(vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 20.sp,
                                        fontFamily = OnestFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        color = GroupeakDarkBlue,
                                        modifier = Modifier.width(120.dp).padding(start = 9.dp)
                                    )
                                    Text(
                                        text = role,
                                        fontSize = 20.sp,
                                        fontFamily = OnestFontFamily,
                                        color = Gray650,
                                        modifier = Modifier.width(100.dp).wrapContentWidth(Alignment.CenterHorizontally)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        painter = painterResource(
                                            id = if (isSelected)
                                                R.drawable.ic_selected_check_mark
                                            else
                                                R.drawable.ic_check_mark
                                        ),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                if (index != availableMembers.lastIndex) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFB0B3C7))
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(23.dp))
                    }
                }
            }
        }
    }
}