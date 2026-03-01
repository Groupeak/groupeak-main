package com.example.groupeak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R

private val Blue3988FE = Color(0xFF3988FE)
private val Gray70717A = Color(0xFF70717A)
private val Divider969696 = Color(0xFF969696)
private val BorderB0B3C7 = Color(0xFFB0B3C7)

private val WindowWidth = 352.dp
private val WindowHeight = 47.dp

@Composable
fun CreateBottomSheetContent(
    onCreateProjectClick: () -> Unit,
    onCreateTaskClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 412.dp)
            .padding(top = 30.dp, bottom = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Создать",
            color = Blue3988FE,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(13.dp))

        CreateBottomSheetItem(
            iconRes = R.drawable.ic_folder,
            title = "Проект",
            subtitle = "Название, дедлайн, описание",
            onClick = onCreateProjectClick
        )

        Spacer(modifier = Modifier.height(22.dp))

        CreateBottomSheetItem(
            iconRes = R.drawable.ic_checklist,
            title = "Задача",
            subtitle = "Название, дедлайн, приоритет, описание",
            onClick = onCreateTaskClick
        )
    }
}

@Composable
private fun CreateBottomSheetItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)

    val titleStyle = TextStyle(
        color = Blue3988FE,
        fontSize = 16.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal
    )
    val subtitleStyle = TextStyle(
        color = Gray70717A,
        fontSize = 12.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Normal
    )

    Box(
        modifier = Modifier
            .size(WindowWidth, WindowHeight)
            .border(1.dp, BorderB0B3C7, shape)
            .clickable(onClick = onClick)
    ) {
        Layout(
            content = {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Blue3988FE,
                    modifier = Modifier.size(32.dp)
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(35.dp)
                        .background(Divider969696)
                )

                Text(text = title, style = titleStyle)
                Text(text = subtitle, style = subtitleStyle)
            }
        ) { measurables, constraints ->
            val width = constraints.maxWidth
            val height = constraints.maxHeight

            val iconP = measurables[0].measure(constraints.copy(minWidth = 0, minHeight = 0))
            val dividerP = measurables[1].measure(constraints.copy(minWidth = 0, minHeight = 0))
            val titleP = measurables[2].measure(constraints.copy(minWidth = 0, minHeight = 0))
            val subtitleP = measurables[3].measure(constraints.copy(minWidth = 0, minHeight = 0))

            val xIcon = 16.dp.roundToPx()
            val yIcon = (height - iconP.height) / 2

            val xDivider = 59.dp.roundToPx()
            val yDivider = (height - dividerP.height) / 2

            val xText = 71.dp.roundToPx()
            val yTitle = 5.dp.roundToPx()

            val gap = 3.dp.roundToPx()
            val ySubtitle = yTitle + titleP.height + gap

            layout(width, height) {
                iconP.placeRelative(x = xIcon, y = yIcon)
                dividerP.placeRelative(x = xDivider, y = yDivider)
                titleP.placeRelative(x = xText, y = yTitle)
                subtitleP.placeRelative(x = xText, y = ySubtitle)
            }
        }
    }
}