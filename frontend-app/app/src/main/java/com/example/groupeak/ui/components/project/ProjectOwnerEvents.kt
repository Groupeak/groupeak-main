package com.example.groupeak.ui.components.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.screens.project_for_owner.ProjectEventItemUi
import com.example.groupeak.ui.theme.OnestFontFamily

private val AccentBlue = Color(0xFF3988FE)
private val BorderGray = Color(0xFFB0B3C7)
private val DotGray = Color(0xFFD9D9D9)
private val DarkText = Color(0xFF333B46)

@Composable
fun EventsBody(
    events: List<ProjectEventItemUi>,
    onEventClick: (ProjectEventItemUi) -> Unit = {},
    onEditEvent: (Long) -> Unit = {},
    onDeleteEvent: (Long) -> Unit = {},
) {
    val scrollState = rememberScrollState()

    // Группируем мероприятия по датам
    val groupedEvents = events.groupBy { it.date }

    if (groupedEvents.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 69.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Нет мероприятий",
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 36.sp,
                lineHeight = 40.sp,
                color = Color(0xFF70717A),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 71.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
            .verticalScroll(scrollState),
    ) {
        groupedEvents.forEach { (date, dateEvents) ->
            // Заголовок даты
            Text(
                text = date,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 20.sp,
                letterSpacing = (-0.4).sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Карточки мероприятий для этой даты
            dateEvents.forEachIndexed { index, event ->
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) },
                    onEdit = { onEditEvent(event.id) },
                    onDelete = { onDeleteEvent(event.id) },
                )

                if (index != dateEvents.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}

@Composable
fun EventCard(
    event: ProjectEventItemUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    val isLongTitle = event.title.length > 35
    val cardHeight = if (isLongTitle) 89.dp else 69.dp
    val lineHeight = if (isLongTitle) 59.dp else 40.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .background(Color(0xFFFEFEFE), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 15.dp, horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = event.time,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
                color = Color.Black,
                modifier = Modifier.width(48.dp),
            )

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(lineHeight)
                    .background(Color(0xFF3988FE), RoundedCornerShape(18.dp))
            )

            Text(
                text = event.title,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = (-0.4).sp,
                color = Color(0xFF1876FF),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 15.dp, end = 8.dp),
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_edit),
                contentDescription = "Редактировать",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onEdit() },
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_trash_red),
                contentDescription = "Удалить",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDelete() },
            )
        }
    }
}
