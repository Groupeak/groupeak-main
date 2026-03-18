package com.example.groupeak.ui.screens.feedback

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
import androidx.compose.ui.res.colorResource
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
import com.example.groupeak.ui.theme.Gray70717A
import com.example.groupeak.ui.theme.OnestFontFamily
import kotlinx.coroutines.delay

// Размеры из ProfileScreen (иконка уведомлений)
private val NotifHitBoxSize = 28.dp
private val NotifIconSize = 22.dp

// Размеры карточки и полей
private val WhiteCardWidth = 348.dp
private val WhiteCardHeight = 673.dp
private val FieldWidth = 328.dp
private val FieldHeight = 55.dp
private val BigFieldHeight = 381.dp

// Отступы экрана
private val GapHeaderToCard = 30.dp
private val GapCardToBottomBar = 44.dp

// Лейблы должны начинаться на 21dp от левого края карточки.
// Поля центрированы: (348 - 328) / 2 = 10dp, значит добавляем 11dp.
private val LabelStartPaddingFromCard = 11.dp

// Вертикальные отступы внутри карточки
private val TopPaddingToFirstLabel = 19.dp
private val GapLabelToField = 6.dp
private val GapBetweenInputBlocks = 17.dp

// Кнопка и нижняя зона карточки
private val GapFromBigFieldToButton = 19.dp
private val GapFromButtonToCardBottom = 30.dp
private val ButtonHeight = 32.dp
private val ButtonWidth = 154.dp

// Доп. высота карточки при показе текста
private val SentExtraHeight = 21.dp

// Отступы для текста "Сообщение отправлено"
private val GapFromButtonToSentText = 16.dp
private val GapFromSentTextToCardBottom = 17.dp

// Высота зоны текста под кнопкой
private val SentTextHeight: Dp =
    GapFromButtonToCardBottom + SentExtraHeight - GapFromButtonToSentText - GapFromSentTextToCardBottom

// Резерв снизу, чтобы контент не залезал под кнопку
private val BaseReserveBottomForButtonArea: Dp =
    GapFromBigFieldToButton + ButtonHeight + GapFromButtonToCardBottom

private val FieldTextStyle = TextStyle(
    fontSize = 14.sp,
    fontFamily = OnestFontFamily,
    fontWeight = FontWeight.Normal,
    color = GroupeakDarkBlue
)

private val StatusMessageTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 14.sp,
    fontFamily = OnestFontFamily,
    fontWeight = FontWeight.Normal,
)

private val SuccessStatusColor = Color(0xFF26BC30)
private val ErrorStatusColor = Color(0xFFFF0000)

@Composable
fun FeedbackScreen(
    onBack: () -> Unit,
    senderEmail: String = "",
    hasNotifications: Boolean = true,
    onNotificationsClick: () -> Unit = {}
) {
    var subject by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }

    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var showSentText by rememberSaveable { mutableStateOf(false) }
    var sentPulse by rememberSaveable { mutableStateOf(0) }
    var statusText by rememberSaveable { mutableStateOf("Сообщение отправлено") }
    var statusIsError by rememberSaveable { mutableStateOf(false) }

    val statusColor = if (statusIsError) ErrorStatusColor else SuccessStatusColor

    val animatedCardHeight by animateDpAsState(
        targetValue = if (isExpanded) WhiteCardHeight + SentExtraHeight else WhiteCardHeight,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "cardHeight"
    )

    val sentAlpha by animateFloatAsState(
        targetValue = if (showSentText) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "sentAlpha"
    )
    val sentSlide by animateDpAsState(
        targetValue = if (showSentText) 0.dp else 10.dp,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "sentSlide"
    )

    val baseOutsideOffset by animateDpAsState(
        targetValue = if (isExpanded) 0.dp else SentExtraHeight,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "baseOutsideOffset"
    )

    LaunchedEffect(sentPulse) {
        if (sentPulse == 0) return@LaunchedEffect

        isExpanded = true
        delay(140)
        showSentText = true

        delay(2100)

        showSentText = false
        isExpanded = false
    }

    GroupeakBackground {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FeedbackHeader(
                    title = "Обратная связь",
                    hasNotifications = hasNotifications,
                    onBackClick = onBack,
                    onNotificationsClick = onNotificationsClick
                )

                Spacer(Modifier.height(GapHeaderToCard))

                Box(
                    modifier = Modifier
                        .requiredWidth(WhiteCardWidth)
                        .requiredHeight(animatedCardHeight)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .requiredHeight(WhiteCardHeight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = 10.dp,
                                    end = 10.dp,
                                    bottom = BaseReserveBottomForButtonArea
                                )
                        ) {
                            Spacer(Modifier.height(TopPaddingToFirstLabel))

                            LabeledInputField(
                                label = "Электронная почта отправителя",
                                labelStartPadding = LabelStartPaddingFromCard,
                                iconRes = R.drawable.ic_fio,
                                value = senderEmail,
                                valueColor = Gray70717A,
                                onValueChange = { },
                                placeholder = "for@example.com",
                                readOnly = true,
                                fieldModifier = Modifier.requiredWidth(FieldWidth)
                            )

                            Spacer(Modifier.height(GapBetweenInputBlocks))

                            LabeledInputField(
                                label = "Тема сообщения",
                                labelStartPadding = LabelStartPaddingFromCard,
                                iconRes = R.drawable.ic_email,
                                value = subject,
                                onValueChange = {
                                    subject = it
                                    if (showSentText || isExpanded) {
                                        showSentText = false
                                        isExpanded = false
                                    }
                                },
                                placeholder = "Проблема со входом в аккаунт",
                                readOnly = false,
                                fieldModifier = Modifier.requiredWidth(FieldWidth)
                            )

                            Spacer(Modifier.height(GapBetweenInputBlocks))

                            MultilineFieldScrollable(
                                placeholder = "Опишите проблему",
                                value = message,
                                onValueChange = {
                                    message = it
                                    if (showSentText || isExpanded) {
                                        showSentText = false
                                        isExpanded = false
                                    }
                                },
                                fieldModifier = Modifier
                                    .requiredWidth(FieldWidth)
                                    .requiredHeight(BigFieldHeight)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = GapFromButtonToCardBottom),
                            contentAlignment = Alignment.Center
                        ) {
                            SendButton154x32(
                                text = "Отправить",
                                onClick = {
                                    val isSubjectEmpty = subject.trim().isEmpty()
                                    val isMessageEmpty = message.trim().isEmpty()

                                    if (isSubjectEmpty || isMessageEmpty) {
                                        statusText = "Введите тему и описание обращения"
                                        statusIsError = true
                                    } else {
                                        statusText = "Сообщение отправлено"
                                        statusIsError = false
                                    }

                                    sentPulse += 1
                                }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = GapFromSentTextToCardBottom)
                            .requiredHeight(SentTextHeight)
                            .offset(y = baseOutsideOffset + sentSlide)
                            .graphicsLayer { alpha = sentAlpha },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = statusText,
                            style = StatusMessageTextStyle.copy(color = statusColor),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .wrapContentHeight(unbounded = true)
                                .offset(y = (-0.5).dp)
                        )
                    }
                }

                Spacer(Modifier.height(GapCardToBottomBar))
            }
        }
    }
}


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
    fieldModifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        Box(
            modifier = fieldModifier
                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                singleLine = false,
                textStyle = FieldTextStyle.copy(lineHeight = 18.sp),
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
