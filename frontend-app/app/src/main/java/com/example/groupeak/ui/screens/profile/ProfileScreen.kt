package com.example.groupeak.ui.screens.profile

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.GroupeakDarkBlue
import com.example.groupeak.ui.theme.GroupeakLightGray
import com.example.groupeak.ui.theme.Gray650
import com.example.groupeak.ui.theme.OnestFontFamily

// Размер области нажатия по уведомлениям (справа сверху в шапке)
private val NotifHitBoxSize = 28.dp

// Размер иконки уведомлений внутри области нажатия
private val NotifIconSize = 22.dp

// Единый стиль текста для значений в полях
private val FieldTextStyle = TextStyle(
    fontSize = 14.sp,
    fontFamily = OnestFontFamily,
    fontWeight = FontWeight.Normal
)

/**
 * Визуальная маска для даты.
 * Пользователь видит "DD.MM.YYYY", а в state хранятся только цифры "DDMMYYYY".
 */
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

// Преобразует "DDMMYYYY" в "DD.MM.YYYY" для режима просмотра (не редактирование)
private fun formatBirthDateDigitsToMasked(digits: String): String {
    val raw = digits.filter { it.isDigit() }.take(8)
    if (raw.isEmpty()) return ""
    return buildString {
        for (i in raw.indices) {
            append(raw[i])
            if (i == 1 || i == 3) append('.')
        }
    }
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onPositionClick: () -> Unit,
    onPositionChange: (String) -> Unit,
    onAboutChange: (String) -> Unit
) {
    var positionMenuExpanded by remember { mutableStateOf(false) }
    val positionMenuScroll = rememberScrollState()

    val positionOptions = listOf(
        "Frontend",
        "Backend",
        "Designer",
        "QA",
        "DevOps",
        "Android",
        "iOS",
        "Data Analyst",
        "Product",
        "HR"
    )

    val positionMenuMaxHeight = 48.dp * 3

    GroupeakBackground {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileHeader(
                    title = "Профиль",
                    hasNotifications = uiState.hasNotifications,
                    onBackClick = onBackClick,
                    onNotificationsClick = onNotificationsClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.padding(top = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .requiredWidth(348.dp)
                            .requiredHeight(633.dp)
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(bottom = 11.dp)
                    ) {
                        Text(
                            text = if (uiState.isEditing) "Сохранить" else "Изменить",
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = BluePrimary300,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 20.dp, top = 11.dp)
                                .clickable { if (uiState.isEditing) onSaveClick() else onEditClick() }
                        )

                        Spacer(Modifier.height(27.dp))

                        if (!uiState.isEditing) {
                            LabeledField(
                                label = "Фамилия Имя Отчество",
                                iconRes = R.drawable.ic_fio,
                                value = uiState.fullName,
                                modifier = Modifier
                                    .requiredWidth(328.dp)
                                    .align(Alignment.CenterHorizontally),
                                fieldModifier = Modifier.fillMaxWidth(),
                                fieldHeight = 55.dp,
                                gapBetween = 6.dp,
                                labelStartPadding = 16.dp,
                                placeholder = "Не указано",
                                placeholderColor = Gray650
                            )
                        } else {
                            LabeledInputField(
                                label = "Фамилия Имя Отчество",
                                iconRes = R.drawable.ic_fio,
                                value = uiState.fullName,
                                onValueChange = onFullNameChange,
                                modifier = Modifier
                                    .requiredWidth(328.dp)
                                    .align(Alignment.CenterHorizontally),
                                fieldModifier = Modifier.fillMaxWidth(),
                                fieldHeight = 55.dp,
                                gapBetween = 6.dp,
                                labelStartPadding = 16.dp,
                                placeholder = "Не указано"
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .requiredWidth(326.dp)
                                .align(Alignment.CenterHorizontally),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            BirthDateField(
                                iconRes = R.drawable.ic_calendar,
                                valueDigits = uiState.birthDate,
                                onValueChange = onBirthDateChange,
                                enabled = uiState.isEditing,
                                modifier = Modifier.requiredWidth(155.dp),
                                fieldModifier = Modifier.fillMaxWidth(),
                                fieldHeight = 55.dp,
                                gapBetween = 5.dp,
                                labelStartPadding = 16.dp,
                                placeholder = "ДД.ММ.ГГГГ"
                            )

                            if (!uiState.isEditing) {
                                LabeledField(
                                    label = "Должность",
                                    iconRes = R.drawable.ic_briefcase,
                                    value = uiState.position,
                                    modifier = Modifier.requiredWidth(155.dp),
                                    fieldModifier = Modifier.fillMaxWidth(),
                                    fieldHeight = 55.dp,
                                    placeholder = "Не указана",
                                    placeholderColor = Gray650,
                                    gapBetween = 5.dp,
                                    labelStartPadding = 16.dp
                                )
                            } else {
                                Box(modifier = Modifier.requiredWidth(155.dp)) {

                                    LabeledSelectField(
                                        label = "Должность",
                                        iconRes = R.drawable.ic_briefcase,
                                        value = uiState.position,
                                        expanded = positionMenuExpanded,
                                        onClick = {
                                            val newValue = !positionMenuExpanded
                                            positionMenuExpanded = newValue
                                            if (newValue) onPositionClick()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        fieldModifier = Modifier.fillMaxWidth(),
                                        fieldHeight = 55.dp,
                                        gapBetween = 5.dp,
                                        labelStartPadding = 16.dp,
                                        placeholder = "Не указана",
                                        contentEndPadding = 10.dp
                                    )

                                    PositionDropdownPopup(
                                        expanded = positionMenuExpanded,
                                        onDismiss = { positionMenuExpanded = false },
                                        width = 155.dp,
                                        maxHeight = positionMenuMaxHeight,
                                        yOffsetFromTop = 82.dp,
                                        scrollState = positionMenuScroll,
                                        options = positionOptions,
                                        onSelect = { option ->
                                            onPositionChange(option)
                                            positionMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "О себе",
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = BluePrimary300,
                            modifier = Modifier.padding(start = 28.dp)
                        )

                        Spacer(Modifier.height(5.14.dp))

                        val aboutScrollState = rememberScrollState()

                        Box(
                            modifier = Modifier
                                .requiredWidth(327.dp)
                                .requiredHeight(352.35.dp)
                                .align(Alignment.CenterHorizontally)
                                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                                .background(Color.White, RoundedCornerShape(18.dp))
                                .padding(14.dp)
                        ) {
                            if (!uiState.isEditing) {
                                val aboutEmpty = uiState.about.isBlank()
                                Text(
                                    text = if (aboutEmpty) "Расскажите немного о себе" else uiState.about,
                                    style = FieldTextStyle,
                                    color = if (aboutEmpty) Gray650 else GroupeakDarkBlue,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(aboutScrollState)
                                        .padding(start = 4.dp)
                                )
                            } else {
                                BasicTextField(
                                    value = uiState.about,
                                    onValueChange = onAboutChange,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(aboutScrollState)
                                        .padding(start = 4.dp),
                                    textStyle = FieldTextStyle.copy(color = GroupeakDarkBlue),
                                    decorationBox = { inner ->
                                        if (uiState.about.isBlank()) {
                                            Text(
                                                text = "Расскажите немного о себе",
                                                style = FieldTextStyle,
                                                color = Gray650
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-39).dp)
                            .size(82.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.im_avatar),
                            contentDescription = "Аватар",
                            modifier = Modifier
                                .size(78.dp)
                                .background(Color.White, CircleShape)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PositionDropdownPopup(
    expanded: Boolean,
    onDismiss: () -> Unit,
    width: Dp,
    maxHeight: Dp,
    yOffsetFromTop: Dp,
    scrollState: androidx.compose.foundation.ScrollState,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    if (!expanded) return

    val density = LocalDensity.current
    val yOffsetPx = with(density) { yOffsetFromTop.roundToPx() }

    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(0, yOffsetPx),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Column(
            modifier = Modifier
                .width(width)
                .heightIn(max = maxHeight)
                .background(Color.White, RoundedCornerShape(18.dp))
                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                .verticalScroll(scrollState)
        ) {
            options.forEach { option ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onSelect(option) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = option,
                        style = FieldTextStyle,
                        color = GroupeakDarkBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    title: String,
    hasNotifications: Boolean,
    onBackClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 55.dp, bottom = 32.dp)
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
            text = title,
            fontSize = 32.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = BluePrimary300,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 19.dp)
                .size(NotifHitBoxSize)
                .clickable { onNotificationsClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notif),
                contentDescription = "Уведомления",
                tint = BluePrimary300,
                modifier = Modifier.size(NotifIconSize)
            )

            if (hasNotifications) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-3).dp, y = 2.dp)
                        .background(Color.Red, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    iconRes: Int,
    value: String,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    fieldHeight: Dp = 55.dp,
    labelSlotHeight: Dp = 22.dp,
    gapBetween: Dp = 8.dp,
    labelStartPadding: Dp = 0.dp,
    iconStartPadding: Dp = 11.dp,
    contentEndPadding: Dp = 14.dp,
    iconSize: Dp = 22.dp,
    iconTextGap: Dp = 12.dp,
    placeholder: String = "",
    placeholderColor: Color = Gray650
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(labelSlotHeight)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = BluePrimary300,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = labelStartPadding)
            )
        }

        Spacer(Modifier.height(gapBetween))

        val showPlaceholder = value.isBlank() && placeholder.isNotBlank()

        Row(
            modifier = fieldModifier
                .fillMaxWidth()
                .height(fieldHeight)
                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                .padding(start = iconStartPadding, end = contentEndPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = BluePrimary300,
                modifier = Modifier.size(iconSize)
            )

            Spacer(Modifier.width(iconTextGap))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (showPlaceholder) placeholder else value,
                    style = FieldTextStyle,
                    color = if (showPlaceholder) placeholderColor else GroupeakDarkBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

@Composable
private fun LabeledInputField(
    label: String,
    iconRes: Int,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    fieldHeight: Dp = 55.dp,
    labelSlotHeight: Dp = 22.dp,
    gapBetween: Dp = 8.dp,
    labelStartPadding: Dp = 0.dp,
    iconStartPadding: Dp = 11.dp,
    contentEndPadding: Dp = 14.dp,
    iconSize: Dp = 22.dp,
    iconTextGap: Dp = 12.dp,
    placeholder: String = ""
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(labelSlotHeight)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = BluePrimary300,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = labelStartPadding)
            )
        }

        Spacer(Modifier.height(gapBetween))

        Row(
            modifier = fieldModifier
                .fillMaxWidth()
                .height(fieldHeight)
                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                .padding(start = iconStartPadding, end = contentEndPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = BluePrimary300,
                modifier = Modifier.size(iconSize)
            )

            Spacer(Modifier.width(iconTextGap))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = FieldTextStyle.copy(color = GroupeakDarkBlue),
                    decorationBox = { inner ->
                        if (value.isBlank() && placeholder.isNotBlank()) {
                            Text(
                                text = placeholder,
                                style = FieldTextStyle,
                                color = Gray650,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        }
                        inner()
                    }
                )
            }
        }
    }
}

@Composable
private fun LabeledSelectField(
    label: String,
    iconRes: Int,
    value: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    fieldHeight: Dp = 55.dp,
    labelSlotHeight: Dp = 22.dp,
    gapBetween: Dp = 8.dp,
    labelStartPadding: Dp = 0.dp,
    iconStartPadding: Dp = 11.dp,
    contentEndPadding: Dp = 10.dp,
    iconSize: Dp = 22.dp,
    iconTextGap: Dp = 12.dp,
    placeholder: String = "Не указана",
    placeholderColor: Color = Gray650
) {
    // ✅ плавнее вращение
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "arrowRotation"
    )

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(labelSlotHeight)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = BluePrimary300,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = labelStartPadding)
            )
        }

        Spacer(Modifier.height(gapBetween))

        val showPlaceholder = value.isBlank()

        Row(
            modifier = fieldModifier
                .fillMaxWidth()
                .height(fieldHeight)
                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                .padding(start = iconStartPadding, end = contentEndPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = BluePrimary300,
                modifier = Modifier.size(iconSize)
            )

            Spacer(Modifier.width(iconTextGap))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (showPlaceholder) placeholder else value,
                    style = FieldTextStyle,
                    color = if (showPlaceholder) placeholderColor else GroupeakDarkBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }

            Box(
                modifier = Modifier
                    .size(25.dp)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_down_arrow),
                    contentDescription = "Открыть список",
                    tint = BluePrimary300,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun BirthDateField(
    iconRes: Int,
    valueDigits: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    fieldHeight: Dp = 55.dp,
    labelSlotHeight: Dp = 22.dp,
    gapBetween: Dp = 8.dp,
    labelStartPadding: Dp = 0.dp,
    iconStartPadding: Dp = 11.dp,
    contentEndPadding: Dp = 14.dp,
    iconSize: Dp = 22.dp,
    iconTextGap: Dp = 12.dp,
    placeholder: String = "ДД.ММ.ГГГГ"
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(labelSlotHeight)
        ) {
            Text(
                text = "Дата рождения",
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = BluePrimary300,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = labelStartPadding)
            )
        }

        Spacer(Modifier.height(gapBetween))

        Row(
            modifier = fieldModifier
                .fillMaxWidth()
                .height(fieldHeight)
                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                .padding(start = iconStartPadding, end = contentEndPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = BluePrimary300,
                modifier = Modifier.size(iconSize)
            )

            Spacer(Modifier.width(iconTextGap))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (!enabled) {
                    val masked = formatBirthDateDigitsToMasked(valueDigits)
                    Text(
                        text = if (masked.isBlank()) placeholder else masked,
                        style = FieldTextStyle,
                        color = if (masked.isBlank()) Gray650 else GroupeakDarkBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                } else {
                    BasicTextField(
                        value = valueDigits,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = DateVisualTransformation,
                        textStyle = FieldTextStyle.copy(color = GroupeakDarkBlue),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        decorationBox = { inner ->
                            if (valueDigits.isBlank()) {
                                Text(
                                    text = placeholder,
                                    style = FieldTextStyle,
                                    color = Gray650,
                                    maxLines = 1,
                                    overflow = TextOverflow.Clip
                                )
                            }
                            inner()
                        }
                    )
                }
            }
        }
    }
}
