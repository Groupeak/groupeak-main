package com.example.groupeak.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.GroupeakDarkBlue
import com.example.groupeak.ui.theme.GroupeakLightGray
import com.example.groupeak.ui.theme.Gray70717A
import com.example.groupeak.ui.theme.OnestFontFamily

private val CardWidth = 348.dp
private val CardHeight = 530.dp
private val DividerWidth = 250.dp

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAllTasksClick: () -> Unit,
    onProjectClick: (Long, Boolean) -> Unit,
) {
    GroupeakBackground {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar(
                avatarUrl = uiState.avatarUrl,
                hasNotifications = uiState.hasNotifications,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
            )

            Text(
                text = "Добрый день!",
                fontSize = 24.sp,
                lineHeight = 31.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 131.dp)
            )

            HomeCard(
                upcomingTasks = uiState.upcomingTasks,
                projects = uiState.projects,
                onAllTasksClick = onAllTasksClick,
                onProjectClick = onProjectClick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 197.dp)
            )
        }
    }
}

@Composable
private fun TopBar(
    avatarUrl: String?,
    hasNotifications: Boolean,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 55.dp, bottom = 58.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 11.dp)
                .size(40.dp)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            ProfileAvatar(
                avatarUrl = avatarUrl,
                modifier = Modifier.size(40.dp)
            )
        }

        Text(
            text = "Главная",
            fontSize = 32.sp,
            lineHeight = 41.sp,
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
                .size(28.dp)
                .clickable { onNotificationsClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notif),
                contentDescription = "Уведомления",
                tint = BluePrimary300,
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
private fun ProfileAvatar(
    avatarUrl: String?,
    modifier: Modifier = Modifier,
) {
    if (!avatarUrl.isNullOrBlank()) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "Аватар профиля",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.im_avatar),
            error = painterResource(id = R.drawable.im_avatar),
            fallback = painterResource(id = R.drawable.im_avatar),
            modifier = modifier
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = GroupeakLightGray.copy(alpha = 0.35f),
                    shape = CircleShape
                )
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.im_avatar),
            contentDescription = "Аватар профиля",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = GroupeakLightGray.copy(alpha = 0.35f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun HomeCard(
    upcomingTasks: List<HomeTaskUi>,
    projects: List<HomeProjectUi>,
    onAllTasksClick: () -> Unit,
    onProjectClick: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(CardWidth)
            .height(CardHeight)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ближайшие задачи",
                fontSize = 16.sp,
                lineHeight = 19.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = BluePrimary300
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.clickable { onAllTasksClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Все задачи",
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = BluePrimary300
                )

                Spacer(modifier = Modifier.width(3.dp))

                Icon(
                    painter = painterResource(id = R.drawable.ic_down_arrow),
                    contentDescription = "Все задачи",
                    tint = BluePrimary300,
                    modifier = Modifier
                        .size(23.dp)
                        .rotate(-90f)
                )
            }
        }

        upcomingTasks.take(3).forEach { task ->
            HomeTaskRow(task = task)
            SectionDivider()
        }

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Проекты",
            fontSize = 16.sp,
            lineHeight = 19.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = BluePrimary300,
            modifier = Modifier.padding(start = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(projects) { index, project ->
                ProjectRow(
                    project = project,
                    onClick = { onProjectClick(project.id, project.isOwner) }
                )

                if (index != projects.lastIndex) {
                    SectionDivider()
                }
            }
        }
    }
}

@Composable
private fun HomeTaskRow(
    task: HomeTaskUi,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 37.dp, end = 37.dp, top = 17.dp, bottom = 17.dp)
    ) {
        Text(
            text = task.title,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(1.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.projectName,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = Gray70717A
            )

            DotSeparator()

            Text(
                text = task.deadline,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Gray70717A
            )
        }
    }
}

@Composable
private fun ProjectRow(
    project: HomeProjectUi,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 41.dp, end = 41.dp, top = 17.dp, bottom = 17.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = project.title,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = GroupeakDarkBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (project.isOwner) {
                Spacer(modifier = Modifier.width(6.dp))
                Image(
                    painter = painterResource(id = R.drawable.crown),
                    contentDescription = "Владелец проекта",
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = project.completedCount.toString(),
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF26BC30)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "Выполнено",
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = GroupeakDarkBlue
            )

            DotSeparator()

            Text(
                text = project.overdueCount.toString(),
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = Color.Red
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "Просрочено",
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = GroupeakDarkBlue
            )
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier
            .padding(start = 49.dp)
            .width(DividerWidth),
        color = GroupeakLightGray.copy(alpha = 0.7f)
    )
}

@Composable
private fun DotSeparator() {
    Spacer(modifier = Modifier.width(8.dp))
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(Color(0xFFD9D9D9))
    )
    Spacer(modifier = Modifier.width(8.dp))
}