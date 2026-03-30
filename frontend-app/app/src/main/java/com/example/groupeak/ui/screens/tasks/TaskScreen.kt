
package com.example.groupeak.ui.screens.tasks

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.OnestFontFamily

private enum class TasksTab { ACTIVE, INACTIVE }
private enum class TaskBucket { CURRENT, REVIEW, INACTIVE }

private enum class TaskPriority(val title: String, val color: Color) {
    HIGH("Высокий приоритет", Color(0xFFFF2B2B)),
    MEDIUM("Средний приоритет", Color(0xFFD0C000)),
    LOW("Низкий приоритет", Color(0xFF1BCB41))
}

private enum class InactiveStatus(val title: String, val color: Color) {
    DONE("Выполнено", Color(0xFF32CD32)),
    OVERDUE("Просрочено", Color(0xFFFF1E1E))
}

private data class TaskParticipantUi(
    val name: String,
    val role: String
)

private data class TaskUi(
    val id: Int,
    val title: String,
    val project: String,
    val priority: TaskPriority,
    val deadline: String,
    val detailsDeadline: String = deadline,
    val description: String = "",
    val bucket: TaskBucket,
    val checked: Boolean = false,
    val inactiveStatus: InactiveStatus? = null,
    val participants: List<TaskParticipantUi> = emptyList()
)

private val NotifHitBoxSize = 28.dp
private val NotifIconSize = 22.dp
private val TaskCheckboxSize = 19.dp
private val TaskDividerThickness = 1.dp

private val DetailFieldHorizontalPadding = 16.dp
private val DetailFieldIconSpacing = 14.dp

@Composable
fun TasksScreen(
    onBack: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(TasksTab.ACTIVE) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedProjectFilter by remember { mutableStateOf<String?>(null) }
    var selectedPriorityFilter by remember { mutableStateOf<TaskPriority?>(null) }
    var selectedTask by remember { mutableStateOf<TaskUi?>(null) }
    var completingTaskId by remember { mutableStateOf<Int?>(null) }
    var recentlyMovedTaskId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    val tasks = remember {
        mutableStateListOf(
            TaskUi(
                id = 1,
                title = "Подготовить отчет по работе",
                project = "LinUX",
                priority = TaskPriority.HIGH,
                deadline = "22.02.26",
                detailsDeadline = "22.02.2026",
                description = "Требуется дополнить и отправить на проверку отчет, с новой информацией по работе",
                bucket = TaskBucket.CURRENT,
                participants = listOf(
                    TaskParticipantUi("Дупло Вячеслав", "Автор задачи"),
                    TaskParticipantUi("Петров Василий", "Frontend"),
                    TaskParticipantUi("Скляров Мирослав", "Designer"),
                    TaskParticipantUi("Иванов Артем", "Backend"),
                    TaskParticipantUi("Сидоров Максим", "QA"),
                    TaskParticipantUi("Кузнецов Илья", "Manager"),
                    TaskParticipantUi("Михайлова Анна", "Analyst"),
                    TaskParticipantUi("Орлов Кирилл", "DevOps"),
                    TaskParticipantUi("Фролов Денис", "iOS"),
                    TaskParticipantUi("Егорова Мария", "Android"),
                    TaskParticipantUi("Новиков Павел", "Research"),
                    TaskParticipantUi("Семенов Роман", "Support"),
                    TaskParticipantUi("Тарасова Виктория", "HR")
                )
            ),
            TaskUi(
                id = 2,
                title = "Проанализировать целевую аудиторию",
                project = "Fortnuts",
                priority = TaskPriority.MEDIUM,
                deadline = "25.02.26",
                bucket = TaskBucket.CURRENT
            ),
            TaskUi(
                id = 3,
                title = "Проверить код на корректность по тестам",
                project = "МПО ВЭМ",
                priority = TaskPriority.LOW,
                deadline = "28.02.26",
                bucket = TaskBucket.CURRENT
            ),
            TaskUi(
                id = 4,
                title = "Придумать задачу для ботанов",
                project = "МПО ВЭМ",
                priority = TaskPriority.HIGH,
                deadline = "28.02.26",
                bucket = TaskBucket.CURRENT
            ),
            TaskUi(
                id = 5,
                title = "Сверить бухгалтерию за 4-ый квартал",
                project = "iMo.Studio",
                priority = TaskPriority.LOW,
                deadline = "08.03.26",
                bucket = TaskBucket.CURRENT
            ),
            TaskUi(
                id = 6,
                title = "Сверить бухгалтерию за 4-ый квартал",
                project = "iMo.Studio",
                priority = TaskPriority.LOW,
                deadline = "08.03.26",
                bucket = TaskBucket.REVIEW,
                checked = true
            ),
            TaskUi(
                id = 7,
                title = "Подготовить отчет по работе",
                project = "LinUX",
                priority = TaskPriority.HIGH,
                deadline = "15.02.26",
                bucket = TaskBucket.INACTIVE,
                inactiveStatus = InactiveStatus.DONE
            ),
            TaskUi(
                id = 8,
                title = "Проанализировать целевую аудиторию",
                project = "Fortnuts",
                priority = TaskPriority.MEDIUM,
                deadline = "25.02.26",
                bucket = TaskBucket.INACTIVE,
                inactiveStatus = InactiveStatus.OVERDUE
            ),
            TaskUi(
                id = 9,
                title = "Проверить код на корректность по тестам",
                project = "МПО ВЭМ",
                priority = TaskPriority.LOW,
                deadline = "28.02.26",
                bucket = TaskBucket.INACTIVE,
                inactiveStatus = InactiveStatus.DONE
            ),
            TaskUi(
                id = 10,
                title = "Придумать задачу для ботанов",
                project = "МПО ВЭМ",
                priority = TaskPriority.HIGH,
                deadline = "28.02.26",
                bucket = TaskBucket.INACTIVE,
                inactiveStatus = InactiveStatus.DONE
            ),
            TaskUi(
                id = 11,
                title = "Сверить бухгалтерию за 4-ый квартал",
                project = "iMo.Studio",
                priority = TaskPriority.LOW,
                deadline = "08.03.26",
                bucket = TaskBucket.INACTIVE,
                inactiveStatus = InactiveStatus.OVERDUE
            )
        )
    }

    fun moveTaskToReview(taskId: Int) {
        if (completingTaskId != null) return

        completingTaskId = taskId

        scope.launch {
            delay(260)

            val index = tasks.indexOfFirst { it.id == taskId }
            if (index != -1) {
                tasks[index] = tasks[index].copy(
                    checked = true,
                    bucket = TaskBucket.REVIEW
                )
                recentlyMovedTaskId = taskId
            }

            completingTaskId = null

            delay(650)
            if (recentlyMovedTaskId == taskId) {
                recentlyMovedTaskId = null
            }
        }
    }

    fun matchesFilter(task: TaskUi): Boolean {
        val projectMatches = selectedProjectFilter == null || task.project == selectedProjectFilter
        val priorityMatches = selectedPriorityFilter == null || task.priority == selectedPriorityFilter
        return projectMatches && priorityMatches
    }

    val hasAppliedFilters = selectedProjectFilter != null || selectedPriorityFilter != null
    val isFilterHighlighted = showFilterDialog || hasAppliedFilters

    val availableProjects = tasks.map { it.project }.distinct()
    val currentTasks = tasks.filter { it.bucket == TaskBucket.CURRENT }.filter(::matchesFilter)
    val reviewTasks = tasks.filter { it.bucket == TaskBucket.REVIEW }.filter(::matchesFilter)
    val inactiveTasks = tasks.filter { it.bucket == TaskBucket.INACTIVE }.filter(::matchesFilter)

    GroupeakBackground {
        Column(modifier = modifier.fillMaxSize()) {
            ProfileHeader(
                title = "Список задач",
                hasNotifications = true,
                onBackClick = onBack,
                onNotificationsClick = onNotificationsClick
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 19.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TasksTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.width(21.dp))

                FilterButton(
                    isActive = isFilterHighlighted,
                    onClick = { showFilterDialog = true }
                )
            }

            if (showFilterDialog) {
                TasksFilterDialog(
                    availableProjects = availableProjects,
                    initialProject = selectedProjectFilter,
                    initialPriority = selectedPriorityFilter,
                    onDismiss = { showFilterDialog = false },
                    onApply = { project, priority ->
                        selectedProjectFilter = project
                        selectedPriorityFilter = priority
                        showFilterDialog = false
                    },
                    onReset = {
                        selectedProjectFilter = null
                        selectedPriorityFilter = null
                        showFilterDialog = false
                    }
                )
            }

            selectedTask?.let { task ->
                TaskDetailsDialog(
                    task = task,
                    onDismiss = { selectedTask = null }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (selectedTab == TasksTab.ACTIVE) {
                    item {
                        SectionTitle("Текущие", currentTasks.size)
                    }

                    items(
                        items = currentTasks,
                        key = { it.id }
                    ) { task ->
                        AnimatedVisibility(
                            visible = completingTaskId != task.id,
                            enter = expandVertically(),
                            exit = shrinkVertically(
                                animationSpec = tween(
                                    durationMillis = 260,
                                    easing = FastOutSlowInEasing
                                )
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 180)
                            ) + slideOutVertically(
                                animationSpec = tween(
                                    durationMillis = 260,
                                    easing = FastOutSlowInEasing
                                ),
                                targetOffsetY = { it / 2 }
                            )
                        ) {
                            TaskRow(
                                task = task,
                                muted = false,
                                rightText = null,
                                rightTextColor = Color.Transparent,
                                onCheckedChange = { moveTaskToReview(task.id) },
                                onClick = { selectedTask = task }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(3.dp))
                        SectionTitle("На проверке", reviewTasks.size)
                    }

                    items(
                        items = reviewTasks,
                        key = { it.id }
                    ) { task ->
                        ReviewAnimatedTaskRow(
                            task = task,
                            animateEnter = recentlyMovedTaskId == task.id,
                            rightText = "В обработке",
                            rightTextColor = Color(0xFFD6C100)
                        )
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(38.dp))
                    }

                    items(
                        items = inactiveTasks,
                        key = { it.id }
                    ) { task ->
                        TaskRow(
                            task = task,
                            muted = true,
                            rightText = task.inactiveStatus?.title,
                            rightTextColor = task.inactiveStatus?.color ?: Color.Transparent,
                            onCheckedChange = {},
                            onClick = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewAnimatedTaskRow(
    task: TaskUi,
    animateEnter: Boolean,
    rightText: String,
    rightTextColor: Color
) {
    var visible by remember(task.id, animateEnter) { mutableStateOf(!animateEnter) }

    LaunchedEffect(task.id, animateEnter) {
        if (animateEnter) {
            visible = false
            visible = true
        } else {
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 220, delayMillis = 40)
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            initialOffsetY = { it / 2 }
        ) + expandVertically(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        TaskRow(
            task = task,
            muted = true,
            rightText = rightText,
            rightTextColor = rightTextColor,
            onCheckedChange = {},
            onClick = null
        )
    }
}

@Composable
private fun ProfileHeader(
    title: String,
    hasNotifications: Boolean,
    onBackClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 55.dp, bottom = 27.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back_arrow),
            contentDescription = "Назад",
            tint = BluePrimary300,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .size(28.dp)
                .clickable { onBackClick() }
        )

        Text(
            text = title,
            fontSize = 32.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = BluePrimary300,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 19.dp)
                .size(NotifHitBoxSize)
                .clickable { onNotificationsClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notif),
                contentDescription = "Уведомления",
                tint = BluePrimary300,
                modifier = Modifier.size(NotifIconSize)
            )

            if (hasNotifications) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-3).dp, y = 2.dp)
                        .background(Color.Red, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun TasksTabs(
    selectedTab: TasksTab,
    onTabSelected: (TasksTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabWidth = 154.5.dp

    val offsetX by animateDpAsState(
        targetValue = if (selectedTab == TasksTab.ACTIVE) 0.dp else tabWidth,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "tasksTabOffset"
    )

    Box(
        modifier = modifier
            .width(309.dp)
            .height(39.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFB8B4C6),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .width(tabWidth)
                .fillMaxHeight()
                .background(
                    color = Color(0xFF71AAFF),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        Row(modifier = Modifier.fillMaxSize()) {
            TaskTabText(
                text = "В работе",
                selected = selectedTab == TasksTab.ACTIVE,
                onClick = { onTabSelected(TasksTab.ACTIVE) },
                modifier = Modifier.weight(1f)
            )

            TaskTabText(
                text = "Неактивные",
                selected = selectedTab == TasksTab.INACTIVE,
                onClick = { onTabSelected(TasksTab.INACTIVE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TaskTabText(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = if (selected) Color.White else Color(0xFF333B46)
        )
    }
}

@Composable
private fun FilterButton(
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(42.dp)
            .height(39.dp)
            .background(Color.White, CircleShape)
            .border(1.dp, Color(0xFFB8B4C6), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = if (isActive) R.drawable.ic_filter_active else R.drawable.ic_filter
            ),
            contentDescription = "Фильтр",
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun TasksFilterDialog(
    availableProjects: List<String>,
    initialProject: String?,
    initialPriority: TaskPriority?,
    onDismiss: () -> Unit,
    onApply: (String?, TaskPriority?) -> Unit,
    onReset: () -> Unit
) {
    var selectedProject by remember(initialProject) { mutableStateOf(initialProject) }
    var selectedPriority by remember(initialPriority) { mutableStateOf(initialPriority) }
    var projectExpanded by remember { mutableStateOf(false) }

    val dialogHeight by animateDpAsState(
        targetValue = if (projectExpanded) 417.dp else 245.dp,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "filter_dialog_height"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(348.dp)
                .height(dialogHeight)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(18.dp)
                )
                .clipToBounds()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 17.dp, start = 10.dp, end = 10.dp, bottom = 20.dp)
            ) {
                Text(
                    text = "Проект",
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = BluePrimary300,
                    modifier = Modifier.padding(start = 11.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                FilterProjectSelector(
                    availableProjects = availableProjects,
                    selectedProject = selectedProject,
                    expanded = projectExpanded,
                    onExpandedChange = { projectExpanded = it },
                    onProjectSelected = { selectedProject = it }
                )

                Spacer(modifier = Modifier.height(11.dp))

                Text(
                    text = "Приоритет",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = BluePrimary300,
                    modifier = Modifier.padding(start = 11.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                FilterPrioritySelector(
                    selectedPriority = selectedPriority,
                    onPrioritySelected = { selectedPriority = it }
                )

                Spacer(modifier = Modifier.height(23.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilterActionButton(
                        text = "Применить",
                        filled = true,
                        onClick = { onApply(selectedProject, selectedPriority) }
                    )

                    FilterActionButton(
                        text = "Сбросить",
                        filled = false,
                        onClick = {
                            selectedProject = null
                            selectedPriority = null
                            projectExpanded = false
                            onReset()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterProjectSelector(
    availableProjects: List<String>,
    selectedProject: String?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onProjectSelected: (String?) -> Unit
) {
    val blockHeight by animateDpAsState(
        targetValue = if (expanded) 226.dp else 55.dp,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "filter_project_block_height"
    )

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "filter_project_chevron_rotation"
    )

    val listScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .width(328.dp)
            .height(blockHeight)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFB0B3C7),
                shape = RoundedCornerShape(18.dp)
            )
            .clipToBounds()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clickable { onExpandedChange(!expanded) }
                .padding(start = 11.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterFolderIcon()

            Spacer(modifier = Modifier.width(8.dp))

            AnimatedContent(
                targetState = selectedProject ?: "Выберите проект для фильтрации",
                transitionSpec = {
                    (fadeIn(animationSpec = tween(180)) + slideInVertically(
                        animationSpec = tween(180),
                        initialOffsetY = { it / 3 }
                    )).togetherWith(
                        fadeOut(animationSpec = tween(120)) + slideOutVertically(
                            animationSpec = tween(120),
                            targetOffsetY = { -it / 3 }
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
                label = "filter_project_text"
            ) { projectText ->
                Text(
                    text = projectText,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF333B46),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            FilterChevronIcon(rotation = chevronRotation)
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(171.dp)
                    .verticalScroll(listScrollState)
                    .padding(top = 1.dp, bottom = 14.dp)
            ) {
                availableProjects.forEachIndexed { index, project ->
                    FilterProjectOptionRow(
                        text = project,
                        selected = selectedProject == project,
                        onClick = {
                            onProjectSelected(project)
                            scope.launch {
                                delay(120)
                                onExpandedChange(false)
                            }
                        }
                    )

                    if (index != availableProjects.lastIndex) {
                        Box(
                            modifier = Modifier
                                .padding(start = 49.dp, end = 49.dp)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFB0B3C7))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterProjectOptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val animatedTextColor by animateColorAsState(
        targetValue = if (selected) BluePrimary300 else Color(0xFF333B46),
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "filter_project_text_color"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "filter_project_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(49.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            lineHeight = 19.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = animatedTextColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
        )
    }
}

@Composable
private fun FilterPrioritySelector(
    selectedPriority: TaskPriority?,
    onPrioritySelected: (TaskPriority?) -> Unit
) {
    val segmentWidth = 328.dp / 3
    var previousPriority by remember { mutableStateOf(selectedPriority) }

    val selectedIndex = when (selectedPriority) {
        TaskPriority.LOW -> 0
        TaskPriority.MEDIUM -> 1
        TaskPriority.HIGH -> 2
        null -> 0
    }

    val shouldSnapToFirstSelection = previousPriority == null && selectedPriority != null

    val animatedOffset by animateDpAsState(
        targetValue = segmentWidth * selectedIndex,
        animationSpec = if (shouldSnapToFirstSelection) {
            tween(durationMillis = 0)
        } else {
            tween(durationMillis = 280, easing = FastOutSlowInEasing)
        },
        label = "filter_priority_offset"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (selectedPriority == null) 0f else 1f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "filter_priority_alpha"
    )
    val animatedColor by animateColorAsState(
        targetValue = when (selectedPriority) {
            TaskPriority.LOW -> Color(0xFF28C52E)
            TaskPriority.MEDIUM -> Color(0xFFD6C100)
            TaskPriority.HIGH -> Color(0xFFFF1E1E)
            null -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "filter_priority_color"
    )

    LaunchedEffect(selectedPriority) {
        previousPriority = selectedPriority
    }

    Box(
        modifier = Modifier
            .width(328.dp)
            .height(37.dp)
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFB8B4C6),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .padding(1.dp)
                .graphicsLayer { alpha = animatedAlpha }
                .background(
                    color = animatedColor,
                    shape = RoundedCornerShape(20.dp)
                )
        )

        Row(modifier = Modifier.fillMaxSize()) {
            FilterPriorityItem(
                text = "Низкий",
                selected = selectedPriority == TaskPriority.LOW,
                onClick = { onPrioritySelected(TaskPriority.LOW) },
                modifier = Modifier.weight(1f)
            )

            FilterPriorityItem(
                text = "Средний",
                selected = selectedPriority == TaskPriority.MEDIUM,
                onClick = { onPrioritySelected(TaskPriority.MEDIUM) },
                modifier = Modifier.weight(1f)
            )

            FilterPriorityItem(
                text = "Высокий",
                selected = selectedPriority == TaskPriority.HIGH,
                onClick = { onPrioritySelected(TaskPriority.HIGH) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FilterPriorityItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedTextColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0xFF444B59),
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "filter_priority_text_color"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontFamily = OnestFontFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = animatedTextColor
        )
    }
}

@Composable
private fun FilterActionButton(
    text: String,
    filled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(32.dp)
            .background(
                color = if (filled) Color(0xFF4C91F1) else Color.White,
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = if (filled) 0.dp else 1.dp,
                color = BluePrimary300,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = if (filled) Color.White else BluePrimary300
        )
    }
}

@Composable
private fun FilterFolderIcon() {
    Image(
        painter = painterResource(id = R.drawable.ic_folder),
        contentDescription = "Папка",
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun FilterChevronIcon(
    rotation: Float = 0f
) {
    Image(
        painter = painterResource(id = R.drawable.ic_down_arrow),
        contentDescription = "Открыть список проектов",
        modifier = Modifier
            .size(24.dp)
            .graphicsLayer { rotationZ = rotation }
    )
}

@Composable
private fun SectionTitle(
    title: String,
    count: Int
) {
    Text(
        text = "$title ($count)",
        fontSize = 18.sp,
        lineHeight = 20.sp,
        fontFamily = OnestFontFamily,
        fontWeight = FontWeight.Normal,
        color = Color.Black,
        modifier = Modifier.padding(start = 29.dp, bottom = 14.dp)
    )
}

@Composable
private fun TaskRow(
    task: TaskUi,
    muted: Boolean,
    rightText: String?,
    rightTextColor: Color = Color(0xFFD6C100),
    onCheckedChange: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val isInactiveTask = task.bucket == TaskBucket.INACTIVE
    val isReviewTask = task.bucket == TaskBucket.REVIEW

    val titleColor = when {
        isInactiveTask -> Color(0xFF70717A)
        isReviewTask -> Color(0xFF969696)
        else -> Color.Black
    }
    val projectColor = when {
        isInactiveTask -> Color(0xFF70717A)
        isReviewTask -> Color(0xFF969696)
        else -> Color.Black
    }
    val priorityTextColor = when {
        isInactiveTask || isReviewTask -> Color(0xFF70717A)
        else -> task.priority.color
    }
    val dateColor = when {
        isInactiveTask || isReviewTask -> Color(0xFF969696)
        else -> Color.Black
    }

    val dividerColor = Color(0xFFB0B3C7)
    val dividerWidth = if (isInactiveTask) 368.dp else 284.dp
    val dividerStartPadding = if (isInactiveTask) 22.dp else 54.dp
    val rowEndPadding = when {
        isInactiveTask && rightText == InactiveStatus.DONE.title -> 35.dp
        isInactiveTask && rightText == InactiveStatus.OVERDUE.title -> 30.dp
        else -> 35.dp
    }
    val rightBlockWidth = when {
        isInactiveTask -> 118.dp
        isReviewTask -> 128.dp
        else -> 32.dp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 36.dp, end = rowEndPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
            ) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    letterSpacing = (-0.4).sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = titleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Clip
                )

                Text(
                    text = "Проект: ${task.project}",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.25.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = projectColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.priority.title,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.25.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = priorityTextColor,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(Color(0xFFD9D9D9), CircleShape)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = task.deadline,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.25.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = if (isInactiveTask || isReviewTask) FontWeight.Normal else FontWeight.Medium,
                        color = dateColor,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier.width(rightBlockWidth),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (rightText != null) {
                    Text(
                        text = rightText,
                        fontSize = if (isInactiveTask) 15.sp else 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.25.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = rightTextColor,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                } else {
                    TaskCheckbox(
                        checked = task.checked,
                        onClick = onCheckedChange
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(17.dp))

        Box(
            modifier = Modifier
                .padding(start = dividerStartPadding)
                .width(dividerWidth)
                .height(TaskDividerThickness)
                .background(dividerColor)
        )
    }
}

@Composable
private fun TaskCheckbox(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkboxFillColor = Color(0xFF4C88EA)

    Box(
        modifier = modifier
            .size(TaskCheckboxSize)
            .background(
                color = if (checked) checkboxFillColor else Color.Transparent,
                shape = RoundedCornerShape(2.dp)
            )
            .border(
                width = 2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(2.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Canvas(modifier = Modifier.size(14.dp)) {
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.15f, size.height * 0.55f),
                    end = Offset(size.width * 0.40f, size.height * 0.80f),
                    strokeWidth = 2.8f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.40f, size.height * 0.80f),
                    end = Offset(size.width * 0.88f, size.height * 0.22f),
                    strokeWidth = 2.8f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun TaskDetailsDialog(
    task: TaskUi,
    onDismiss: () -> Unit
) {
    var participantsExpanded by remember(task.id) { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(348.dp)
                .height(616.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .offset(x = 10.dp, y = 20.dp)
                    .width(328.dp)
            ) {
                TaskDialogLabel("Название")
            }

            Box(
                modifier = Modifier
                    .offset(x = 10.dp, y = 44.dp)
                    .width(328.dp)
            ) {
                TaskDetailField(
                    iconRes = R.drawable.ic_project,
                    text = task.title
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = 10.dp, y = 110.dp)
                    .width(328.dp)
            ) {
                TaskDialogLabel("Проект")
            }

            Box(
                modifier = Modifier
                    .offset(x = 10.dp, y = 134.dp)
                    .width(328.dp)
            ) {
                TaskDetailField(
                    iconRes = R.drawable.ic_folder,
                    text = task.project
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = 10.dp, y = 200.dp)
                    .width(328.dp)
            ) {
                TaskDialogLabel("Дедлайн")
            }

            Box(
                modifier = Modifier
                    .offset(x = 10.dp, y = 224.dp)
                    .width(328.dp)
            ) {
                TaskDetailField(
                    iconRes = R.drawable.ic_calendar,
                    text = task.detailsDeadline
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = 10.dp, y = 380.dp)
                    .width(328.dp)
            ) {
                TaskDialogLabel("Описание")
            }

            Box(
                modifier = Modifier
                    .offset(x = 10.dp, y = 404.dp)
                    .width(328.dp)
            ) {
                TaskDescriptionField(text = task.description)
            }

            Box(
                modifier = Modifier
                    .offset(x = 10.dp, y = 532.dp)
                    .width(328.dp)
            ) {
                TaskDialogLabel("Приоритет")
            }

            Box(
                modifier = Modifier
                    .offset(x = 7.dp, y = 558.dp)
                    .width(328.dp)
            ) {
                TaskPriorityReadOnlySelector(priority = task.priority)
            }

            Box(
                modifier = Modifier
                    .offset(x = 10.dp, y = 290.dp)
                    .width(328.dp)
            ) {
                TaskDialogLabel("Участники")
            }

            Box(
                modifier = Modifier
                    .offset(x = 10.dp, y = 314.dp)
                    .width(328.dp)
            ) {
                ParticipantsSection(
                    participants = task.participants,
                    expanded = participantsExpanded,
                    onExpandChange = { participantsExpanded = it }
                )
            }
        }
    }
}

@Composable
private fun TaskDialogLabel(
    text: String
) {
    Text(
        text = text,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontFamily = OnestFontFamily,
        fontWeight = FontWeight.Normal,
        color = BluePrimary300,
        modifier = Modifier.padding(start = 11.dp)
    )
}

@Composable
private fun TaskDetailField(
    iconRes: Int,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFB0B3C7),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = DetailFieldHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(DetailFieldIconSpacing))

        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF333B46),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TaskDescriptionField(
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(117.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFB0B3C7),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = text.ifBlank { "Описание отсутствует" },
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF333B46)
        )
    }
}

@Composable
private fun ParticipantsSection(
    participants: List<TaskParticipantUi>,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    val animDuration = 260
    val scope = rememberCoroutineScope()
    var isClosing by remember { mutableStateOf(false) }
    val participantsScrollState = rememberScrollState()

    val hasExpandedContent = participants.isNotEmpty()
    val showExpandedContent = expanded || isClosing

    fun open() {
        isClosing = false
        onExpandChange(true)
    }

    fun close() {
        if (!expanded || isClosing) return
        isClosing = true
        onExpandChange(false)
        scope.launch {
            delay(animDuration.toLong())
            isClosing = false
        }
    }

    val containerHeight by animateDpAsState(
        targetValue = if (expanded && hasExpandedContent) 218.dp else 55.dp,
        animationSpec = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
        label = "taskParticipantsHeight"
    )

    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
        label = "taskParticipantsArrowRotation"
    )

    val dropdownHeight = (containerHeight - 55.dp).coerceAtLeast(0.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(containerHeight)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFB0B3C7),
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clickable {
                    if (!hasExpandedContent) return@clickable
                    if (expanded) close() else open()
                }
                .padding(horizontal = DetailFieldHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_members),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(DetailFieldIconSpacing))

            ParticipantsSummaryText(
                participants = participants,
                modifier = Modifier.weight(1f)
            )

            Image(
                painter = painterResource(id = R.drawable.ic_down_arrow),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = arrowRotation }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dropdownHeight)
                .clipToBounds()
        ) {
            if (showExpandedContent && hasExpandedContent) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(participantsScrollState)
                ) {
                    participants.forEachIndexed { index, participant ->
                        val topPadding = if (index == 0) 1.dp else 15.dp
                        Spacer(modifier = Modifier.height(topPadding))
                        ParticipantRow(participant = participant)
                        Spacer(modifier = Modifier.height(15.dp))

                        if (index != participants.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 22.dp, end = 22.dp)
                                    .height(1.dp)
                                    .background(Color(0xFFB0B3C7))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: TaskParticipantUi
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 35.dp, end = 19.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = participant.name,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Box(
            modifier = Modifier.width(112.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = participant.role,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF969696),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TaskPriorityReadOnlySelector(
    priority: TaskPriority
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(37.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFB0B3C7),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            TaskPriorityReadOnlyItem(
                text = "Низкий",
                selected = priority == TaskPriority.LOW,
                selectedColor = Color(0xFF28C52E),
                modifier = Modifier.weight(1f)
            )

            TaskPriorityReadOnlyItem(
                text = "Средний",
                selected = priority == TaskPriority.MEDIUM,
                selectedColor = Color(0xFFD6C100),
                modifier = Modifier.weight(1f)
            )

            TaskPriorityReadOnlyItem(
                text = "Высокий",
                selected = priority == TaskPriority.HIGH,
                selectedColor = Color(0xFFFF1E1E),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TaskPriorityReadOnlyItem(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = if (selected) selectedColor else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            fontFamily = OnestFontFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else Color(0xFF333B46)
        )
    }
}

@Composable
private fun ParticipantsSummaryText(
    participants: List<TaskParticipantUi>,
    modifier: Modifier = Modifier
) {
    val extraCount = (participants.size - 2).coerceAtLeast(0)
    val namesText = when {
        participants.isEmpty() -> "Участники не указаны"
        participants.size == 1 -> participants.first().name
        participants.size == 2 -> "${participants[0].name}, ${participants[1].name}"
        else -> "${participants[0].name}, ${participants[1].name}"
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = namesText,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF333B46),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (extraCount > 0) {
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "и еще $extraCount",
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333B46),
                maxLines = 1
            )
        }
    }
}

private fun shortParticipantName(fullName: String): String {
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    if (parts.isEmpty()) return fullName
    if (parts.size == 1) return parts.first()
    return "${parts[0]} ${parts[1].first()}."
}
