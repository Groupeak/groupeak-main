package com.example.groupeak.ui.screens.change_email

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.components.PrimaryButton
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.GroupeakDarkBlue
import com.example.groupeak.ui.theme.GroupeakLightGray
import com.example.groupeak.ui.theme.Gray650
import com.example.groupeak.ui.theme.Gray70717A
import com.example.groupeak.ui.theme.OnestFontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

// === Шапка (как в ProfileScreen) ===
@Composable
private fun CommonHeader(
    title: String,
    hasNotifications: Boolean = false,
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
                .size(28.dp)
                .clickable { onNotificationsClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notif),
                contentDescription = "Уведомления",
                tint = BluePrimary300,
                modifier = Modifier.size(22.dp)
            )

            if (hasNotifications) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-3).dp, y = 2.dp)
                        .background(Color.Red, RoundedCornerShape(50))
                )
            }
        }
    }
}

// === LabeledField (только чтение) ===
@Composable
private fun LabeledField(
    label: String,
    iconRes: Int,
    value: String,
    valueColor: Color = GroupeakDarkBlue,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = BluePrimary300,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .border(
                    width = 1.dp,
                    color = GroupeakLightGray,
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
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = valueColor,
                maxLines = 1
            )
        }
    }
}

// === LabeledInputField (редактируемое поле) ===
@Composable
private fun LabeledInputField(
    label: String,
    iconRes: Int,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = if (isError && errorMessage != null) errorMessage else label,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = if (isError) Color.Red else BluePrimary300,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .border(
                    width = 1.dp,
                    color = if (isError) Color.Red else GroupeakLightGray,
                    shape = RoundedCornerShape(18.dp)
                )
                .background(Color.White, RoundedCornerShape(18.dp))
                .padding(start = 16.dp, end = 16.dp),
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = GroupeakDarkBlue
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            decorationBox = { inner ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = BluePrimary300,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                fontFamily = OnestFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = Gray650
                            )
                        }
                        inner()
                    }
                }
            }
        )
    }
}

@Composable
private fun LabeledInputField(
    label: String,
    iconRes: Int,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false
) {
    Column(modifier = modifier) {
        // label всегда синий
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = BluePrimary300,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .border(
                    width = 1.dp,
                    color = if (isError) Color.Red else GroupeakLightGray,
                    shape = RoundedCornerShape(18.dp)
                )
                .background(Color.White, RoundedCornerShape(18.dp))
                .padding(start = 16.dp, end = 16.dp),
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = GroupeakDarkBlue
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            decorationBox = { inner ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = BluePrimary300,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                fontFamily = OnestFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = Gray650
                            )
                        }
                        inner()
                    }
                }
            }
        )
    }
}

@Composable
private fun LabeledPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false
) {
    Column(modifier = modifier) {
        // label всегда синий
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = BluePrimary300,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .width(328.dp)
                .height(55.dp)
                .border(
                    width = 1.dp,
                    color = if (isError) Color.Red else GroupeakLightGray,
                    shape = RoundedCornerShape(18.dp)
                )
                .background(Color.White, RoundedCornerShape(18.dp))
                .padding(start = 16.dp, end = 16.dp)
                .align(Alignment.CenterHorizontally),
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = GroupeakDarkBlue
            ),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            decorationBox = { inner ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock),
                        contentDescription = null,
                        tint = BluePrimary300,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                fontFamily = OnestFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = Gray650
                            )
                        }
                        inner()
                    }

                    Icon(
                        painter = painterResource(
                            id = if (isPasswordVisible) R.drawable.ic_eye_on else R.drawable.ic_eye_off
                        ),
                        contentDescription = null,
                        tint = BluePrimary300,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onToggleVisibility() }
                    )
                }
            }
        )
    }
}
// ==========================================

@Composable
fun ChangeEmailScreen(
    uiState: ChangeEmailUiState,
    onBackClick: () -> Unit,
    onNewEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onChangeClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    val CardBaseHeight = 378.dp
    val CardExtraHeight = 28.dp
    val CardWidth = 348.dp
    val FieldWidth = 328.dp
    val FieldHeight = 55.dp
    val ButtonWidth = 154.dp
    val ButtonHeight = 32.dp

    // анимационный триггер
    var pulse by rememberSaveable { mutableStateOf(0) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var showText by rememberSaveable { mutableStateOf(false) }

    val hasError = uiState.newEmailError != null || uiState.passwordError != null
    val errorMessage = uiState.newEmailError ?: uiState.passwordError

    // высота карточки
    val cardHeight by animateDpAsState(
        targetValue = if (isExpanded) CardBaseHeight + CardExtraHeight else CardBaseHeight,
        animationSpec = tween(400, easing = FastOutSlowInEasing)
    )

    // анимация текста
    val textAlpha by animateFloatAsState(
        targetValue = if (showText) 1f else 0f,
        animationSpec = tween(260, easing = FastOutSlowInEasing)
    )
    val textOffset by animateDpAsState(
        targetValue = if (showText) 0.dp else 10.dp,
        animationSpec = tween(280, easing = FastOutSlowInEasing)
    )

    // эффект для анимации успешной смены / ошибки
    LaunchedEffect(pulse) {
        if (pulse == 0) return@LaunchedEffect
        isExpanded = true
        kotlinx.coroutines.delay(140)
        showText = true
        kotlinx.coroutines.delay(2100)
        showText = false
        kotlinx.coroutines.delay(280)
        isExpanded = false
    }

    GroupeakBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CommonHeader(
                title = "Смена почты",
                onBackClick = onBackClick,
                onNotificationsClick = onNotificationsClick
            )

            Spacer(modifier = Modifier.height(150.dp))

            Box(
                modifier = Modifier
                    .width(CardWidth)
                    .height(cardHeight)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(19.dp))

                    LabeledField(
                        label = "Текущая почта",
                        iconRes = R.drawable.ic_email,
                        value = uiState.currentEmail,
                        valueColor = Gray70717A
                    )

                    Spacer(Modifier.height(11.dp))

                    LabeledInputField(
                        label = "Новая почта",
                        iconRes = R.drawable.ic_email,
                        value = uiState.newEmail,
                        onValueChange = onNewEmailChange,
                        placeholder = "Введите новую почту",
                        isError = uiState.newEmailError != null,
                    )

                    Spacer(Modifier.height(11.dp))

                    LabeledPasswordField(
                        label = "Подтверждение изменений паролем",
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        isPasswordVisible = uiState.isPasswordVisible,
                        onToggleVisibility = onTogglePasswordVisibility,
                        placeholder = "Введите пароль",
                        isError = uiState.passwordError != null,
                    )

                    Spacer(Modifier.height(19.dp))

                    PrimaryButton(
                        modifier = Modifier
                            .width(ButtonWidth)
                            .height(ButtonHeight)
                            .align(Alignment.CenterHorizontally),
                        text = "Изменить",
                        onClick = {
                            if (!uiState.isLoading) {
                                onChangeClick()
                                pulse += 1
                            }
                        },
                        fontSize = 14.sp
                    )

                    // текст успешного изменения или ошибки
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .graphicsLayer { alpha = textAlpha }
                            .offset(y = textOffset),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                uiState.isSuccess -> "Почта изменена"
                                hasError && errorMessage != null -> errorMessage
                                else -> ""
                            },
                            color = if (uiState.isSuccess) Color(0xFF26BC30) else Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}