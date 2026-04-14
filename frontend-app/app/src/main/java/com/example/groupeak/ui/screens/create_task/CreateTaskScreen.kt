package com.example.groupeak.ui.screens.create_task

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.components.PrimaryButton
import com.example.groupeak.ui.theme.*

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

private fun formatShortName(fullName: String): String {
    val parts = fullName
        .trim()
        .split("\\s+".toRegex())
        .filter { it.isNotBlank() }

    return when (parts.size) {
        0 -> ""
        1 -> parts[0]
        else -> "${parts[0]} ${parts[1].first()}."
    }
}

@Composable
fun CreateTaskScreen(
    uiState: CreateTaskUiState,
    onBackClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onProjectSelected: (Long) -> Unit,
    onDeadlineChange: (String) -> Unit,
    onToggleParticipant: (Long) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onCreateClick: () -> Unit,
) {
    var showProjectDialog by remember { mutableStateOf(false) }
    var showParticipantsDialog by remember { mutableStateOf(false) }

    var isExpanded by remember { mutableStateOf(false) }
    var showStatusText by remember { mutableStateOf(false) }

    val animatedCardHeight by animateDpAsState(
        targetValue = if (isExpanded) 675.dp + 36.dp else 675.dp,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "cardHeight",
    )

    val statusAlpha by animateFloatAsState(
        targetValue = if (showStatusText) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "statusAlpha",
    )

    val statusSlide by animateDpAsState(
        targetValue = if (showStatusText) 0.dp else 10.dp,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "statusSlide",
    )

    LaunchedEffect(uiState.statusMessagePulse) {
        if (uiState.statusMessagePulse == 0 || uiState.statusMessage.isNullOrBlank()) return@LaunchedEffect

        isExpanded = true
        kotlinx.coroutines.delay(140)
        showStatusText = true

        kotlinx.coroutines.delay(2100)
        showStatusText = false
        kotlinx.coroutines.delay(280)
        isExpanded = false
    }

    GroupeakBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    text = "Создание задачи",
                    fontSize = 32.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = BluePrimary300,
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
                        .clickable { },
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

            Box(
                modifier = Modifier
                    .width(348.dp)
                    .height(animatedCardHeight)
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .padding(top = 20.dp, start = 10.dp, end = 10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    InputFieldWithError(
                        label = "Название",
                        iconRes = R.drawable.ic_task_name,
                        value = uiState.title,
                        onValueChange = onTitleChange,
                        placeholder = "Введите название задачи",
                        error = uiState.titleError
                    )

                    Spacer(Modifier.height(11.dp))

                    SelectionFieldWithError(
                        label = "Проект",
                        iconRes = R.drawable.ic_project,
                        selectedText = uiState.project,
                        placeholder = if (uiState.isLoadingProjects) "Загрузка проектов..." else "Выберите проект для задачи",
                        error = uiState.projectError,
                        onClick = { showProjectDialog = true }
                    )

                    Spacer(Modifier.height(11.dp))

                    InputFieldWithError(
                        label = "Дедлайн",
                        iconRes = R.drawable.ic_calendar,
                        value = uiState.deadline,
                        onValueChange = onDeadlineChange,
                        placeholder = "ДД.ММ.ГГГГ",
                        error = uiState.deadlineError,
                        keyboardType = KeyboardType.Number,
                        visualTransformation = DateVisualTransformation
                    )

                    Spacer(Modifier.height(11.dp))

                    SelectionFieldWithError(
                        label = "Участники",
                        iconRes = R.drawable.ic_members,
                        selectedText = uiState.participants,
                        placeholder = if (uiState.selectedProjectId == null) {
                            "Сначала выберите проект"
                        } else if (uiState.isLoadingMembers) {
                            "Загрузка участников..."
                        } else {
                            "Выберите участников"
                        },
                        error = uiState.participantsError,
                        onClick = { showParticipantsDialog = true },
                        isMultiSelect = true
                    )

                    Spacer(Modifier.height(11.dp))

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
                            .border(1.dp, if (uiState.descriptionError != null) Color.Red else GroupeakLightGray, RoundedCornerShape(18.dp))
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
                                        text = "Описание задачи",
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

                    Spacer(Modifier.height(11.dp))

                    Text(
                        text = "Приоритет",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = BluePrimary300,
                        modifier = Modifier.padding(start = 11.dp, top = 8.dp),
                        style = TextStyle(
                            lineHeight = 16.sp,
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            )
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    PrioritySelector(
                        selectedPriority = uiState.priority,
                        onPriorityChange = onPriorityChange
                    )

                    Spacer(Modifier.height(12.dp))

                    PrimaryButton(
                        modifier = Modifier
                            .width(154.dp)
                            .height(32.dp)
                            .align(Alignment.CenterHorizontally),
                        text = if (uiState.isLoading) "Создание..." else "Создать",
                        onClick = onCreateClick,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp + 36.dp)
                            .graphicsLayer { alpha = statusAlpha }
                            .offset(y = statusSlide),
                        contentAlignment = Alignment.Center,
                    ) {
                        val statusMessage = uiState.statusMessage.orEmpty()
                        if (statusMessage.isNotBlank()) {
                            Text(
                                text = statusMessage,
                                color = if (uiState.statusMessageIsError) Color(0xFFFF0000) else Color(0xFF26BC30),
                                fontSize = 14.sp,
                                fontFamily = OnestFontFamily,
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

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
                                text = "Выбор проекта",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = OnestFontFamily,
                                color = BluePrimary300,
                                modifier = Modifier
                                    .padding(bottom = 15.dp)
                                    .align(Alignment.CenterHorizontally)
                            )

                            when {
                                uiState.isLoadingProjects -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(380.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = BluePrimary300)
                                    }
                                }

                                uiState.availableProjects.isEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(380.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Нет доступных проектов",
                                            fontSize = 16.sp,
                                            fontFamily = OnestFontFamily,
                                            color = Gray650,
                                        )
                                    }
                                }

                                else -> {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(380.dp)
                                    ) {
                                        itemsIndexed(uiState.availableProjects) { index, project ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        onProjectSelected(project.id)
                                                        showProjectDialog = false
                                                    }
                                                    .padding(vertical = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = project.name,
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
                                                        id = if (uiState.selectedProjectId == project.id)
                                                            R.drawable.ic_selected_circle
                                                        else
                                                            R.drawable.ic_circle
                                                    ),
                                                    contentDescription = null,
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                            }

                                            if (index != uiState.availableProjects.lastIndex) {
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

                            Spacer(Modifier.height(23.dp))
                        }
                    }
                }
            }
        }

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
                                text = "Выбор участников",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = OnestFontFamily,
                                color = BluePrimary300,
                                modifier = Modifier
                                    .padding(bottom = 15.dp)
                                    .align(Alignment.CenterHorizontally)
                            )

                            when {
                                uiState.selectedProjectId == null -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(380.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Сначала выберите проект",
                                            fontSize = 16.sp,
                                            fontFamily = OnestFontFamily,
                                            color = Gray650,
                                        )
                                    }
                                }

                                uiState.isLoadingMembers -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(380.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = BluePrimary300)
                                    }
                                }

                                uiState.availableMembers.isEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(380.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "В этом проекте нет участников",
                                            fontSize = 16.sp,
                                            fontFamily = OnestFontFamily,
                                            color = Gray650,
                                        )
                                    }
                                }

                                else -> {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(380.dp)
                                    ) {
                                        itemsIndexed(uiState.availableMembers) { index, member ->
                                            val isSelected = uiState.selectedParticipantIds.contains(member.userId)

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        onToggleParticipant(member.userId)
                                                    }
                                                    .padding(vertical = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = formatShortName(member.fullName),
                                                    fontSize = 20.sp,
                                                    fontFamily = OnestFontFamily,
                                                    fontWeight = FontWeight.Normal,
                                                    color = GroupeakDarkBlue,
                                                    modifier = Modifier
                                                        .width(120.dp)
                                                        .padding(start = 9.dp)
                                                )

                                                Text(
                                                    text = member.position,
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

                                            if (index != uiState.availableMembers.lastIndex) {
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
    visualTransformation: VisualTransformation = VisualTransformation.None
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
    }
}

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
                    .size(22.dp)
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
                color = if (selectedText.isNotBlank()) GroupeakDarkBlue else Gray650,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_down_arrow),
                contentDescription = "Открыть выбор",
                tint = BluePrimary300,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp)
            )
        }
    }
}

private enum class PriorityLevel { LOW, MEDIUM, HIGH }

@Composable
private fun PrioritySelector(
    selectedPriority: String?,
    onPriorityChange: (String) -> Unit
) {
    val segmentWidth = 328.dp / 3

    val priorityEnum = when (selectedPriority) {
        "Низкий" -> PriorityLevel.LOW
        "Средний" -> PriorityLevel.MEDIUM
        "Высокий" -> PriorityLevel.HIGH
        else -> null
    }

    var previousPriority by remember { mutableStateOf(priorityEnum) }
    val selectedIndex = when (priorityEnum) {
        PriorityLevel.LOW -> 0
        PriorityLevel.MEDIUM -> 1
        PriorityLevel.HIGH -> 2
        null -> 0
    }

    val shouldSnapToFirstSelection = previousPriority == null && priorityEnum != null

    val animatedOffset by animateDpAsState(
        targetValue = segmentWidth * selectedIndex,
        animationSpec = if (shouldSnapToFirstSelection) {
            tween(durationMillis = 0)
        } else {
            tween(durationMillis = 280, easing = FastOutSlowInEasing)
        },
        label = "priority_offset"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (priorityEnum == null) 0f else 1f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "priority_alpha"
    )

    val animatedColor by animateColorAsState(
        targetValue = when (priorityEnum) {
            PriorityLevel.LOW -> Color(0xFF26BC30)
            PriorityLevel.MEDIUM -> Color(0xFFCDC036)
            PriorityLevel.HIGH -> Color(0xFFFF0000)
            null -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "priority_color"
    )

    LaunchedEffect(priorityEnum) {
        previousPriority = priorityEnum
    }

    Box(
        modifier = Modifier
            .width(328.dp)
            .height(37.dp)
            .background(Color.Transparent, RoundedCornerShape(18.dp))
            .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
    ) {
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(18.dp))
                .background(animatedColor.copy(alpha = animatedAlpha))
        )

        Row(modifier = Modifier.fillMaxSize()) {
            PrioritySegmentItem(
                text = "Низкий",
                selected = selectedPriority == "Низкий",
                onClick = { onPriorityChange("Низкий") },
                modifier = Modifier.weight(1f)
            )
            PrioritySegmentItem(
                text = "Средний",
                selected = selectedPriority == "Средний",
                onClick = { onPriorityChange("Средний") },
                modifier = Modifier.weight(1f)
            )
            PrioritySegmentItem(
                text = "Высокий",
                selected = selectedPriority == "Высокий",
                onClick = { onPriorityChange("Высокий") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PrioritySegmentItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedTextColor by animateColorAsState(
        targetValue = if (selected) Color.White else GroupeakDarkBlue,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "priority_text_color"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontFamily = OnestFontFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = animatedTextColor
        )
    }
}