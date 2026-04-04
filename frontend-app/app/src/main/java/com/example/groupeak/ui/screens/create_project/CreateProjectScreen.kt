package com.example.groupeak.ui.screens.create_project

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.Gray650
import com.example.groupeak.ui.theme.GroupeakDarkBlue
import com.example.groupeak.ui.theme.GroupeakLightGray
import com.example.groupeak.ui.theme.OnestFontFamily
import kotlinx.coroutines.delay

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
                val original = offset.coerceIn(0, raw.length)
                var transformed = original
                if (raw.length > 2 && original > 2) transformed += 1
                if (raw.length > 4 && original > 4) transformed += 1
                return transformed.coerceIn(0, out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val transformed = offset.coerceIn(0, out.length)
                var original = transformed
                if (raw.length > 2 && transformed > 2) original -= 1
                if (raw.length > 4 && transformed > 5) original -= 1
                return original.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

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

private val CardVerticalOffset = 90.dp

private val FieldTextStyle = TextStyle(
    fontSize = 14.sp,
    fontFamily = OnestFontFamily,
    fontWeight = FontWeight.Normal,
    color = GroupeakDarkBlue,
)

private val StatusMessageTextStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 16.sp,
    fontFamily = OnestFontFamily,
    fontWeight = FontWeight.Normal,
)

private val ErrorTextStyle = TextStyle(
    fontSize = 12.sp,
    fontFamily = OnestFontFamily,
    fontWeight = FontWeight.Normal,
    color = Color(0xFFD32F2F),
)

@Composable
fun CreateProjectScreen(
    uiState: CreateProjectUiState,
    onBack: () -> Unit,
    onProjectNameChange: (String) -> Unit,
    onDeadlineChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    hasNotifications: Boolean = true,
    onNotificationsClick: () -> Unit = {},
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var showStatusText by rememberSaveable { mutableStateOf(false) }

    val animatedCardHeight by animateDpAsState(
        targetValue = if (isExpanded) WhiteCardHeight + SentExtraHeight else WhiteCardHeight,
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
        delay(140)
        showStatusText = true

        if (!uiState.statusMessageIsError) {
            delay(2100)
            showStatusText = false
            isExpanded = false
        }
    }

    GroupeakBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FeedbackHeader(
                    title = "Создание проекта",
                    hasNotifications = hasNotifications,
                    onBackClick = onBack,
                    onNotificationsClick = onNotificationsClick,
                )

                Spacer(Modifier.height(GapHeaderToCard))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = CardVerticalOffset)
                            .requiredWidth(WhiteCardWidth)
                            .requiredHeight(animatedCardHeight)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                        ) {
                            Spacer(Modifier.height(TopPaddingToFirstLabel))

                            LabeledInputField(
                                label = "Название",
                                labelStartPadding = LabelStartPaddingFromCard,
                                iconRes = R.drawable.ic_folder,
                                value = uiState.projectName,
                                onValueChange = {
                                    onProjectNameChange(it)
                                    if (showStatusText || isExpanded) {
                                        showStatusText = false
                                        isExpanded = false
                                    }
                                },
                                placeholder = "Название проекта",
                                readOnly = false,
                                error = uiState.projectNameError,
                                fieldModifier = Modifier.requiredWidth(FieldWidth),
                            )

                            Spacer(Modifier.height(GapBetweenInputBlocks))

                            LabeledInputField(
                                label = "Дедлайн",
                                labelStartPadding = LabelStartPaddingFromCard,
                                iconRes = R.drawable.ic_calendar,
                                value = uiState.deadline,
                                onValueChange = {
                                    onDeadlineChange(it)
                                    if (showStatusText || isExpanded) {
                                        showStatusText = false
                                        isExpanded = false
                                    }
                                },
                                placeholder = "ДД.ММ.ГГГГ",
                                readOnly = false,
                                error = uiState.deadlineError,
                                keyboardType = KeyboardType.Number,
                                visualTransformation = DateVisualTransformation,
                                fieldModifier = Modifier.requiredWidth(FieldWidth),
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
                                    .padding(start = LabelStartPaddingFromCard),
                            )

                            Spacer(modifier = Modifier.height(GapLabelToField))

                            MultilineFieldScrollable(
                                placeholder = "Описание проекта",
                                value = uiState.description,
                                onValueChange = {
                                    onDescriptionChange(it)
                                    if (showStatusText || isExpanded) {
                                        showStatusText = false
                                        isExpanded = false
                                    }
                                },
                                fieldModifier = Modifier.requiredWidth(FieldWidth),
                            )

                            Spacer(modifier = Modifier.height(GapFromBigFieldToButton))

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                SendButton154x32(
                                    text = if (uiState.isLoading) "Создание..." else "Создать",
                                    enabled = !uiState.isLoading,
                                    onClick = onCreateClick,
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(SentTextHeight)
                                    .graphicsLayer { alpha = statusAlpha }
                                    .offset(y = statusSlide),
                                contentAlignment = Alignment.Center,
                            ) {
                                val statusMessage = uiState.statusMessage.orEmpty()
                                if (statusMessage.isNotBlank()) {
                                    Text(
                                        text = statusMessage,
                                        style = StatusMessageTextStyle.copy(
                                            color = if (uiState.statusMessageIsError) {
                                                Color(0xFFD32F2F)
                                            } else {
                                                Color(0xFF26BC30)
                                            }
                                        ),
                                        textAlign = TextAlign.Center,
                                    )
                                }
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

@Composable
private fun FeedbackHeader(
    title: String,
    hasNotifications: Boolean,
    onBackClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 55.dp),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back_arrow),
            contentDescription = "Назад",
            tint = BluePrimary300,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .size(28.dp)
                .clickable { onBackClick() },
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
                .fillMaxWidth(),
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 19.dp)
                .size(NotifHitBoxSize)
                .clickable { onNotificationsClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notif),
                contentDescription = "Уведомления",
                tint = BluePrimary300,
                modifier = Modifier.size(NotifIconSize),
            )

            if (hasNotifications) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-3).dp, y = 2.dp)
                        .background(Color.Red, CircleShape),
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
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    fieldHeight: Dp = FieldHeight,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = BluePrimary300,
            modifier = Modifier.padding(start = labelStartPadding),
        )

        Spacer(Modifier.height(GapLabelToField))

        Row(
            modifier = fieldModifier
                .requiredHeight(fieldHeight)
                .border(1.dp, GroupeakLightGray, RoundedCornerShape(18.dp))
                .padding(start = 11.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = BluePrimary300,
                modifier = Modifier.size(22.dp),
            )

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = readOnly,
                    textStyle = FieldTextStyle.copy(color = valueColor),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = visualTransformation,
                    decorationBox = { inner ->
                        if (value.isBlank()) {
                            Text(
                                text = placeholder,
                                style = FieldTextStyle,
                                color = Gray650,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                            )
                        }
                        inner()
                    },
                )
            }
        }

        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                style = ErrorTextStyle,
                modifier = Modifier.padding(start = labelStartPadding),
            )
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
    fieldHeight: Dp = BigFieldHeight,
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        Box(
            modifier = fieldModifier
                .requiredHeight(fieldHeight)
                .border(
                    1.dp,
                    GroupeakLightGray,
                    RoundedCornerShape(18.dp),
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
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
                            color = Gray650,
                        )
                    }
                    inner()
                },
            )
        }
    }
}

@Composable
private fun SendButton154x32(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .requiredWidth(ButtonWidth)
            .requiredHeight(ButtonHeight),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BluePrimary300,
            contentColor = Color.White,
            disabledContainerColor = BluePrimary300.copy(alpha = 0.55f),
            disabledContentColor = Color.White.copy(alpha = 0.9f),
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.SemiBold,
        )
    }
}