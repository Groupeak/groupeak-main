package com.example.groupeak.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.theme.*
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle

@Composable
fun EventsScreen(
    uiState: EventsUiState,
    onBackClick: () -> Unit,
    onEventClick: (EventUiModel) -> Unit,
    onNotificationsClick: () -> Unit,
    onDismissDialog: () -> Unit
) {
    GroupeakBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Шапка
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 55.dp, bottom = 12.dp)
            ) {
                Icon(
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
                    text = "Мероприятия",
                    fontSize = 32.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF1876FF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 8.dp)
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
                        tint = Color(0xFF1876FF),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Пустое состояние или список
            if (uiState.events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 338.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "Нет мероприятий",
                        fontSize = 32.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF7071FA),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Список мероприятий
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    val groupedEvents = uiState.events.groupBy { it.date }

                    groupedEvents.forEach { (date, events) ->
                        item {
                            Text(
                                text = date,
                                fontSize = 20.sp,
                                fontFamily = OnestFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 8.dp, start = 13.dp, bottom = 7.dp)
                            )
                        }

                        itemsIndexed(events) { index, event ->
                            EventCard(
                                event = event,
                                onClick = { onEventClick(event) }
                            )

                            Spacer(
                                Modifier.height(
                                    if (index == events.lastIndex) 33.dp
                                    else 16.dp
                                )
                            )
                        }
                    }

                    // Отступ снизу для нижней панели
                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }

        uiState.selectedEvent?.let { event ->
            EventDetailDialog(
                event = event,
                onDismiss = onDismissDialog
            )
        }
    }
}

@Composable
private fun EventCard(
    event: EventUiModel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = event.time,
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.width(40.dp)
            )

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF3988FE), RoundedCornerShape(18.dp))
                    .padding(horizontal = 13.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp)
            ) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1876FF),
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = event.project,
                    fontSize = 15.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    lineHeight = 20.sp
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_event_arrow),
                contentDescription = "Открыть",
                tint = Color(0xFF3988FE),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EventDetailDialog(
    event: EventUiModel,
    onDismiss: () -> Unit
) {
    var showParticipantsDialog by remember { mutableStateOf(false) }
    // Высота 632.dp с ссылкой, 545.dp без ссылки
    val dialogHeight = if (event.link != null) 632.dp else 545.dp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(348.dp)
                .height(dialogHeight)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 16.dp)
                ) {
                    EventDetailField(
                        label = "Название",
                        value = event.title,
                        iconRes = R.drawable.ic_task_name
                    )
                    Spacer(Modifier.height(11.dp))
                    EventDetailField(
                        label = "Проект",
                        value = event.project,
                        iconRes = R.drawable.ic_folder
                    )
                    Spacer(Modifier.height(11.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        EventDetailField(
                            label = "Дата",
                            value = event.fullDate,
                            iconRes = R.drawable.ic_calendar,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        EventDetailField(
                            label = "Время",
                            value = event.time,
                            iconRes = R.drawable.ic_clock,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(11.dp))
                    ParticipantsField(
                        label = "Участники",
                        participants = event.participants,
                        iconRes = R.drawable.ic_members,
                        onClick = { showParticipantsDialog = true }
                    )
                    Spacer(Modifier.height(11.dp))
                    if (event.link != null) {
                        val uriHandler = LocalUriHandler.current
                        EventDetailLinkField(
                            label = "Ссылка",
                            value = event.link,
                            iconRes = R.drawable.ic_link,
                            onClick = { uriHandler.openUri(event.link) }
                        )
                        Spacer(Modifier.height(11.dp))
                    }
                    Text(
                        text = "Описание",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = BluePrimary300,
                        modifier = Modifier.padding(start = 11.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(117.dp)
                            .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(11.dp)
                    ) {
                        Text(
                            text = event.description ?: "Описание не указано",
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            color = Color(0xFF333B46),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    if (showParticipantsDialog) {
        ParticipantsDialog(
            participants = event.participants,
            onDismiss = { showParticipantsDialog = false }
        )
    }
}

// Поле участников со стрелкой (кликабельное)
@Composable
private fun ParticipantsField(
    label: String,
    participants: List<Participant>,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Умное формирование текста с "и еще n"
    val displayText = buildAnnotatedString {
        if (participants.isEmpty()) {
            append("Нет участников")
        } else if (participants.size == 1) {
            append(participants[0].name)
        } else if (participants.size == 2) {
            append("${participants[0].name}, ${participants[1].name}")
        } else {
            // Показываем первые 2 имени + "и еще n" полужирным
            append("${participants[0].name}, ${participants[1].name} ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                append("и еще ${participants.size - 2}")
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = BluePrimary300,
            modifier = Modifier.padding(start = 11.dp)
        )

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                .background(Color.White, RoundedCornerShape(18.dp))
                .clickable { onClick() }
                .padding(start = 11.dp, end = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = BluePrimary300,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = displayText,
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,
                color = Color(0xFF333B46),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_down_arrow),
                contentDescription = "Открыть список",
                tint = BluePrimary300,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ParticipantsDialog(
    participants: List<Participant>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(348.dp)
                .height(495.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Участники",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = OnestFontFamily,
                        color = BluePrimary300,
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                            .align(Alignment.CenterHorizontally)
                    )


                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(405.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        participants.forEachIndexed { index, participant ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 23.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = participant.name,
                                    fontSize = 20.sp,
                                    fontFamily = OnestFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = GroupeakDarkBlue,
                                    modifier = Modifier
                                        .width(140.dp)
                                        .padding(start = 9.dp)
                                )

                                if (participant.role.isNotEmpty()) {
                                    Text(
                                        text = participant.role,
                                        fontSize = 20.sp,
                                        fontFamily = OnestFontFamily,
                                        color = Gray650,
                                        modifier = Modifier
                                            .weight(1f)
                                            .wrapContentWidth(Alignment.CenterHorizontally)
                                    )
                                }
                            }

                            if (index != participants.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFB0B3C7))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventDetailField(
    label: String,
    value: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = BluePrimary300,
            modifier = Modifier.padding(start = 11.dp)
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                .background(Color.White, RoundedCornerShape(18.dp))
                .padding(start = 11.dp, end = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = BluePrimary300,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,
                color = Color(0xFF333B46),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EventDetailLinkField(
    label: String,
    value: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = BluePrimary300,
            modifier = Modifier.padding(start = 11.dp)
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                .background(Color.White, RoundedCornerShape(18.dp))
                .clickable { onClick() }
                .padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = BluePrimary300,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,
                color = Color(0xFF3988FE),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}