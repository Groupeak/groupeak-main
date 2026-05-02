package com.example.groupeak.ui.screens.change_password

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.theme.*

private val CardBaseHeight = 378.dp
private val CardExtraHeight = 28.dp
private val CardWidth = 348.dp
private val FieldWidth = 328.dp
private val FieldHeight = 55.dp
private val ButtonWidth = 154.dp
private val ButtonHeight = 32.dp
private val FieldTextStyle = TextStyle(fontSize = 14.sp, fontFamily = OnestFontFamily, color = GroupeakDarkBlue)

@Composable
fun ChangePasswordScreen(
    uiState: ChangePasswordUiState,
    onBackClick: () -> Unit,
    onOldPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onChangeClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onToggleOldPasswordVisibility: () -> Unit,
    onToggleNewPasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit
) {
    // внутренний триггер для анимации
    var pulse by rememberSaveable { mutableStateOf(0) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var showText by rememberSaveable { mutableStateOf(false) }

    val hasError = uiState.oldPasswordError != null ||
            uiState.newPasswordError != null ||
            uiState.confirmPasswordError != null
    val errorMessage = uiState.oldPasswordError
        ?: uiState.newPasswordError
        ?: uiState.confirmPasswordError

    // высота карточки
    val cardHeight by animateDpAsState(
        targetValue = if (isExpanded) CardBaseHeight + CardExtraHeight else CardBaseHeight,
        animationSpec = tween(400, easing = FastOutSlowInEasing)
    )

    // анимация текста
    val textAlpha by animateFloatAsState(targetValue = if (showText) 1f else 0f,
        animationSpec = tween(260, easing = FastOutSlowInEasing))
    val textOffset by animateDpAsState(targetValue = if (showText) 0.dp else 10.dp,
        animationSpec = tween(280, easing = FastOutSlowInEasing))

    // эффект для "пульса" и тайминга
    LaunchedEffect(pulse) {
        if (pulse == 0) return@LaunchedEffect
        isExpanded = true
        kotlinx.coroutines.delay(140)
        showText = true
        kotlinx.coroutines.delay(2100)
        showText = false
        kotlinx.coroutines.delay(280) // ждем окончания анимации текста
        isExpanded = false
    }

    GroupeakBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CommonHeader(
                title = "Смена пароля",
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

                    LabeledPasswordField(
                        "Старый пароль",
                        uiState.oldPassword,
                        onOldPasswordChange,
                        uiState.isOldPasswordVisible,
                        onToggleOldPasswordVisibility,
                        "Введите старый пароль",
                        uiState.oldPasswordError != null
                    )

                    Spacer(Modifier.height(11.dp))

                    LabeledPasswordField(
                        "Новый пароль",
                        uiState.newPassword,
                        onNewPasswordChange,
                        uiState.isNewPasswordVisible,
                        onToggleNewPasswordVisibility,
                        "Введите новый пароль",
                        uiState.newPasswordError != null
                    )

                    Spacer(Modifier.height(11.dp))

                    LabeledPasswordField(
                        "Подтверждение нового пароля",
                        uiState.confirmPassword,
                        onConfirmPasswordChange,
                        uiState.isConfirmPasswordVisible,
                        onToggleConfirmPasswordVisibility,
                        "Подтвердите новый пароль",
                        uiState.confirmPasswordError != null
                    )

                    Spacer(Modifier.height(19.dp))

                    Button(
                        onClick = {
                            onChangeClick()
                            pulse += 1
                        },
                        modifier = Modifier
                            .width(ButtonWidth)
                            .height(ButtonHeight),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary300,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "Изменить",
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // блок текста успешного изменения / ошибки
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
                                uiState.isSuccess -> "Пароль изменён"
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

@Composable
private fun LabeledPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    placeholder: String,
    isError: Boolean
) {
    Column {
        Text(label, fontSize = 14.sp, fontFamily = OnestFontFamily, color = BluePrimary300, modifier = Modifier.padding(start = 16.dp))
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = TextStyle(fontSize = 14.sp, fontFamily = OnestFontFamily, color = GroupeakDarkBlue),
            modifier = Modifier
                .width(FieldWidth)
                .height(FieldHeight)
                .border(1.dp, if (isError) Color.Red else GroupeakLightGray, RoundedCornerShape(18.dp))
                .background(Color.White, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally),
            decorationBox = { inner ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(painter = painterResource(R.drawable.ic_lock), contentDescription = null, tint = BluePrimary300, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) Text(placeholder, fontSize = 14.sp, fontFamily = OnestFontFamily, color = Gray650)
                        inner()
                    }
                    Icon(
                        painter = painterResource(if (isPasswordVisible) R.drawable.ic_eye_on else R.drawable.ic_eye_off),
                        contentDescription = null,
                        tint = BluePrimary300,
                        modifier = Modifier.size(24.dp).clickable { onToggleVisibility() }
                    )
                }
            }
        )
    }
}

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