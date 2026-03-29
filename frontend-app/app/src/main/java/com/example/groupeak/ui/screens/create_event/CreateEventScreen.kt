package com.example.groupeak.ui.screens.create_event

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.components.PrimaryButton
import com.example.groupeak.ui.theme.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.style.TextOverflow

// VisualTransformation для даты (ДД.ММ.ГГГГ)
private object DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter { it.isDigit() }.take(8)
        val out = buildString {
            for (i in raw.indices) {
                append(raw[i])
                if (i == 1 || i == 3) append('.')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, raw.length)
                var t = o
                if (raw.length > 2 && o > 2) t += 1
                if (raw.length > 4 && o > 4) t += 1
                return t.coerceIn(0, out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceIn(0, out.length)
                var o = t
                if (raw.length > 2 && t > 2) o -= 1
                if (raw.length > 4 && t > 5) o -= 1
                return o.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

// VisualTransformation для времени (ЧЧ:ММ)
private object TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter { it.isDigit() }.take(4)
        val out = buildString {
            for (i in raw.indices) {
                append(raw[i])
                if (i == 1) append(':')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, raw.length)
                var t = o
                if (raw.length > 2 && o > 2) t += 1
                return t.coerceIn(0, out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceIn(0, out.length)
                var o = t
                if (raw.length > 2 && t > 2) o -= 1
                return o.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

// Форматирование участников
private fun formatParticipantsText(participants: String, placeholder: String): AnnotatedString {
    val list = participants.split(", ").filter { it.isNotBlank() }
    return when {
        list.isEmpty() -> AnnotatedString(placeholder)
        list.size == 1 -> AnnotatedString(list[0])
        list.size == 2 -> AnnotatedString("${list[0]}, ${list[1]}")
        else -> {
            buildAnnotatedString {
                append("${list[0]}, ${list[1]} ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("и еще ${list.size - 2}")
                }
            }
        }
    }
}

@Composable
fun CreateEventScreen(
    uiState: CreateEventUiState,
    onBackClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onProjectChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onParticipantsChange: (String) -> Unit,
    onLinkChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    var showProjectDialog by remember { mutableStateOf(false) }
    var showParticipantsDialog by remember { mutableStateOf(false) }

    GroupeakBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Шапка
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 55.dp, bottom = 23.dp)
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
                    text = "Создание мероприятия",
                    fontSize = 27.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = BluePrimary300,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 40.dp)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 19.dp)
                        .size(28.dp)
                        .clickable { /* onNotificationsClick */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notif),
                        contentDescription = "Уведомления",
                        tint = BluePrimary300,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Белый блок с LazyColumn для скролла
            Box(
                modifier = Modifier
                    .width(348.dp)
                    .height(675.dp)
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .padding(top = 20.dp, start = 10.dp, end = 10.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    item {
                        // Название
                        InputFieldWithError(
                            label = "Название",
                            iconRes = R.drawable.ic_task_name,
                            value = uiState.title,
                            onValueChange = onTitleChange,
                            placeholder = "Название мероприятия",
                            error = uiState.titleError
                        )

                        Spacer(Modifier.height(11.dp))

                        // Проект
                        SelectionFieldWithError(
                            label = "Проект",
                            iconRes = R.drawable.ic_project,
                            selectedText = uiState.project,
                            placeholder = "Выберите проект для мероприятия",
                            error = null,
                            onClick = { showProjectDialog = true }
                        )

                        Spacer(Modifier.height(11.dp))

                        // Дата и время
                        Row(modifier = Modifier.fillMaxWidth()) {
                            InputFieldWithError(
                                label = "Дата",
                                iconRes = R.drawable.ic_calendar,
                                value = uiState.date,
                                onValueChange = onDateChange,
                                placeholder = "ДД.ММ.ГГГГ",
                                error = uiState.dateError,
                                keyboardType = KeyboardType.Number,
                                visualTransformation = DateVisualTransformation,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(Modifier.width(8.dp))

                            InputFieldWithError(
                                label = "Время",
                                iconRes = R.drawable.ic_clock,
                                value = uiState.time,
                                onValueChange = onTimeChange,
                                placeholder = "ЧЧ:ММ",
                                error = uiState.timeError,
                                keyboardType = KeyboardType.Number,
                                visualTransformation = TimeVisualTransformation,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(11.dp))

                        // Участники
                        SelectionFieldWithError(
                            label = "Участники",
                            iconRes = R.drawable.ic_members,
                            selectedText = uiState.participants,
                            placeholder = "Выберите участников",
                            error = null,
                            onClick = { showParticipantsDialog = true },
                            isMultiSelect = true
                        )

                        Spacer(Modifier.height(11.dp))

                        // Ссылка
                        InputFieldWithError(
                            label = "Ссылка",
                            iconRes = R.drawable.ic_link,
                            value = uiState.link,
                            onValueChange = onLinkChange,
                            placeholder = "Ссылка на собрание",
                            error = null
                        )

                        Spacer(Modifier.height(11.dp))

                        // Описание
                        Text(
                            text = "Описание",
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = BluePrimary300,
                            modifier = Modifier.padding(start = 11.dp),
                            style = TextStyle(
                                lineHeight = 16.sp,
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )

                        Spacer(Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(117.dp)
                                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                                .background(Color.White, RoundedCornerShape(18.dp))
                                .padding(start = 11.dp, end = 5.dp, top = 6.dp)
                        ) {
                            BasicTextField(
                                value = uiState.description,
                                onValueChange = onDescriptionChange,
                                modifier = Modifier
                                    .height(117.dp)
                                    .width(328.dp),
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    fontFamily = OnestFontFamily,
                                    color = GroupeakDarkBlue,
                                    lineHeight = 16.sp,
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Default
                                ),
                                decorationBox = { inner ->
                                    if (uiState.description.isEmpty()) {
                                        Text(
                                            text = "Описание мероприятия",
                                            fontSize = 14.sp,
                                            fontFamily = OnestFontFamily,
                                            color = Gray650,
                                            style = TextStyle(
                                                lineHeight = 16.sp,
                                                lineHeightStyle = LineHeightStyle(
                                                    alignment = LineHeightStyle.Alignment.Center,
                                                    trim = LineHeightStyle.Trim.Both
                                                )
                                            )
                                        )
                                    }
                                    inner()
                                }
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PrimaryButton(
                                modifier = Modifier
                                    .width(154.dp)
                                    .height(32.dp),
                                text = "Создать",
                                onClick = onCreateClick,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }

        // Диалог выбора проекта
        if (showProjectDialog) {
            Dialog(onDismissRequest = { showProjectDialog = false }) {
                Box(
                    modifier = Modifier
                        .width(348.dp)
                        .height(495.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        val projects = listOf(
                            "SmartHome",
                            "LinUX",
                            "Программа для Tahoe",
                            "Peak.Group",
                            "ManageGroup"
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "Выбор проекта",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = OnestFontFamily,
                                color = BluePrimary300,
                                modifier = Modifier
                                    .padding(bottom = 15.dp)
                                    .align(Alignment.CenterHorizontally)
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp)
                            ) {
                                itemsIndexed(projects) { index, project ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onProjectChange(project)
                                                showProjectDialog = false
                                            }
                                            .padding(vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = project,
                                            fontSize = 20.sp,
                                            fontFamily = OnestFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = GroupeakDarkBlue,
                                            modifier = Modifier
                                                .width(220.dp)
                                                .padding(start = 9.dp)
                                        )

                                        Spacer(Modifier.weight(1f))

                                        Icon(
                                            painter = painterResource(
                                                id = if (uiState.project == project)
                                                    R.drawable.ic_selected_circle
                                                else
                                                    R.drawable.ic_circle
                                            ),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }

                                    if (index != projects.lastIndex) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(Color(0xFFB0B3C7))
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(23.dp))
                        }
                    }
                }
            }
        }

        // Диалог выбора участников
        if (showParticipantsDialog) {
            Dialog(onDismissRequest = { showParticipantsDialog = false }) {
                Box(
                    modifier = Modifier
                        .width(348.dp)
                        .height(495.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        val members = listOf(
                            "Михаил Д." to "Frontend",
                            "Василий П." to "Backend",
                            "Андрей П." to "Designer",
                            "Алексей В." to "PR",
                            "Юрий X." to "Full-stack"
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "Выбор участников",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = OnestFontFamily,
                                color = BluePrimary300,
                                modifier = Modifier
                                    .padding(bottom = 15.dp)
                                    .align(Alignment.CenterHorizontally)
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp)
                            ) {
                                itemsIndexed(members) { index, (name, role) ->
                                    val isSelected = uiState.participants.contains(name, ignoreCase = true)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val currentList = uiState.participants
                                                    .split(", ")
                                                    .filter { it.isNotBlank() }
                                                    .toMutableList()

                                                if (isSelected) {
                                                    currentList.removeAll { it.equals(name, ignoreCase = true) }
                                                } else {
                                                    currentList.add(name)
                                                }

                                                onParticipantsChange(currentList.joinToString(", "))
                                            }
                                            .padding(vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = name,
                                            fontSize = 20.sp,
                                            fontFamily = OnestFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = GroupeakDarkBlue,
                                            modifier = Modifier
                                                .width(120.dp)
                                                .padding(start = 9.dp)
                                        )

                                        Text(
                                            text = role,
                                            fontSize = 20.sp,
                                            fontFamily = OnestFontFamily,
                                            color = Gray650,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .width(100.dp)
                                                .wrapContentWidth(Alignment.CenterHorizontally)
                                        )

                                        Spacer(Modifier.weight(1f))

                                        Icon(
                                            painter = painterResource(
                                                id = if (isSelected)
                                                    R.drawable.ic_selected_check_mark
                                                else
                                                    R.drawable.ic_check_mark
                                            ),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }

                                    if (index != members.lastIndex) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(Color(0xFFB0B3C7))
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(23.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InputFieldWithError(
    label: String,
    iconRes: Int,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = if (error != null) Color.Red else BluePrimary300,
            modifier = Modifier.padding(start = 11.dp),
            style = TextStyle(
                lineHeight = 16.sp,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            )
        )

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .border(
                    width = 1.dp,
                    color = if (error != null) Color.Red else GroupeakLightGray,
                    shape = RoundedCornerShape(18.dp)
                )
                .background(Color.White, RoundedCornerShape(18.dp))
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
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = OnestFontFamily,
                    color = GroupeakDarkBlue,
                    lineHeight = 16.sp,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both
                    )
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = visualTransformation,
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            color = Gray650
                        )
                    }
                    inner()
                }
            )
        }

        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 11.dp)
            )
        }
    }
}

@Composable
private fun SelectionFieldWithError(
    label: String,
    iconRes: Int,
    selectedText: String,
    placeholder: String,
    error: String?,
    onClick: () -> Unit,
    isMultiSelect: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = if (error != null) Color.Red else BluePrimary300,
            modifier = Modifier.padding(start = 11.dp),
            style = TextStyle(
                lineHeight = 16.sp,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            )
        )

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .border(
                    width = 1.dp,
                    color = if (error != null) Color.Red else GroupeakLightGray,
                    shape = RoundedCornerShape(18.dp)
                )
                .background(Color.White, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = BluePrimary300,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(24.dp)
            )

            Spacer(Modifier.width(8.dp))

            val displayText = if (isMultiSelect) {
                formatParticipantsText(selectedText, placeholder)
            } else {
                if (selectedText.isNotBlank()) AnnotatedString(selectedText) else AnnotatedString(placeholder)
            }

            Text(
                text = displayText,
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,
                letterSpacing = (-0.05).sp,
                color = if (selectedText.isNotBlank()) GroupeakDarkBlue else Gray650,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_down_arrow),
                contentDescription = "Открыть выбор",
                tint = BluePrimary300,
                modifier = Modifier
                    .padding(end = 11.dp)
                    .size(23.dp)
            )
        }

        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 16.dp)
            )
        }
    }
}