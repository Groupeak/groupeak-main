package com.example.groupeak.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.theme.OnestFontFamily
import com.example.groupeak.ui.theme.NotificationsGray

@Composable
fun NotificationItem(
    title: String,
    subtitle: String,
    project: String,
    date: String,
    isRead: Boolean,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Весь элемент — это Column
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Основной контент
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .background(Color(0xFFFEF4CB), shape = CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_notification_doc),
                    contentDescription = "Документ",
                    modifier = Modifier
                        .size(55.dp)
                        .align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Box(modifier = Modifier.height(20.dp), contentAlignment = Alignment.TopStart) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = OnestFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black,
                        letterSpacing = (-0.4f / 15f).em,
                        lineHeight = 20.sp
                    )
                }
                Box(modifier = Modifier.height(20.dp), contentAlignment = Alignment.TopStart) {
                    Text(
                        text = subtitle,
                        fontSize = 15.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = NotificationsGray,
                        lineHeight = 20.sp,
                        letterSpacing = (-0.4f / 15f).em
                    )
                }
                Box(modifier = Modifier.height(20.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "Проект: $project",
                            fontSize = 15.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black,
                            letterSpacing = (-0.4f / 15f).em,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• $date",
                            fontSize = 15.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = NotificationsGray,
                            letterSpacing = (-0.4f / 12f).em,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.26.dp)
                .background(Color.Black.copy(alpha = 0.1f))
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}