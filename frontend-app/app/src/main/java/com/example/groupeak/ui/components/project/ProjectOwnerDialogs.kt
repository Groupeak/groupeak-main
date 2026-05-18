package com.example.groupeak.ui.components.project

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.groupeak.R
import com.example.groupeak.ui.components.PrimaryButton
import com.example.groupeak.ui.screens.project_for_owner.ProjectEventItemUi
import com.example.groupeak.ui.theme.*

private val HeaderBlue = Color(0xFF1876FF)

// --- Visual Transformations ---

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

// --- Validation Helpers ---

private fun isValidDate(date: String): Boolean {
    val digits = date.filter { it.isDigit() }
    if (digits.length != 8) return false
    return try {
        val day = digits.substring(0, 2).toInt()
        val month = digits.substring(2, 4).toInt()
        val year = digits.substring(4, 8).toInt()
        day in 1..31 && month in 1..12 && year >= 2020 && year <= 2100
    } catch (e: Exception) { false }
}

private fun isValidTime(time: String): Boolean {
    val digits = time.filter { it.isDigit() }
    if (digits.length != 4) return false
    return try {
        val hours = digits.substring(0, 2).toInt()
        val minutes = digits.substring(2, 4).toInt()
        hours in 0..23 && minutes in 0..59
    } catch (e: Exception) { false }
}

// --- Dialog Components ---

@Composable
fun DeleteConfirmationDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(237.dp)
                .height(104.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = HeaderBlue,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PrimaryButton(
                        modifier = Modifier
                            .width(91.dp)
                            .height(32.dp),
                        text = "Да",
                        onClick = onConfirm,
                        fontSize = 14.sp,
                    )

                    Button(
                        modifier = Modifier
                            .width(91.dp)
                            .height(32.dp),
                        onClick = onDismiss,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = BluePrimary300
                        ),
                        border = BorderStroke(1.dp, BluePrimary300)
                    ) {
                        Text(
                            text = "Нет",
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InviteMemberDialog(
    email: String,
    position: String,
    onEmailChange: (String) -> Unit,
    onPositionChange: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(348.dp)
                .height(309.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(top = 16.dp, start = 10.dp, end = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Приглашение",
                    fontSize = 20.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = HeaderBlue,
                )

                Spacer(modifier = Modifier.height(11.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Почта",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = HeaderBlue,
                        modifier = Modifier.padding(start = 11.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(horizontal = 16.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            color = Color(0xFF333B46),
                        ),
                        decorationBox = { inner ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_email),
                                    contentDescription = null,
                                    tint = HeaderBlue,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(Modifier.weight(1f)) {
                                    if (email.isEmpty()) {
                                        Text(
                                            text = "Введите почту участника",
                                            fontSize = 14.sp,
                                            fontFamily = OnestFontFamily,
                                            color = Gray650,
                                        )
                                    }
                                    inner()
                                }
                            }
                        },
                    )
                }

                Spacer(modifier = Modifier.height(11.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Должность",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = HeaderBlue,
                        modifier = Modifier.padding(start = 11.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = position,
                        onValueChange = onPositionChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(horizontal = 10.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            color = Color(0xFF333B46),
                        ),
                        decorationBox = { inner ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_role),
                                    contentDescription = null,
                                    tint = HeaderBlue,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(Modifier.weight(1f)) {
                                    if (position.isEmpty()) {
                                        Text(
                                            text = "Введите должность участника",
                                            fontSize = 14.sp,
                                            fontFamily = OnestFontFamily,
                                            color = Gray650,
                                        )
                                    }
                                    inner()
                                }
                            }
                        },
                    )
                }

                Spacer(modifier = Modifier.height(21.dp))

                PrimaryButton(
                    modifier = Modifier
                        .width(261.dp)
                        .height(32.dp),
                    text = "Отправить приглашение",
                    onClick = onSend,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
fun UpdateDateDialog(
    newDate: String,
    dateError: String?,
    onDateChange: (String) -> Unit,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(227.dp)
                .height(184.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back_arrow),
                        contentDescription = "Назад",
                        tint = HeaderBlue,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDismiss() },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Введите новую дату",
                        fontSize = 18.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = HeaderBlue,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                BasicTextField(
                    value = newDate,
                    onValueChange = onDateChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .border(
                            1.dp,
                            if (dateError != null) Color.Red else GroupeakLightGray,
                            RoundedCornerShape(18.dp),
                        )
                        .background(Color.White, RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        color = Color(0xFF333B46),
                    ),
                    decorationBox = { inner ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_calendar),
                                contentDescription = null,
                                tint = HeaderBlue,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(Modifier.weight(1f)) {
                                if (newDate.isEmpty()) {
                                    Text(
                                        text = "ДД.ММ.ГГГГ",
                                        fontSize = 14.sp,
                                        fontFamily = OnestFontFamily,
                                        color = Gray650,
                                    )
                                }
                                inner()
                            }
                        }
                    },
                )

                if (dateError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateError,
                        fontSize = 12.sp,
                        fontFamily = OnestFontFamily,
                        color = Color.Red,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    modifier = Modifier
                        .width(154.dp)
                        .height(32.dp),
                    text = "Обновить",
                    onClick = onUpdate,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
fun ProjectEventEditDialog(
    event: ProjectEventItemUi,
    projectName: String,
    availableProjects: List<String> = listOf(projectName),
    availableMembers: List<Pair<String, String>> = emptyList(),
    initialTitle: String = event.title,
    initialDate: String = event.date,
    initialTime: String = event.time,
    initialLink: String = event.link,
    initialDescription: String = event.description,
    initialParticipants: String = "",
    onTitleChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onLinkChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onParticipantsChange: (String) -> Unit,
    onProjectSelect: (String) -> Unit,
    onSave: (title: String, date: String, time: String, link: String, description: String, participants: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var date by remember { mutableStateOf(initialDate) }
    var time by remember { mutableStateOf(initialTime) }
    var link by remember { mutableStateOf(initialLink) }
    var description by remember { mutableStateOf(initialDescription) }
    var participants by remember { mutableStateOf(initialParticipants) }
    var selectedProject by remember { mutableStateOf(projectName) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var showProjectSelectDialog by remember { mutableStateOf(false) }
    var showParticipantsSelectDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(348.dp)
                .height(720.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(
                                top = 20.dp,
                                start = 10.dp,
                                end = 10.dp,
                                bottom = 12.dp
                            )
                    ) {
                        Text(
                            text = "Название",
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = BluePrimary300,
                            letterSpacing = (-0.4).sp,
                            modifier = Modifier.padding(start = 11.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        BasicTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                onTitleChange(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                                .background(Color.White, RoundedCornerShape(18.dp))
                                .padding(start = 11.dp, end = 11.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                fontFamily = OnestFontFamily,
                                color = Color(0xFF333B46),
                                letterSpacing = (-0.4).sp,
                            ),
                            decorationBox = { inner ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_task_name),
                                        contentDescription = null,
                                        tint = BluePrimary300,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (title.isEmpty()) {
                                            Text(
                                                text = "Название мероприятия",
                                                fontSize = 14.sp,
                                                fontFamily = OnestFontFamily,
                                                color = Gray650,
                                                letterSpacing = (-0.4).sp,
                                            )
                                        }
                                        inner()
                                    }
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(11.dp))

                        Text(
                            text = "Проект",
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = BluePrimary300,
                            modifier = Modifier.padding(start = 11.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                                .background(Color.White, RoundedCornerShape(18.dp))
                                .clickable { showProjectSelectDialog = true }
                                .padding(start = 11.dp, end = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_folder),
                                contentDescription = null,
                                tint = BluePrimary300,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedProject,
                                fontSize = 14.sp,
                                fontFamily = OnestFontFamily,
                                color = Color(0xFF333B46),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_down_arrow),
                                contentDescription = "Выбрать проект",
                                tint = BluePrimary300,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(11.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Дата",
                                    fontSize = 14.sp,
                                    fontFamily = OnestFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = if (dateError != null) Color.Red else BluePrimary300,
                                    modifier = Modifier.padding(start = 11.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                BasicTextField(
                                    value = date,
                                    onValueChange = {
                                        val filtered = it.filter { c -> c.isDigit() || c == '.' }.take(10)
                                        date = filtered
                                        onDateChange(filtered)
                                        dateError = if (filtered.isNotEmpty() && !isValidDate(filtered)) {
                                            "Неверная дата"
                                        } else null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(55.dp)
                                        .border(
                                            1.dp,
                                            if (dateError != null) Color.Red else GroupeakLightGray,
                                            RoundedCornerShape(18.dp)
                                        )
                                        .background(Color.White, RoundedCornerShape(18.dp))
                                        .padding(start = 11.dp, end = 11.dp),
                                    singleLine = true,
                                    visualTransformation = DateVisualTransformation,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        fontFamily = OnestFontFamily,
                                        color = Color(0xFF333B46),
                                    ),
                                    decorationBox = { inner ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_calendar),
                                                contentDescription = null,
                                                tint = BluePrimary300,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(modifier = Modifier.weight(1f)) {
                                                if (date.isEmpty()) {
                                                    Text(
                                                        text = "ДД.ММ.ГГГГ",
                                                        fontSize = 14.sp,
                                                        fontFamily = OnestFontFamily,
                                                        color = Gray650,
                                                    )
                                                }
                                                inner()
                                            }
                                        }
                                    },
                                )
                                if (dateError != null) {
                                    Text(
                                        text = dateError!!,
                                        fontSize = 12.sp,
                                        color = Color.Red,
                                        modifier = Modifier.padding(top = 4.dp, start = 11.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Время",
                                    fontSize = 14.sp,
                                    fontFamily = OnestFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = if (timeError != null) Color.Red else BluePrimary300,
                                    modifier = Modifier.padding(start = 11.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                BasicTextField(
                                    value = time,
                                    onValueChange = {
                                        val filtered = it.filter { c -> c.isDigit() || c == ':' }.take(5)
                                        time = filtered
                                        onTimeChange(filtered)
                                        timeError = if (filtered.isNotEmpty() && !isValidTime(filtered)) {
                                            "Неверное время"
                                        } else null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(55.dp)
                                        .border(
                                            1.dp,
                                            if (timeError != null) Color.Red else GroupeakLightGray,
                                            RoundedCornerShape(18.dp)
                                        )
                                        .background(Color.White, RoundedCornerShape(18.dp))
                                        .padding(start = 11.dp, end = 11.dp),
                                    singleLine = true,
                                    visualTransformation = TimeVisualTransformation,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        fontFamily = OnestFontFamily,
                                        color = Color(0xFF333B46),
                                    ),
                                    decorationBox = { inner ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_clock),
                                                contentDescription = null,
                                                tint = BluePrimary300,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(modifier = Modifier.weight(1f)) {
                                                if (time.isEmpty()) {
                                                    Text(
                                                        text = "ЧЧ:ММ",
                                                        fontSize = 14.sp,
                                                        fontFamily = OnestFontFamily,
                                                        color = Gray650,
                                                    )
                                                }
                                                inner()
                                            }
                                        }
                                    },
                                )
                                if (timeError != null) {
                                    Text(
                                        text = timeError!!,
                                        fontSize = 12.sp,
                                        color = Color.Red,
                                        modifier = Modifier.padding(top = 4.dp, start = 11.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(11.dp))

                        Text(
                            text = "Участники",
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = BluePrimary300,
                            modifier = Modifier.padding(start = 11.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                                .background(Color.White, RoundedCornerShape(18.dp))
                                .clickable { showParticipantsSelectDialog = true }
                                .padding(start = 11.dp, end = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_members),
                                contentDescription = null,
                                tint = BluePrimary300,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val displayText = if (participants.isEmpty()) {
                                "Выберите участников"
                            } else {
                                val list = participants.split(", ").filter { it.isNotBlank() }
                                if (list.size <= 2) participants
                                else "${list[0]}, ${list[1]} и еще ${list.size - 2}"
                            }
                            Text(
                                text = displayText,
                                fontSize = 14.sp,
                                fontFamily = OnestFontFamily,
                                color = if (participants.isEmpty()) Gray650 else Color(0xFF333B46),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_down_arrow),
                                contentDescription = "Выбрать участников",
                                tint = BluePrimary300,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(11.dp))

                        Text(
                            text = "Ссылка",
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = BluePrimary300,
                            modifier = Modifier.padding(start = 11.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        BasicTextField(
                            value = link,
                            onValueChange = {
                                link = it
                                onLinkChange(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                                .background(Color.White, RoundedCornerShape(18.dp))
                                .padding(start = 16.dp, end = 16.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                fontFamily = OnestFontFamily,
                                color = Color(0xFF333B46),
                            ),
                            decorationBox = { inner ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_link),
                                        contentDescription = null,
                                        tint = BluePrimary300,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (link.isEmpty()) {
                                            Text(
                                                text = "Ссылка на собрание",
                                                fontSize = 14.sp,
                                                fontFamily = OnestFontFamily,
                                                color = Gray650,
                                            )
                                        }
                                        inner()
                                    }
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(11.dp))

                        Text(
                            text = "Описание",
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = BluePrimary300,
                            modifier = Modifier.padding(start = 11.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(117.dp)
                                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                                .background(Color.White, RoundedCornerShape(18.dp))
                                .padding(start = 11.dp, end = 5.dp, top = 6.dp)
                        ) {
                            BasicTextField(
                                value = description,
                                onValueChange = {
                                    description = it
                                    onDescriptionChange(it)
                                },
                                modifier = Modifier.fillMaxSize(),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 14.sp,
                                    fontFamily = OnestFontFamily,
                                    color = Color(0xFF333B46),
                                    lineHeight = 16.sp
                                ),
                                decorationBox = { inner ->
                                    if (description.isEmpty()) {
                                        Text(
                                            text = "Описание мероприятия",
                                            fontSize = 14.sp,
                                            fontFamily = OnestFontFamily,
                                            color = Gray650,
                                        )
                                    }
                                    inner()
                                }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PrimaryButton(
                            modifier = Modifier
                                .width(154.dp)
                                .height(32.dp),
                            text = "Сохранить",
                            onClick = {
                                if (!isValidDate(date)) {
                                    dateError = "Неверная дата"
                                    return@PrimaryButton
                                }
                                if (!isValidTime(time)) {
                                    timeError = "Неверное время"
                                    return@PrimaryButton
                                }
                                onSave(title, date, time, link, description, participants)
                            },
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    if (showProjectSelectDialog) {
        Dialog(
            onDismissRequest = { showProjectSelectDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.width(348.dp).height(495.dp)) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
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
                            modifier = Modifier.fillMaxWidth().height(380.dp)
                        ) {
                            itemsIndexed(availableProjects) { index, project ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedProject = project
                                            onProjectSelect(project)
                                            showProjectSelectDialog = false
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
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        painter = painterResource(
                                            id = if (selectedProject == project)
                                                R.drawable.ic_selected_circle
                                            else
                                                R.drawable.ic_circle
                                        ),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                if (index != availableProjects.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFB0B3C7))
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(23.dp))
                    }
                }
            }
        }
    }

    if (showParticipantsSelectDialog) {
        Dialog(
            onDismissRequest = { showParticipantsSelectDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.width(348.dp).height(495.dp)) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
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
                            modifier = Modifier.fillMaxWidth().height(380.dp)
                        ) {
                            itemsIndexed(availableMembers) { index, (name, role) ->
                                val isSelected = participants.contains(name, ignoreCase = true)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val currentList = participants
                                                .split(", ")
                                                .filter { it.isNotBlank() }
                                                .toMutableList()

                                            if (isSelected) {
                                                currentList.removeAll {
                                                    it.equals(name, ignoreCase = true)
                                                }
                                            } else {
                                                currentList.add(name)
                                            }

                                            val newParticipants = currentList.joinToString(", ")
                                            participants = newParticipants
                                            onParticipantsChange(newParticipants)
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
                                        modifier = Modifier
                                            .width(100.dp)
                                            .wrapContentWidth(Alignment.CenterHorizontally)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
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
                                if (index != availableMembers.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFB0B3C7))
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(23.dp))
                    }
                }
            }
        }
    }
}