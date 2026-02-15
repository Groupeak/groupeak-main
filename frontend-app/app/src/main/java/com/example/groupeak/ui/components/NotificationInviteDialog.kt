package com.example.groupeak.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.groupeak.R
import com.example.groupeak.ui.screens.notifications.NotificationData
import com.example.groupeak.ui.theme.OnestFontFamily
import kotlinx.coroutines.delay

private val White = Color(0xFFFFFFFF)

@Composable
fun NotificationInviteDialog(
    notification: NotificationData,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit,
) {

    var showMessage by rememberSaveable { mutableStateOf(false) }
    var messageKind by rememberSaveable { mutableStateOf(0) }
    var messagePulse by rememberSaveable { mutableStateOf(0) }

    val messageText =
        if (messageKind == 0) "Приглашение принято"
        else "Приглашение отклонено"

    val messageColor =
        if (messageKind == 0) Color(0xFF26BC30)
        else Color(0xFFFF0000)

    val messageAlpha by animateFloatAsState(
        targetValue = if (showMessage) 1f else 0f,
        animationSpec = tween(250),
        label = "messageAlpha"
    )

    val messageSlide by animateDpAsState(
        targetValue = if (showMessage) 0.dp else (-8).dp,
        animationSpec = tween(250),
        label = "messageSlide"
    )

    LaunchedEffect(messagePulse) {
        if (messagePulse == 0) return@LaunchedEffect

        showMessage = true
        delay(3000)
        showMessage = false
        delay(250)

        onDismiss()

        if (messageKind == 0) onAccept()
        else onDecline()
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {

        // 🔥 Фиксируем диалог сверху
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp), // отступ сверху (можешь изменить)
            contentAlignment = Alignment.TopCenter
        ) {

            Column(
                modifier = Modifier
                    .width(340.dp) // точная ширина по макету
                    .background(White, RoundedCornerShape(24.dp))
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = 300f
                        )
                    )
            ) {

                NotificationCard {

                    InfoField(
                        label = "Проект",
                        value = notification.project,
                        icon = R.drawable.ic_project
                    )

                    Spacer(Modifier.height(12.dp))

                    InfoField(
                        label = "Должность",
                        value = notification.role ?: "",
                        icon = R.drawable.ic_role
                    )

                    Spacer(Modifier.height(12.dp))

                    DescriptionBlock(
                        title = "Описание проекта",
                        text = notification.description
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            onClick = {
                                messageKind = 0
                                messagePulse += 1
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3988FE),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("Принять", fontSize = 14.sp)
                        }

                        OutlinedButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            onClick = {
                                messageKind = 1
                                messagePulse += 1
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF3988FE)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF3988FE)),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("Отклонить", fontSize = 14.sp)
                        }
                    }

                    if (showMessage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .offset(y = messageSlide)
                                .graphicsLayer { alpha = messageAlpha },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = messageText,
                                fontSize = 14.sp,
                                fontFamily = OnestFontFamily,
                                color = messageColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
