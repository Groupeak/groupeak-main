package com.example.groupeak.ui.screens.createproject

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.GroupeakDarkBlue
import com.example.groupeak.ui.theme.GroupeakLightGray
import com.example.groupeak.ui.theme.Gray650
import com.example.groupeak.ui.theme.OnestFontFamily
import kotlinx.coroutines.delay

// ================= РАЗМЕРЫ =================

private val NotifHitBoxSize = 28.dp
private val NotifIconSize = 22.dp

private val WhiteCardWidth = 348.dp
private val WhiteCardHeight = 504.dp
private val FieldWidth = 328.dp
private val FieldHeight = 55.dp
private val BigFieldHeight = 202.dp

private val GapHeaderToCard = 30.dp
private val GapCardToBottomBar = 44.dp

private val LabelStartPaddingFromCard = 11.dp

private val TopPaddingToFirstLabel = 12.dp
private val GapLabelToField = 6.dp
private val GapBetweenInputBlocks = 12.dp

private val GapFromBigFieldToButton = 12.dp
private val GapFromButtonToCardBottom = 30.dp
private val ButtonHeight = 32.dp
private val ButtonWidth = 154.dp

private val SentExtraHeight = 36.dp

private val GapFromButtonToSentText = 16.dp
private val GapFromSentTextToCardBottom = 17.dp
private val SentTextHeight: Dp =
    GapFromButtonToCardBottom + SentExtraHeight - GapFromButtonToSentText - GapFromSentTextToCardBottom

private val BaseReserveBottomForButtonArea: Dp =
    GapFromBigFieldToButton + ButtonHeight + GapFromButtonToCardBottom

private val CardVerticalOffset = 90.dp

private val FieldTextStyle = TextStyle(
    fontSize = 14.sp,
    fontFamily = OnestFontFamily,
    fontWeight = FontWeight.Normal,
    color = GroupeakDarkBlue
)

private val SentMessageTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 14.sp,
    fontFamily = OnestFontFamily,
    fontWeight = FontWeight.Normal,
    color = Color(0xFF26BC30)
)

// ================= ОСНОВНОЙ ЭКРАН =================

@Composable
fun CreateProjectScreen(
    onBack: () -> Unit,
    hasNotifications: Boolean = true,
    onNotificationsClick: () -> Unit = {}
) {
    var projectName by rememberSaveable { mutableStateOf("") }
    var deadline by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var showCreatedText by rememberSaveable { mutableStateOf(false) }
    var createdPulse by rememberSaveable { mutableStateOf(0) }

    val animatedCardHeight by animateDpAsState(
        targetValue = if (isExpanded) WhiteCardHeight + SentExtraHeight else WhiteCardHeight,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "cardHeight"
    )

    val createdAlpha by animateFloatAsState(
        targetValue = if (showCreatedText) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "createdAlpha"
    )

    val createdSlide by animateDpAsState(
        targetValue = if (showCreatedText) 0.dp else 10.dp,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "createdSlide"
    )

    LaunchedEffect(createdPulse) {
        if (createdPulse == 0) return@LaunchedEffect

        isExpanded = true
        delay(140)
        showCreatedText = true

        delay(2100)

        showCreatedText = false
        isExpanded = false
    }

    GroupeakBackground {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Шапка
                FeedbackHeader(
                    title = "Создание проекта",
                    hasNotifications = hasNotifications,
                    onBackClick = onBack,
                    onNotificationsClick = onNotificationsClick
                )

                Spacer(Modifier.height(GapHeaderToCard))

                // 🔷 ОБЛАСТЬ ЦЕНТРИРОВАНИЯ КАРТОЧКИ
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter  // ← Фиксируем верх
                ) {
                    // Белая карточка
                    Box(
                        modifier = Modifier
                            .padding(top = CardVerticalOffset)  // ← Визуальное центрирование
                            .requiredWidth(WhiteCardWidth)
                            .requiredHeight(animatedCardHeight)  // ← Анимация только высоты
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp)
                        ) {

                            Spacer(Modifier.height(TopPaddingToFirstLabel))

                            LabeledInputField(
                                label = "Название",
                                labelStartPadding = LabelStartPaddingFromCard,
                                iconRes = R.drawable.ic_folder,
                                value = projectName,
                                onValueChange = {
                                    projectName = it
                                    if (showCreatedText || isExpanded) {
                                        showCreatedText = false
                                        isExpanded = false
                                    }
                                },
                                placeholder = "Название проекта",
                                readOnly = false,
                                fieldModifier = Modifier.requiredWidth(FieldWidth)
                            )

                            Spacer(Modifier.height(GapBetweenInputBlocks))

                            LabeledInputField(
                                label = "Дедлайн",
                                labelStartPadding = LabelStartPaddingFromCard,
                                iconRes = R.drawable.ic_calendar,
                                value = deadline,
                                onValueChange = {
                                    deadline = it
                                    if (showCreatedText || isExpanded) {
                                        showCreatedText = false
                                        isExpanded = false
                                    }
                                },
                                placeholder = "ДД.ММ.ГГГГ",
                                readOnly = false,
                                fieldModifier = Modifier.requiredWidth(FieldWidth)
                            )

                            Spacer(Modifier.height(GapBetweenInputBlocks))

                            Text(
                                text = "Описание",
                                fontSize = 14.sp,
                                fontFamily = OnestFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = BluePrimary300,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = LabelStartPaddingFromCard)
                            )

                            Spacer(modifier = Modifier.height(GapLabelToField))

                            MultilineFieldScrollable(
                                placeholder = "Описание проекта",
                                value = description,
                                onValueChange = {
                                    description = it
                                    if (showCreatedText || isExpanded) {
                                        showCreatedText = false
                                        isExpanded = false
                                    }
                                },
                                fieldModifier = Modifier.requiredWidth(FieldWidth)
                            )

                            Spacer(modifier = Modifier.height(GapFromBigFieldToButton))

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                SendButton154x32(
                                    text = "Создать",
                                    onClick = { createdPulse += 1 }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(SentTextHeight)
                                    .graphicsLayer { alpha = createdAlpha },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Проект создан",
                                    style = SentMessageTextStyle,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(GapFromButtonToCardBottom))
                        }
                    }
                }

                Spacer(Modifier.height(GapCardToBottomBar))
            }
        }
    }
}

// ================= ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ =================

@Composable
private fun FeedbackHeader(
    title: String,
    hasNotifications: Boolean,
    onBackClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 55.dp)
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
private fun LabeledInputField(
    label: String,
    labelStartPadding: Dp,
    iconRes: Int,
    value: String,
    valueColor: Color = GroupeakDarkBlue,
    onValueChange: (String) -> Unit,
    placeholder: String,
    readOnly: Boolean,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    fieldHeight: Dp = FieldHeight
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = BluePrimary300,
            modifier = Modifier.padding(start = labelStartPadding)
        )

        Spacer(Modifier.height(GapLabelToField))

        Row(
            modifier = fieldModifier
                .requiredHeight(fieldHeight)
                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                .padding(start = 11.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = BluePrimary300,
                modifier = Modifier.size(22.dp)
            )

            Spacer(Modifier.width(12.dp))

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
                    readOnly = readOnly,
                    textStyle = FieldTextStyle.copy(color = valueColor),
                    decorationBox = { inner ->
                        if (value.isBlank()) {
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
private fun MultilineFieldScrollable(
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    fieldHeight: Dp = BigFieldHeight
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        Box(
            modifier = fieldModifier
                .requiredHeight(fieldHeight)
                .border(
                    1.dp,
                    GroupeakLightGray,
                    RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                singleLine = false,
                textStyle = FieldTextStyle,
                decorationBox = { inner ->
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
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

@Composable
private fun SendButton154x32(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .requiredWidth(ButtonWidth)
            .requiredHeight(ButtonHeight),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BluePrimary300,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.SemiBold
        )
    }
}