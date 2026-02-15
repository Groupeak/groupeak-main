package com.example.groupeak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.groupeak.R
import com.example.groupeak.ui.screens.notifications.NotificationData

private val Blue = Color(0xFF3988FE)
private val White = Color(0xFFFFFFFF)
private val Gray = Color(0xFFB0B3C7)
private val TextDark = Color(0xFF333B46)

@Composable
fun NotificationInfoDialog(
    notification: NotificationData,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(
        usePlatformDefaultWidth = false
    )) {
        Column(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .background(White, RoundedCornerShape(24.dp))
                .padding(12.dp)
        ) {

            InfoField(
                label = "Проект",
                value = notification.project,
                icon = R.drawable.ic_project
            )

            Spacer(Modifier.height(16.dp))

            InfoField(
                label = "Тема уведомления",
                value = notification.title,
                icon = R.drawable.ic_mail
            )

            Spacer(Modifier.height(12.dp))

            DescriptionBlock(
                title = "Описание",
                text = notification.description
            )
        }
    }
}


