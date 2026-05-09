package com.example.groupeak.ui.components.project

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.screens.project_for_owner.*
import com.example.groupeak.ui.theme.OnestFontFamily

// ===== ЦВЕТА (соответствуют Figma) =====
private val AccentBlue = Color(0xFF3988FE)
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

// ===== ОСНОВНОЙ КОМПОНЕНТ =====
@Composable
fun ExactTasksBody(
    uiState: ProjectForOwnerUiState,
    onTaskClick: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onUpdateDate: (Long) -> Unit,
    onApproveTask: (Long) -> Unit = {},
    onRejectTask: (Long) -> Unit = {},
) {
    val scrollState = rememberScrollState()
    var showAllCurrent by remember { mutableStateOf(false) }
    var showAllReview by remember { mutableStateOf(false) }
    var showAllInactive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 69.dp, start = 32.dp, end = 20.dp, bottom = 20.dp)
            .verticalScroll(scrollState),
    ) {
        when (uiState.tasksMode) {
            ProjectTasksMode.DEFAULT, ProjectTasksMode.ALL_THREE -> {
                // Секция "Текущие"
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
                    showUpdateDateButton = false,
                    isReviewSection = false,
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Секция "На проверке"
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
                    onApproveTask = onApproveTask,
                    onRejectTask = onRejectTask,
                    showDeleteButton = true,
                    showUpdateDateButton = true,
                    isReviewSection = true,
                )

                // Секция "Неактивные"
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
                        showUpdateDateButton = true,
                        isReviewSection = false,
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(430.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyStateText(text = "Нет задач", fontSize = 36.sp)
                }
            }
        }
    }
}

// ===== СЕКЦИЯ ЗАДАЧ (с пустым состоянием) =====
@Composable
fun TaskSectionOrEmpty(
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
            modifier = Modifier.fillMaxWidth().height(emptySectionHeight),
            contentAlignment = Alignment.Center,
        ) {
            EmptyStateText(text = emptyText, fontSize = emptyFontSize)
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

// ===== ЗАГОЛОВОК СЕКЦИИ =====
@Composable
fun TasksSectionHeader(
    title: String,
    actionText: String?,
    isExpanded: Boolean,
    onActionClick: (() -> Unit)?,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "section_arrow_rotation",
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
            letterSpacing = (-0.4).sp,
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
                    letterSpacing = (-0.4).sp,
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

// ===== ПУСТОЕ СОСТОЯНИЕ =====
@Composable
fun EmptyStateText(text: String, fontSize: TextUnit) {
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

// ===== АНИМИРОВАННЫЕ СТРОКИ ЗАДАЧ =====
@Composable
fun AnimatedTaskRows(
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
        isReviewSection = isReviewSection,
    )

    AnimatedVisibility(
        visible = expanded && extraTasks.isNotEmpty(),
        enter = expandVertically(
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = tween(durationMillis = 220)),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(durationMillis = 180)),
    ) {
        Column {
            if (initialTasks.isNotEmpty() && extraTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(17.dp))
            }
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
                isReviewSection = isReviewSection,
            )
        }
    }
}

// ===== ОТРИСОВКА СПИСКА ЗАДАЧ =====
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
        Spacer(modifier = Modifier.height(17.dp))
        HorizontalDivider(
            modifier = Modifier.padding(start = 25.dp).width(284.dp),
            thickness = 1.dp,
            color = BorderGray,
        )
        if (index != tasks.lastIndex) {
            Spacer(modifier = Modifier.height(17.dp))
        }
    }
}

// ===== ОДНА СТРОКА ЗАДАЧИ =====
@Composable
fun ExactTaskRow(
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
    isReviewSection: Boolean = false,
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Левая часть: заголовок + мета
        Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
            Text(
                text = task.title.trim(),
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = titleColor,
                letterSpacing = (-0.4).sp,
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
                    letterSpacing = 0.25.sp,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier.size(4.dp).clip(CircleShape).background(dotColor),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = task.deadline,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = metaColor,
                    letterSpacing = 0.25.sp,
                )
            }
        }

        // --- ТОЧЕЧНАЯ МАТЕМАТИКА ВЫРАВНИВАНИЯ ---
        // Стрелочка из заголовка (24dp) находится на расстоянии 12.dp от правого края.
        // Ниже мы высчитываем сдвиг (offset), чтобы центры иконок действий совпали с этими 12.dp.
        val iconOffset = if (isReviewSection) {
            14.dp // Ширина блока 48dp. Сдвиг на 14.dp выравнивает геометрический ЦЕНТР между галочкой и крестиком ровно под стрелкой.
        } else if (showUpdateDateButton && task.visualType == TaskVisualType.ALARM_OFF) {
            4.dp  // Центрируем правую иконку "Просрочено" (24dp) из неактивных ровно под стрелкой.
        } else {
            3.dp  // Центрируем одиночную иконку "Удалить" (22dp) в текущих или выполненных ровно под стрелкой.
        }

        // Правая часть: иконки действий со сдвигом
        Row(
            modifier = Modifier
                .offset(x = iconOffset)
                .padding(end = if (isReviewSection) 2.dp else 4.dp)
        ) {
            if (isReviewSection) {
                // Секция "На проверке": галочка / крестик
                Icon(
                    painter = painterResource(id = R.drawable.ic_green_check),
                    contentDescription = "Подтвердить",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp).clickable { onApproveClick() },
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Отклонить",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp).clickable { onRejectClick() },
                )
            } else if (showUpdateDateButton) {
                // Секция "Неактивные": статус или обновление даты
                when (task.visualType) {
                    TaskVisualType.DONE -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_task_completed),
                            contentDescription = "Выполнено в срок",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    TaskVisualType.ALARM_OFF -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh),
                            contentDescription = "Обновить дату",
                            tint = AccentBlue,
                            modifier = Modifier.size(22.dp).clickable { onUpdateDateClick() },
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_task_not_completed),
                            contentDescription = "Просрочено",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    else -> {
                        if (showDeleteButton) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Удалить",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(22.dp).clickable { onDeleteClick() },
                            )
                        }
                    }
                }
            } else {
                // Секция "Текущие" (здесь только кнопка удаления)
                if (showDeleteButton) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Удалить",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp).clickable { onDeleteClick() },
                    )
                }
            }
        }
    }
}