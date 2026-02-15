package com.example.groupeak.ui.screens.notifications

import android.R.attr.color
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults.color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDefaults.color
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.components.NotificationInfoDialog
import com.example.groupeak.ui.components.NotificationInviteDialog
import com.example.groupeak.ui.components.NotificationItem
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.OnestFontFamily

/* ---------- HEADER ---------- */
@Composable
private fun NotificationHeader(
    title: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 55.dp)
    ) {
        androidx.compose.material3.Icon(
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
    }
}

/* ---------- SCREEN ---------- */
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    var selectedTab by remember { mutableStateOf(Tab.ALL) }
    var selectedNotification by remember { mutableStateOf<NotificationData?>(null) }

    val notifications = remember { mockNotifications }

    GroupeakBackground {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NotificationHeader(
                title = "Уведомления",
                onBackClick = onBack
            )

            AnimatedSegmentedTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            val listToShow = when (selectedTab) {
                Tab.ALL -> notifications
                Tab.UNREAD -> notifications.filter { !it.isRead }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (listToShow.isEmpty()) {
                    Text(
                        text = "Нет уведомлений",
                        fontSize = 18.sp,
                        fontFamily = OnestFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.width(361.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(listToShow) { item ->
                            NotificationItem(
                                title = item.title,
                                subtitle = item.subtitle,
                                project = item.project,
                                date = item.date,
                                isRead = item.isRead,
                                onClick = {
                                    item.isRead = true
                                    selectedNotification = item
                                },
                                //modifier = Modifier.padding(bottom = 20.dp),
                                onDismiss = { item.isRead = true }
                            )
                        }
                    }
                }
            }
        }
    }

    /* ---------- DIALOG CONNECTION ---------- */
    selectedNotification?.let { notification ->
        when (notification.type) {
            NotificationType.INFO -> {
                NotificationInfoDialog(
                    notification = notification,
                    onDismiss = { selectedNotification = null }
                )
            }

            NotificationType.INVITE -> {
                NotificationInviteDialog(
                    notification = notification,
                    onAccept = {
                        // TODO: логика принятия приглашения
                        selectedNotification = null
                    },
                    onDecline = {
                        // TODO: логика отклонения
                        selectedNotification = null
                    },
                    onDismiss = { selectedNotification = null }
                )
            }
        }
    }
}

/* ---------- ANIMATED TABS ---------- */
@Composable
private fun AnimatedSegmentedTabs(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit
) {
    val tabWidth = 361.dp / 2

    val offsetX by animateDpAsState(
        targetValue = if (selectedTab == Tab.ALL) 0.dp else tabWidth,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "tabOffset"
    )

    Box(
        modifier = Modifier
            .padding(top = 16.dp)
            .width(361.dp)
            .height(39.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .width(tabWidth)
                .fillMaxHeight()
                .padding(2.dp)
                .background(
                    color = Color(0xFF71AAFF),
                    shape = RoundedCornerShape(18.dp)
                )
        )

        Row(modifier = Modifier.fillMaxSize()) {
            TabText(
                text = "Все уведомления",
                selected = selectedTab == Tab.ALL,
                onClick = { onTabSelected(Tab.ALL) },
                modifier = Modifier.weight(1f)
            )
            TabText(
                text = "Непрочитанные",
                selected = selectedTab == Tab.UNREAD,
                onClick = { onTabSelected(Tab.UNREAD) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabText(
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
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* ---------- DATA ---------- */
enum class NotificationType { INFO, INVITE }
enum class Tab { ALL, UNREAD }

data class NotificationData(
    val title: String,
    val subtitle: String,
    val project: String,
    val date: String,
    val description: String,
    val role: String? = null,
    val type: NotificationType,
    var isRead: Boolean = false
)

/* ---------- MOCK ---------- */
private val mockNotifications = mutableListOf(
    NotificationData(
        title = "Подсоединить базу данных к backend",
        subtitle = "Уведомление о дедлайне",
        project = "BBWatch",
        date = "03.02.26",
        description = "Необходимо завершить интеграцию базы данных с серверной частью до конца недели.",
        type = NotificationType.INFO,
        isRead = true
    ),
    NotificationData(
        title = "Приглашение в проект",
        subtitle = "Вас пригласили",
        project = "LinUX",
        date = "02.02.26",
        description = "Команда LinUX приглашает вас присоединиться к разработке нового модуля интерфейса.",
        role = "Frontend Developer",
        type = NotificationType.INVITE
    ),
    NotificationData(
        title = "Демонстрация дизайна главной страницы...",
        subtitle = "Срок задачи по дизайну",
        project = "Peak.Group",
        date = "01.02.26",
        description = "Дизайн-команда готова представить макеты главной страницы. Просьба присутствовать на демо.",
        type = NotificationType.INFO
    )
)