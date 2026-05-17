package com.example.groupeak.ui.screens.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.components.project.ProjectDescriptionDialog
import com.example.groupeak.ui.components.project.ProjectFilterDialogHost
import com.example.groupeak.ui.theme.OnestFontFamily

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

private const val ANIM_DURATION = 320

@Composable
fun ProjectScreen(
    uiState: ProjectUiState,
    onBack: () -> Unit,
    onNotificationsClick: () -> Unit,
    onTabSelected: (ProjectTab) -> Unit,
    onInfoClick: () -> Unit,
    onFilterClick: () -> Unit,
    onTaskClick: (Long) -> Unit = {},
    onMemberClick: (Long) -> Unit = {},
    onInviteMemberClick: () -> Unit = {},
    hasNotifications: Boolean = true,
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    var showProjectDescriptionDialog by remember { mutableStateOf(false) }
    var appliedSelectedMemberIds by remember { mutableStateOf(setOf<Long>()) }
    var appliedSelectedPriorityName by remember { mutableStateOf<String?>(null) }

    val appliedSelectedPriority =
        appliedSelectedPriorityName?.let(ProjectTaskPriorityUi::valueOf)

    val isFilterApplied =
        appliedSelectedMemberIds.isNotEmpty() || appliedSelectedPriority != null

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
                    onInfoClick = {
                        showProjectDescriptionDialog = true
                        onInfoClick()
                    }
                )

                Box(
                    modifier = Modifier
                        .offset(x = 12.dp, y = 204.dp)
                        .width(388.dp)
                        .height(if (uiState.selectedTab == ProjectTab.TASKS) 600.dp else 527.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White),
                ) {
                    AnimatedProjectTabsHeader(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = onTabSelected,
                    )

                    AnimatedVisibility(
                        visible = uiState.selectedTab == ProjectTab.TASKS,
                        enter = fadeIn(
                            animationSpec = tween(durationMillis = 180)
                        ) + slideInHorizontally(
                            animationSpec = tween(
                                durationMillis = 220,
                                easing = FastOutSlowInEasing
                            ),
                            initialOffsetX = { it / 3 }
                        ),
                        exit = fadeOut(
                            animationSpec = tween(durationMillis = 140)
                        ) + slideOutHorizontally(
                            animationSpec = tween(
                                durationMillis = 180,
                                easing = FastOutSlowInEasing
                            ),
                            targetOffsetX = { it / 3 }
                        ),
                        modifier = Modifier.offset(x = 340.dp, y = 11.dp),
                    ) {
                        FilterButtonExact(
                            onClick = { showFilterDialog = true },
                            isActive = isFilterApplied,
                        )
                    }

                    if (uiState.selectedTab == ProjectTab.TASKS) {
                        ExactTasksBody(
                            uiState = uiState,
                            onTaskClick = onTaskClick,
                        )
                    } else {
                        MembersBodySimple(
                            uiState = uiState,
                            onMemberClick = onMemberClick,
                            onInviteMemberClick = onInviteMemberClick,
                        )
                    }
                }

                ProjectFilterDialogHost(
                    visible = showFilterDialog,
                    members = uiState.members,
                    initialSelectedMemberIds = appliedSelectedMemberIds,
                    initialPriority = appliedSelectedPriority,
                    onDismiss = { showFilterDialog = false },
                    onApply = { memberIds, priority ->
                        appliedSelectedMemberIds = memberIds
                        appliedSelectedPriorityName = priority?.name
                        showFilterDialog = false
                        onFilterClick()
                    }
                )

                ProjectDescriptionDialog(
                    visible = showProjectDescriptionDialog,
                    projectName = uiState.title,
                    deadline = uiState.deadlineText,
                    description = uiState.description,
                    onDismiss = { showProjectDescriptionDialog = false },
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
            .padding(top = 55.dp, bottom = 58.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .size(28.dp)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back_arrow),
                contentDescription = "Назад",
                tint = HeaderBlue,
                modifier = Modifier.size(28.dp)
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
                .fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 19.dp)
                .size(28.dp)
                .clickable { onNotificationsClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notif),
                contentDescription = "Уведомления",
                tint = HeaderBlue,
                modifier = Modifier.size(22.dp)
            )

            if (hasNotifications) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 1.dp, y = (-1).dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
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
    onInfoClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = 121.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .widthIn(max = 360.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onInfoClick() },
                        contentAlignment = Alignment.Center
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
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(330.dp)
            .height(17.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
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
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(DotGray)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = getMembersString(membersCount),
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = AccentBlue,
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(DotGray)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = getTasksString(tasksCount),
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = AccentBlue,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AnimatedProjectTabsHeader(
    selectedTab: ProjectTab,
    onTabSelected: (ProjectTab) -> Unit,
) {
    val isMembers = selectedTab == ProjectTab.MEMBERS

    val containerWidth = if (isMembers) 367.dp else 327.dp
    val startX = if (isMembers) 10.dp else 6.dp
    val sliderWidth = if (isMembers) 184.dp else 164.dp

    val sliderOffset by animateDpAsState(
        targetValue = if (selectedTab == ProjectTab.TASKS) 0.dp else if (isMembers) 183.dp else 163.dp,
        animationSpec = tween(
            durationMillis = ANIM_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "slider_offset"
    )

    val tasksTextColor by animateColorAsState(
        targetValue = if (selectedTab == ProjectTab.TASKS) Color.White else DarkText,
        animationSpec = tween(durationMillis = 180),
        label = "tasks_text_color"
    )

    val membersTextColor by animateColorAsState(
        targetValue = if (selectedTab == ProjectTab.MEMBERS) Color.White else DarkText,
        animationSpec = tween(durationMillis = 180),
        label = "members_text_color"
    )

    Box(
        modifier = Modifier
            .offset(x = startX, y = 11.dp)
            .width(containerWidth)
            .height(39.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(18.dp))
            .background(Color.White),
    ) {
        Box(
            modifier = Modifier
                .offset(x = sliderOffset)
                .width(sliderWidth)
                .height(39.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SliderBlue),
        )

        Box(
            modifier = Modifier
                .width(if (isMembers) 183.dp else 164.dp)
                .height(39.dp)
                .clickable { onTabSelected(ProjectTab.TASKS) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Задачи",
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = tasksTextColor,
            )
        }

        Box(
            modifier = Modifier
                .offset(x = if (isMembers) 183.dp else 163.dp)
                .width(sliderWidth)
                .height(39.dp)
                .clickable { onTabSelected(ProjectTab.MEMBERS) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Участники",
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = membersTextColor,
            )
        }
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
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(
                id = if (isActive) R.drawable.ic_filter_active else R.drawable.ic_filter
            ),
            contentDescription = "Фильтр",
            modifier = Modifier.size(32.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun DividerExact(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = BorderGray,
    )
}

@Composable
private fun ExactTasksBody(
    uiState: ProjectUiState,
    onTaskClick: (Long) -> Unit,
) {
    val scrollState = rememberScrollState()

    var showAllCurrent by remember(uiState.tasksMode) { mutableStateOf(false) }
    var showAllReview by remember(uiState.tasksMode) { mutableStateOf(false) }
    var showAllInactive by remember(uiState.tasksMode) { mutableStateOf(false) }

    val allSectionsEmpty =
        uiState.currentTasksCount == 0 &&
                uiState.reviewTasksCount == 0 &&
                uiState.inactiveTasksCount == 0

    if (allSectionsEmpty) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 69.dp, start = 25.dp, end = 20.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            EmptyStateText(
                text = "Нет задач",
                fontSize = 36.sp,
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 69.dp, start = 25.dp, end = 20.dp, bottom = 20.dp)
            .verticalScroll(scrollState)
    ) {
        when (uiState.tasksMode) {
            ProjectTasksMode.DEFAULT -> {
                TaskSectionOrEmpty(
                    title = "Текущие (${uiState.currentTasksCount})",
                    actionText = if (uiState.currentTasksCount > 3) {
                        if (showAllCurrent) "Свернуть" else "Все “Текущие”"
                    } else null,
                    isExpanded = showAllCurrent,
                    onActionClick = if (uiState.currentTasksCount > 3) {
                        { showAllCurrent = !showAllCurrent }
                    } else null,
                    tasks = uiState.currentTasks,
                    muted = false,
                    emptyText = "Нет текущих задач",
                    onTaskClick = onTaskClick,
                )

                Spacer(modifier = Modifier.height(18.dp))

                TaskSectionOrEmpty(
                    title = "На проверке (${uiState.reviewTasksCount})",
                    actionText = if (uiState.reviewTasksCount > 3) {
                        if (showAllReview) "Свернуть" else "Все “На проверке”"
                    } else null,
                    isExpanded = showAllReview,
                    onActionClick = if (uiState.reviewTasksCount > 3) {
                        { showAllReview = !showAllReview }
                    } else null,
                    tasks = uiState.reviewTasks,
                    muted = true,
                    emptyText = "Нет задач на проверке",
                    onTaskClick = onTaskClick,
                )
            }

            ProjectTasksMode.FILTERED_CURRENT -> {
                TaskSectionOrEmpty(
                    title = "Текущие (${uiState.currentTasksCount})",
                    actionText = if (uiState.currentTasksCount > 3) {
                        if (showAllCurrent) "Свернуть" else "Все “Текущие”"
                    } else null,
                    isExpanded = showAllCurrent,
                    onActionClick = if (uiState.currentTasksCount > 3) {
                        { showAllCurrent = !showAllCurrent }
                    } else null,
                    tasks = uiState.currentTasks,
                    muted = false,
                    emptyText = "Нет текущих задач",
                    onTaskClick = onTaskClick,
                )
            }

            ProjectTasksMode.REVIEW_INACTIVE -> {
                TaskSectionOrEmpty(
                    title = "На проверке (${uiState.reviewTasksCount})",
                    actionText = if (uiState.reviewTasksCount > 3) {
                        if (showAllReview) "Свернуть" else "Все “На проверке”"
                    } else null,
                    isExpanded = showAllReview,
                    onActionClick = if (uiState.reviewTasksCount > 3) {
                        { showAllReview = !showAllReview }
                    } else null,
                    tasks = uiState.reviewTasks,
                    muted = true,
                    emptyText = "Нет задач на проверке",
                    onTaskClick = onTaskClick,
                )

                Spacer(modifier = Modifier.height(18.dp))

                TaskSectionOrEmpty(
                    title = "Неактивные (${uiState.inactiveTasksCount})",
                    actionText = if (uiState.inactiveTasksCount > 3) {
                        if (showAllInactive) "Свернуть" else "Все “Неактивные”"
                    } else null,
                    isExpanded = showAllInactive,
                    onActionClick = if (uiState.inactiveTasksCount > 3) {
                        { showAllInactive = !showAllInactive }
                    } else null,
                    tasks = uiState.inactiveTasks,
                    muted = true,
                    emptyText = "Нет неактивных задач",
                    onTaskClick = onTaskClick,
                )
            }

            ProjectTasksMode.NO_REVIEW_INACTIVE -> {
                TaskSectionOrEmpty(
                    title = "На проверке (${uiState.reviewTasksCount})",
                    tasks = emptyList(),
                    muted = true,
                    emptyText = "Нет задач на проверке",
                    onTaskClick = onTaskClick,
                )

                Spacer(modifier = Modifier.height(18.dp))

                TaskSectionOrEmpty(
                    title = "Неактивные (${uiState.inactiveTasksCount})",
                    actionText = if (uiState.inactiveTasksCount > 3) {
                        if (showAllInactive) "Свернуть" else "Все “Неактивные”"
                    } else null,
                    isExpanded = showAllInactive,
                    onActionClick = if (uiState.inactiveTasksCount > 3) {
                        { showAllInactive = !showAllInactive }
                    } else null,
                    tasks = uiState.inactiveTasks,
                    muted = true,
                    emptyText = "Нет неактивных задач",
                    onTaskClick = onTaskClick,
                )
            }

            ProjectTasksMode.NO_CURRENT -> {
                TaskSectionOrEmpty(
                    title = "Текущие (${uiState.currentTasksCount})",
                    tasks = emptyList(),
                    muted = false,
                    emptyText = "Нет текущих задач",
                    onTaskClick = onTaskClick,
                )

                Spacer(modifier = Modifier.height(18.dp))

                TaskSectionOrEmpty(
                    title = "На проверке (${uiState.reviewTasksCount})",
                    actionText = if (uiState.reviewTasksCount > 3) {
                        if (showAllReview) "Свернуть" else "Все “На проверке”"
                    } else null,
                    isExpanded = showAllReview,
                    onActionClick = if (uiState.reviewTasksCount > 3) {
                        { showAllReview = !showAllReview }
                    } else null,
                    tasks = uiState.reviewTasks,
                    muted = true,
                    emptyText = "Нет задач на проверке",
                    onTaskClick = onTaskClick,
                )
            }

            ProjectTasksMode.EMPTY -> {
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

            ProjectTasksMode.ALL_THREE -> {
                TaskSectionOrEmpty(
                    title = "Текущие (${uiState.currentTasksCount})",
                    actionText = if (uiState.currentTasksCount > 3) {
                        if (showAllCurrent) "Свернуть" else "Все “Текущие”"
                    } else null,
                    isExpanded = showAllCurrent,
                    onActionClick = if (uiState.currentTasksCount > 3) {
                        { showAllCurrent = !showAllCurrent }
                    } else null,
                    tasks = uiState.currentTasks,
                    muted = false,
                    emptyText = "Нет текущих задач",
                    onTaskClick = onTaskClick,
                )

                Spacer(modifier = Modifier.height(18.dp))

                TaskSectionOrEmpty(
                    title = "На проверке (${uiState.reviewTasksCount})",
                    actionText = if (uiState.reviewTasksCount > 3) {
                        if (showAllReview) "Свернуть" else "Все “На проверке”"
                    } else null,
                    isExpanded = showAllReview,
                    onActionClick = if (uiState.reviewTasksCount > 3) {
                        { showAllReview = !showAllReview }
                    } else null,
                    tasks = uiState.reviewTasks,
                    muted = true,
                    emptyText = "Нет задач на проверке",
                    onTaskClick = onTaskClick,
                )

                Spacer(modifier = Modifier.height(18.dp))

                TaskSectionOrEmpty(
                    title = "Неактивные (${uiState.inactiveTasksCount})",
                    actionText = if (uiState.inactiveTasksCount > 3) {
                        if (showAllInactive) "Свернуть" else "Все “Неактивные”"
                    } else null,
                    isExpanded = showAllInactive,
                    onActionClick = if (uiState.inactiveTasksCount > 3) {
                        { showAllInactive = !showAllInactive }
                    } else null,
                    tasks = uiState.inactiveTasks,
                    muted = true,
                    emptyText = "Нет неактивных задач",
                    onTaskClick = onTaskClick,
                )
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
    actionText: String? = null,
    isExpanded: Boolean = false,
    onActionClick: (() -> Unit)? = null,
    emptyFontSize: TextUnit = 24.sp,
    emptySectionHeight: Dp = 220.dp,
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
            limit = 3,
        )
    }
}

@Composable
private fun TasksSectionHeader(
    title: String,
    actionText: String? = null,
    isExpanded: Boolean = false,
    onActionClick: (() -> Unit)? = null,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "project_section_arrow_rotation"
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

enum class ProjectTaskVisualType {
    NONE,
    DONE,
    ALARM_OFF
}

private fun ProjectTaskVisualType.toDrawableRes(): Int? {
    return when (this) {
        ProjectTaskVisualType.NONE -> null
        ProjectTaskVisualType.DONE -> R.drawable.ic_task_completed
        ProjectTaskVisualType.ALARM_OFF -> R.drawable.ic_alarm_off
    }
}

@Composable
private fun AnimatedTaskRows(
    tasks: List<ProjectTaskItemUi>,
    muted: Boolean,
    expanded: Boolean,
    onTaskClick: (Long) -> Unit,
    limit: Int = 3,
) {
    val initialTasks = tasks.take(limit)
    val extraTasks = if (tasks.size > limit) tasks.drop(limit) else emptyList()

    DrawTaskRows(
        tasks = initialTasks,
        muted = muted,
        onTaskClick = onTaskClick,
    )

    AnimatedVisibility(
        visible = expanded && extraTasks.isNotEmpty(),
        enter = expandVertically(
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            expandFrom = Alignment.Top
        ) + fadeIn(
            animationSpec = tween(durationMillis = 220)
        ),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            shrinkTowards = Alignment.Top
        ) + fadeOut(
            animationSpec = tween(durationMillis = 180)
        )
    ) {
        Column {
            if (initialTasks.isNotEmpty() && extraTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(17.dp))
            }

            DrawTaskRows(
                tasks = extraTasks,
                muted = muted,
                onTaskClick = onTaskClick,
            )
        }
    }
}
@Composable
private fun DrawTaskRows(
    tasks: List<ProjectTaskItemUi>,
    muted: Boolean,
    onTaskClick: (Long) -> Unit,
) {
    tasks.forEachIndexed { index, task ->
        ExactTaskRow(
            task = task,
            muted = muted,
            onClick = { onTaskClick(task.id) },
            trailingIconRes = task.visualType.toDrawableRes(),
        )

        Spacer(modifier = Modifier.height(17.dp))

        DividerExact(modifier = Modifier.padding(start = 25.dp))

        if (index != tasks.lastIndex) {
            Spacer(modifier = Modifier.height(17.dp))
        }
    }
}

@Composable
private fun ExactTaskRow(
    task: ProjectTaskItemUi,
    muted: Boolean,
    onClick: () -> Unit,
    trailingIconRes: Int? = null,
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
        Column(
            modifier = Modifier.weight(1f)
        ) {
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
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

        if (trailingIconRes != null) {
            Spacer(modifier = Modifier.width(10.dp))

            val iconSize = if (trailingIconRes == R.drawable.ic_alarm_off) 30.dp else 32.dp

            Image(
                painter = painterResource(id = trailingIconRes),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun MembersBodySimple(
    uiState: ProjectUiState,
    onMemberClick: (Long) -> Unit,
    onInviteMemberClick: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val members = uiState.members

    if (members.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 69.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateText(
                text = "Нет участников",
                fontSize = 36.sp,
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 69.dp, bottom = 20.dp)
            .verticalScroll(scrollState)
    ) {
        MembersHeaderExact()

        Spacer(modifier = Modifier.height(34.dp))

        members.forEachIndexed { index, member ->
            MemberRowExact(
                member = member,
                onClick = { onMemberClick(member.id) }
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

        if (uiState.isOwner) {
            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .padding(start = 69.dp)
                    .clickable { onInviteMemberClick() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_button),
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
}

@Composable
private fun MembersHeaderExact() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(25.dp)
    ) {
        Text(
            text = "Участник",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            color = HeaderBlue,
            textAlign = TextAlign.Start,
            modifier = Modifier.offset(x = 40.dp)
        )

        Text(
            text = "Должность",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            color = HeaderBlue,
            textAlign = TextAlign.Start,
            modifier = Modifier.offset(x = 240.dp)
        )
    }
}

@Composable
private fun MemberRowExact(
    member: ProjectMemberItemUi,
    onClick: () -> Unit,
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
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = memberColumnCenterX - memberColumnWidth / 2)
                .width(memberColumnWidth)
                .height(26.dp)
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
                    .widthIn(max = 140.dp)
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
                    contentScale = ContentScale.Fit
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
                .width(positionColumnWidth)
        )
    }
}

private fun getMembersString(count: Int): String {
    val lastDigit = count % 10
    val lastTwoDigits = count % 100
    return when {
        lastTwoDigits in 11..14 -> "$count Участников"
        lastDigit == 1 -> "$count Участник"
        lastDigit in 2..4 -> "$count Участника"
        else -> "$count Участников"
    }
}

private fun getTasksString(count: Int): String {
    val lastDigit = count % 10
    val lastTwoDigits = count % 100
    return when {
        lastTwoDigits in 11..14 -> "$count Задач"
        lastDigit == 1 -> "$count Задача"
        lastDigit in 2..4 -> "$count Задачи"
        else -> "$count Задач"
    }
}