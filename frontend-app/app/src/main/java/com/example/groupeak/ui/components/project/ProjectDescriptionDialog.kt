package com.example.groupeak.ui.components.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.groupeak.R
import com.example.groupeak.ui.theme.OnestFontFamily

private val AccentBlue = Color(0xFF3988FE)
private val BorderGray = Color(0xFFB0B3C7)
private val DarkText = Color(0xFF333B46)

private val DialogWidth = 348.dp
private val FieldWidth = 328.dp
private val FieldHeight = 55.dp
private val DescriptionFieldHeight = 202.dp

@Composable
fun ProjectDescriptionDialog(
    visible: Boolean,
    projectName: String,
    deadline: String,
    description: String,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(DialogWidth)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(horizontal = 10.dp, vertical = 18.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Описание проекта",
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    color = AccentBlue,
                )

                Spacer(modifier = Modifier.height(14.dp))

                ReadOnlyProjectField(
                    label = "Название проекта",
                    iconRes = R.drawable.ic_folder,
                    value = projectName,
                )

                Spacer(modifier = Modifier.height(12.dp))

                ReadOnlyProjectField(
                    label = "Дедлайн",
                    iconRes = R.drawable.ic_calendar,
                    value = deadline,
                )

                Spacer(modifier = Modifier.height(12.dp))

                ReadOnlyDescriptionField(
                    label = "Описание",
                    value = description,
                )
            }
        }
    }
}

@Composable
private fun ReadOnlyProjectField(
    label: String,
    iconRes: Int,
    value: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            color = AccentBlue,
            modifier = Modifier.padding(start = 11.dp),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .requiredWidth(FieldWidth)
                .requiredHeight(FieldHeight)
                .border(1.dp, BorderGray, RoundedCornerShape(18.dp))
                .padding(start = 11.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(22.dp),
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = value,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = DarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ReadOnlyDescriptionField(
    label: String,
    value: String,
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            color = AccentBlue,
            modifier = Modifier.padding(start = 11.dp),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .requiredWidth(FieldWidth)
                .requiredHeight(DescriptionFieldHeight)
                .border(1.dp, BorderGray, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = descriptionText(value),
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = DarkText,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            )
        }
    }
}

private fun descriptionText(value: String): String {
    return if (value.isBlank()) "Описание проекта отсутствует" else value
}