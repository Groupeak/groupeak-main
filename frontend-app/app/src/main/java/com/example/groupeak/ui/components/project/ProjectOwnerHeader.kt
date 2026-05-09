package com.example.groupeak.ui.components.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.screens.project_for_owner.ProjectOwnerTab
import com.example.groupeak.ui.theme.OnestFontFamily

private val HeaderBlue = Color(0xFF1876FF)
private val AccentBlue = Color(0xFF3988FE)
private val SliderBlue = Color(0xFF71AAFF)
private val BorderGray = Color(0xFFB0B3C7)
private val DotGray = Color(0xFFD9D9D9)
private val DarkText = Color(0xFF333B46)
private const val ANIM_DURATION = 320

@Composable
fun ProjectTopBar(
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
fun ProjectTitleBlock(
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
fun SummaryRow(
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
                color = Color.Black,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun AnimatedProjectTabsHeader(
    selectedTab: ProjectOwnerTab,
    onTabSelected: (ProjectOwnerTab) -> Unit,
) {
    val isTasks = selectedTab == ProjectOwnerTab.TASKS
    val tabIndex = selectedTab.ordinal

    val targetWidth = if (isTasks) 327.dp else 367.dp
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

    val tabWidth = animatedWidth / 3
    val sliderOffset = tabWidth * tabIndex

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
        Box(
            modifier = Modifier
                .offset(x = sliderOffset)
                .width(tabWidth)
                .height(39.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SliderBlue),
        )

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
fun FilterButtonExact(
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
