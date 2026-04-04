package com.example.groupeak.ui.screens.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.theme.OnestFontFamily

private val HeaderBlue = Color(0xFF1876FF)
private val AccentBlue = Color(0xFF3988FE)
private val SliderBlue = Color(0xFF71AAFF)
private val BorderGray = Color(0xFFB0B3C7)
private val DotGray = Color(0xFFD9D9D9)
private val DarkText = Color(0xFF333B46)
private val ReviewGray = Color(0xFF969696)
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

                InfoButton(
                    onClick = onInfoClick,
                    modifier = Modifier.offset(x = 25.dp, y = 130.dp),
                )

                Text(
                    text = uiState.title,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 30.sp,
                    lineHeight = 38.sp,
                    color = Color.Black,
                    modifier = Modifier.offset(x = 48.dp, y = 121.dp),
                )

                SummaryRow(
                    deadlineText = uiState.deadlineText,
                    membersCount = uiState.membersCount,
                    tasksCount = uiState.tasksCount,
                    modifier = Modifier.offset(x = 63.dp, y = 161.dp),
                )

                Box(
                    modifier = Modifier
                        .offset(x = 12.dp, y = 204.dp)
                        .width(388.dp)
                        .height(if (uiState.selectedTab == ProjectTab.TASKS) 593.dp else 527.dp)
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
                        FilterButtonExact(onClick = onFilterClick)
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
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = 8.dp, y = 62.dp)
                .size(28.dp)
                .clickable { onBack() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back_arrow),
                contentDescription = "Назад",
                tint = AccentBlue,
                modifier = Modifier.size(28.dp),
            )
        }

        Text(
            text = "Проект",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 41.sp,
            color = HeaderBlue,
            modifier = Modifier.offset(x = 150.dp, y = 55.dp),
        )

        Box(
            modifier = Modifier
                .offset(x = 373.dp, y = 64.dp)
                .size(20.dp)
                .clickable { onNotificationsClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notif),
                contentDescription = "Уведомления",
                tint = HeaderBlue,
                modifier = Modifier.size(20.dp),
            )

            if (hasNotifications) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 3.dp, y = (-1).dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                )
            }
        }
    }
}

@Composable
private fun InfoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .border(1.dp, AccentBlue, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "i",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = AccentBlue,
        )
    }
}

@Composable
private fun SummaryRow(
    deadlineText: String,
    membersCount: Int,
    tasksCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Срок сдачи: $deadlineText",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = Color.Black,
        )

        SmallDot()

        Text(
            text = "$membersCount Участников",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = AccentBlue,
        )

        SmallDot()

        Text(
            text = "$tasksCount Задач",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = AccentBlue,
        )
    }
}

@Composable
private fun SmallDot() {
    Spacer(modifier = Modifier.width(8.dp))
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(DotGray),
    )
    Spacer(modifier = Modifier.width(8.dp))
}

@Composable
private fun AnimatedProjectTabsHeader(
    selectedTab: ProjectTab,
    onTabSelected: (ProjectTab) -> Unit,
) {
    val sliderOffset by animateDpAsState(
        targetValue = if (selectedTab == ProjectTab.TASKS) 0.dp else 163.dp,
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
            .offset(x = 6.dp, y = 11.dp)
            .width(327.dp)
            .height(39.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(18.dp))
            .background(Color.White),
    ) {
        Box(
            modifier = Modifier
                .offset(x = sliderOffset, y = 0.dp)
                .width(164.dp)
                .height(39.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SliderBlue),
        )

        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = 0.dp)
                .width(164.dp)
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
                .offset(x = 163.dp, y = 0.dp)
                .width(164.dp)
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
) {
    Box(
        modifier = Modifier
            .width(42.dp)
            .height(39.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_filter),
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
        modifier = modifier.width(284.dp),
        thickness = 1.dp,
        color = BorderGray,
    )
}

@Composable
private fun ExactTasksBody(
    uiState: ProjectUiState,
    onTaskClick: (Long) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Текущие (${uiState.currentTasks.size})",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 20.sp,
            color = Color.Black,
            modifier = Modifier.offset(x = 25.dp, y = 69.dp),
        )

        Text(
            text = "Все “Текущие”",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = AccentBlue,
            modifier = Modifier.offset(x = 248.dp, y = 69.dp),
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_down_arrow),
            contentDescription = "Все текущие",
            tint = AccentBlue,
            modifier = Modifier
                .offset(x = 343.dp, y = 68.dp)
                .size(24.dp)
                .rotate(-90f),
        )

        val current = uiState.currentTasks.take(3)
        val review = uiState.reviewTasks.take(3)

        if (current.isNotEmpty()) {
            ExactTaskRow(
                task = current[0],
                muted = false,
                width = 202.dp,
                modifier = Modifier.offset(x = 32.dp, y = 103.dp),
                onClick = { onTaskClick(current[0].id) },
            )
        }

        DividerExact(modifier = Modifier.offset(x = 50.dp, y = 160.dp))

        if (current.size > 1) {
            ExactTaskRow(
                task = current[1],
                muted = false,
                width = 275.dp,
                modifier = Modifier.offset(x = 32.dp, y = 177.dp),
                onClick = { onTaskClick(current[1].id) },
            )
        }

        DividerExact(modifier = Modifier.offset(x = 50.dp, y = 234.dp))

        if (current.size > 2) {
            ExactTaskRow(
                task = current[2],
                muted = false,
                width = 291.dp,
                modifier = Modifier.offset(x = 32.dp, y = 251.dp),
                onClick = { onTaskClick(current[2].id) },
            )
        }

        DividerExact(modifier = Modifier.offset(x = 50.dp, y = 308.dp))

        Text(
            text = "На проверке (${uiState.reviewTasks.size})",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 20.sp,
            color = Color.Black,
            modifier = Modifier.offset(x = 25.dp, y = 325.dp),
        )

        if (review.isNotEmpty()) {
            ExactTaskRow(
                task = review[0],
                muted = true,
                width = 265.dp,
                modifier = Modifier.offset(x = 32.dp, y = 359.dp),
                onClick = { onTaskClick(review[0].id) },
            )
        }

        DividerExact(modifier = Modifier.offset(x = 50.dp, y = 416.dp))

        if (review.size > 1) {
            ExactTaskRow(
                task = review[1],
                muted = true,
                width = 291.dp,
                modifier = Modifier.offset(x = 32.dp, y = 433.dp),
                onClick = { onTaskClick(review[1].id) },
            )
        }

        DividerExact(modifier = Modifier.offset(x = 50.dp, y = 490.dp))

        if (review.size > 2) {
            ExactTaskRow(
                task = review[2],
                muted = true,
                width = 275.dp,
                modifier = Modifier.offset(x = 32.dp, y = 507.dp),
                onClick = { onTaskClick(review[2].id) },
            )
        }
    }
}

@Composable
private fun ExactTaskRow(
    task: ProjectTaskItemUi,
    muted: Boolean,
    width: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
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

    Box(
        modifier = modifier
            .width(width)
            .height(40.dp)
            .clickable { onClick() },
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
            modifier = Modifier.offset(x = 0.dp, y = 0.dp),
        )

        Text(
            text = task.priority.title,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = priorityColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.offset(x = 0.dp, y = 20.dp),
        )

        val deadlineStart = when (task.deadline) {
            "22.02.26" -> 150.dp
            "25.02.26" -> 150.dp
            "28.02.26" -> 140.dp
            "08.03.26" -> 140.dp
            "15.03.26" -> 140.dp
            else -> 140.dp
        }

        Box(
            modifier = Modifier
                .offset(x = deadlineStart - 8.dp, y = 28.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(dotColor),
        )

        Text(
            text = task.deadline,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = metaColor,
            modifier = Modifier.offset(x = deadlineStart, y = 20.dp),
        )
    }
}

@Composable
private fun MembersBodySimple(
    uiState: ProjectUiState,
    onMemberClick: (Long) -> Unit,
    onInviteMemberClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Участник",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            color = AccentBlue,
            modifier = Modifier.offset(x = 40.dp, y = 69.dp),
        )

        Text(
            text = "Должность",
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            color = AccentBlue,
            modifier = Modifier.offset(x = 240.dp, y = 69.dp),
        )

        val topPositions = listOf(134.dp, 205.dp, 276.dp, 347.dp, 418.dp)
        val dividerPositions = listOf(182.dp, 253.dp, 324.dp, 395.dp, 466.dp)

        uiState.members.take(5).forEachIndexed { index, member ->
            Row(
                modifier = Modifier
                    .offset(x = 30.dp, y = topPositions[index])
                    .width(320.dp)
                    .height(25.dp)
                    .clickable { onMemberClick(member.id) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = member.fullName,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    color = DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(165.dp),
                )

                if (member.isOwner) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.crown),
                        contentDescription = "Владелец",
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.Fit,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = member.position,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFF70717A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .offset(x = 51.dp, y = dividerPositions[index])
                    .width(289.dp),
                thickness = 1.dp,
                color = BorderGray,
            )
        }

        if (uiState.isOwner) {
            Row(
                modifier = Modifier
                    .offset(x = 69.dp, y = 489.dp)
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