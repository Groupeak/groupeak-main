package com.example.groupeak.ui.screens.profile

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.GroupeakDarkBlue
import com.example.groupeak.ui.theme.GroupeakLightGray
import com.example.groupeak.ui.theme.Gray650
import com.example.groupeak.ui.theme.OnestFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
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

// Размеры карточки профиля
private val WhiteCardWidth = 348.dp
private val WhiteCardHeight = 633.dp

// Доп. высота карточки при показе текста "Изменения сохранены" (как в FeedbackScreen)
private val SavedExtraHeight = 40.dp

private val MessageTextStyleBase = TextStyle(
    fontSize = 14.sp,
    lineHeight = 14.sp,
    fontFamily = OnestFontFamily,
    fontWeight = FontWeight.Normal
)

private val SavedMessageColor = Color(0xFF26BC30)
private val CancelledMessageColor = Color(0xFFFF0000)

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
    onCancelEdit: () -> Unit,
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

    var showCancelDialog by remember { mutableStateOf(false) }

    // Анимация текста снизу (как в FeedbackScreen)
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var showMessageText by rememberSaveable { mutableStateOf(false) }
    var messageKind by rememberSaveable { mutableStateOf(0) } // 0 = saved, 1 = canceled
    var messagePulse by rememberSaveable { mutableStateOf(0) }

    val messageText = if (messageKind == 1) "Изменения отменены" else "Изменения сохранены"
    val messageColor = if (messageKind == 1) CancelledMessageColor else SavedMessageColor

    val animatedCardHeight by animateDpAsState(
        targetValue = if (isExpanded) WhiteCardHeight + SavedExtraHeight else WhiteCardHeight,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "cardHeight"
    )

    val savedAlpha by animateFloatAsState(
        targetValue = if (showMessageText) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "savedAlpha"
    )
    val savedSlide by animateDpAsState(
        targetValue = if (showMessageText) 0.dp else 10.dp,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "savedSlide"
    )

    val baseOutsideOffset by animateDpAsState(
        targetValue = if (isExpanded) 0.dp else SavedExtraHeight,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "baseOutsideOffset"
    )

    LaunchedEffect(messagePulse) {
        if (messagePulse == 0) return@LaunchedEffect

        isExpanded = true
        delay(140)
        showMessageText = true

        delay(2100)

        showMessageText = false
        isExpanded = false
    }

    GroupeakBackground {
        Box(modifier = Modifier.fillMaxSize()) {

            if (showCancelDialog) {
                CancelChangesDialog(
                    onDismiss = { showCancelDialog = false },
                    onConfirm = {
                        showCancelDialog = false
                        positionMenuExpanded = false
                        messageKind = 1
                        messagePulse += 1
                        onCancelEdit()
                    }
                )
            }

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
                    Box(
                        modifier = Modifier
                            .requiredWidth(WhiteCardWidth)
                            .requiredHeight(animatedCardHeight)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                    ) {
                        // Основное содержимое (фикс высота), чтобы анимация текста снизу не ломала верстку
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .requiredHeight(WhiteCardHeight)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 11.dp)
                            ) {
                                if (uiState.isEditing) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 20.dp, end = 20.dp, top = 11.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Отмена",
                                            fontSize = 14.sp,
                                            fontFamily = OnestFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = Gray650,
                                            modifier = Modifier.clickable { showCancelDialog = true }
                                        )

                                        Text(
                                            text = "Сохранить",
                                            fontSize = 14.sp,
                                            fontFamily = OnestFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            color = BluePrimary300,
                                            modifier = Modifier.clickable {
                                                positionMenuExpanded = false
                                                onSaveClick()
                                                messageKind = 0
                                                messagePulse += 1
                                            }
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Изменить",
                                        fontSize = 14.sp,
                                        fontFamily = OnestFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        color = BluePrimary300,
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .padding(end = 20.dp, top = 11.dp)
                                            .clickable {
                                                positionMenuExpanded = false
                                                showMessageText = false
                                                isExpanded = false
                                                onEditClick()
                                            }
                                    )
                                }

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
                                            labelStartPadding = 16.dp,
                                            iconTextGap = 8.dp
                                        )
                                    } else {
                                        LabeledPositionSelectOverlay(
                                            label = "Должность",
                                            iconRes = R.drawable.ic_briefcase,
                                            value = uiState.position,
                                            expanded = positionMenuExpanded,
                                            onExpandedChange = { newValue ->
                                                positionMenuExpanded = newValue
                                                if (newValue) onPositionClick()
                                            },
                                            options = positionOptions,
                                            onSelect = { option ->
                                                onPositionChange(option)
                                                positionMenuExpanded = false
                                            },
                                            modifier = Modifier.requiredWidth(155.dp),
                                            fieldHeight = 55.dp,
                                            expandedHeight = 145.5.dp,
                                            gapBetween = 5.dp,
                                            labelStartPadding = 16.dp,
                                            placeholder = "Не указана",
                                            placeholderColor = Gray650,
                                            iconTextGap = 8.dp
                                        )
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
                        }

                        // Текст статуса (снизу) (снизу, с fade+slide, как в FeedbackScreen)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 17.dp)
                                .offset(y = baseOutsideOffset + savedSlide)
                                .graphicsLayer { alpha = savedAlpha },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = messageText,
                                style = MessageTextStyleBase.copy(color = messageColor),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier
                                    .wrapContentHeight(unbounded = true)
                                    .offset(y = (-0.5).dp)
                            )
                        }
                    }

                    // Аватар (поверх карточки)
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
private fun CancelChangesDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .size(width = 215.dp, height = 111.dp)
                .background(Color.White, RoundedCornerShape(18.dp))
        ) {
            // Заголовок: отступ сверху 21dp, по центру X
            // Фиксируем высоту 18dp, чтобы отступ до кнопок был строго 27dp.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 21.dp)
                    .height(18.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "Отменить изменения?",
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF000000),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            // Кнопки: 88x32, скругление 18
            // Отступы: слева 13, между 13, справа 13; снизу 13.
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 13.dp, end = 13.dp, bottom = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Да (заливка)
                Box(
                    modifier = Modifier
                        .size(width = 88.dp, height = 32.dp)
                        .background(BluePrimary300, RoundedCornerShape(18.dp))
                        .clickable { onConfirm() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Да",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }

                // Нет (контур)
                Box(
                    modifier = Modifier
                        .size(width = 88.dp, height = 32.dp)
                        .border(1.dp, BluePrimary300, RoundedCornerShape(18.dp))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = BluePrimary300
                    )
                }
            }
        }
    }
}





@Composable
private fun LabeledPositionSelectOverlay(
    label: String,
    iconRes: Int,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    fieldHeight: Dp = 55.dp,
    expandedHeight: Dp = 145.5.dp,
    labelSlotHeight: Dp = 22.dp,
    gapBetween: Dp = 5.dp,
    labelStartPadding: Dp = 16.dp,
    iconStartPadding: Dp = 11.dp,
    contentEndPadding: Dp = 10.dp,
    iconSize: Dp = 22.dp,
    iconTextGap: Dp = 12.dp,
    placeholder: String = "Не указана",
    placeholderColor: Color = Gray650
) {
    val animDuration = 260

    val scope = rememberCoroutineScope()
    var isClosing by remember { mutableStateOf(false) }

    val showPopup = expanded || isClosing

    fun open() {
        isClosing = false
        onExpandedChange(true)
    }

    fun close() {
        if (!expanded || isClosing) return
        isClosing = true
        onExpandedChange(false)
        scope.launch {
            delay(animDuration.toLong())
            isClosing = false
        }
    }
    val containerHeight by animateDpAsState(
        targetValue = if (expanded) expandedHeight else fieldHeight,
        animationSpec = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
        label = "positionOverlayHeight"
    )

    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
        label = "positionOverlayArrowRotation"
    )

    val dropdownHeight = (containerHeight - fieldHeight).coerceAtLeast(0.dp)
    val listScroll = rememberScrollState()

    Column(modifier = modifier) {
        // label
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

        // Якорь: в верстке всегда только 55dp (ничего не двигается)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
        ) {
            // Обычное поле (видимо, когда popup не показан)
            PositionTopRow(
                iconRes = iconRes,
                value = value,
                isExpanded = expanded,
                rotation = arrowRotation,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = if (showPopup) 0f else 1f },
                iconStartPadding = iconStartPadding,
                contentEndPadding = contentEndPadding,
                iconSize = iconSize,
                iconTextGap = iconTextGap,
                placeholder = placeholder,
                placeholderColor = placeholderColor,
                drawBorder = true,
                onClick = { if (expanded) close() else open() }
            )

            if (showPopup) {
                val positionProvider = remember {
                    object : PopupPositionProvider {
                        override fun calculatePosition(
                            anchorBounds: IntRect,
                            windowSize: IntSize,
                            layoutDirection: LayoutDirection,
                            popupContentSize: IntSize
                        ): IntOffset {
                            // фиксируемся строго в верхнем левом углу поля "Должность"
                            return IntOffset(anchorBounds.left, anchorBounds.top)
                        }
                    }
                }

                Popup(
                    popupPositionProvider = positionProvider,
                    onDismissRequest = { close() },
                    properties = PopupProperties(focusable = false, dismissOnClickOutside = true, dismissOnBackPress = true)
                ) {
                    // Важно: ширина совпадает с полем (в дизайне 155dp)
                    Box(
                        modifier = Modifier
                            .requiredWidth(155.dp)
                            .height(containerHeight)
                            .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Верхняя часть поля
                            PositionTopRow(
                                iconRes = iconRes,
                                value = value,
                                isExpanded = true,
                                rotation = arrowRotation,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(fieldHeight),
                                iconStartPadding = iconStartPadding,
                                contentEndPadding = contentEndPadding,
                                iconSize = iconSize,
                                iconTextGap = iconTextGap,
                                placeholder = placeholder,
                                placeholderColor = placeholderColor,
                                drawBorder = false,
                                onClick = { close() }
                            )

                            // Выпадающая часть: растёт вниз
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dropdownHeight)
                                    .clipToBounds()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(listScroll),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val listTextStartPadding = iconStartPadding + iconSize + iconTextGap
                                    val listEndPadding = contentEndPadding + 24.dp
                                    val dividerWidth = 107.dp
                                    options.forEachIndexed { index, option ->                                        // Текст списка выравниваем по началу значения (как "Не указана")
                                        val rowStartPadding = listTextStartPadding

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onSelect(option)
                                                    close()
                                                }
                                                .padding(start = rowStartPadding, end = listEndPadding),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = option,
                                                style = FieldTextStyle,
                                                color = GroupeakDarkBlue,
                                                textAlign = TextAlign.Start,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        if (index != options.lastIndex) {
                                            Spacer(Modifier.height(7.dp)) // от надписи до линии

                                            // Линия 107dp, отступ слева 23dp от белого блока
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 23.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(dividerWidth)
                                                        .height(1.dp)
                                                        .background(Color(0xFF969696))
                                                )
                                            }

                                            Spacer(Modifier.height(7.dp)) // между надписями 14
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PositionTopRow(
    iconRes: Int,
    value: String,
    isExpanded: Boolean,
    rotation: Float,
    modifier: Modifier = Modifier,
    iconStartPadding: Dp,
    contentEndPadding: Dp,
    iconSize: Dp,
    iconTextGap: Dp,
    placeholder: String,
    placeholderColor: Color,
    drawBorder: Boolean,
    onClick: () -> Unit
) {
    val rowModifier = if (drawBorder) {
        modifier
            .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
            .background(Color.White, RoundedCornerShape(18.dp))
    } else {
        modifier
    }

    Row(
        modifier = rowModifier
            .padding(start = iconStartPadding, end = contentEndPadding)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = BluePrimary300,
            modifier = Modifier.size(iconSize)
        )

        Spacer(Modifier.width(iconTextGap))

        val showPlaceholder = value.isBlank()
        Text(
            text = if (showPlaceholder) placeholder else value,
            style = FieldTextStyle,
            color = if (showPlaceholder) placeholderColor else GroupeakDarkBlue,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

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