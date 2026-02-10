package com.example.groupeak.ui.screens.change_password

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
import com.example.groupeak.ui.theme.OnestFontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

// === LabeledPasswordField (как LabeledField, но для паролей) ===
@Composable
private fun LabeledPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null // ← это будет использоваться как заголовок при ошибке
) {
    Column(modifier = modifier) {
        // Заголовок: либо обычный, либо сообщение об ошибке
        Text(
            text = if (isError && errorMessage != null) errorMessage else label,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = if (isError) Color.Red else BluePrimary300, // ← красный при ошибке
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

                    // Иконка глаза
                    Icon(
                        painter = painterResource(
                            id = if (isPasswordVisible) R.drawable.ic_eye_on else R.drawable.ic_eye_off
                        ),
                        contentDescription = if (isPasswordVisible) "Скрыть пароль" else "Показать пароль",
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

// ==========================================

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
    GroupeakBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CommonHeader(
                title = "Смена пароля",
                hasNotifications = false,
                onBackClick = onBackClick,
                onNotificationsClick = onNotificationsClick
            )

            Spacer(modifier = Modifier.height(150.dp))

            // === АНИМАЦИЯ УСПЕХА ===
            val WhiteCardHeight = 378.dp
            val SentExtraHeight = 19.dp

            var isExpanded by remember { mutableStateOf(false) }
            var showMessageText by remember { mutableStateOf(false) }
            var messagePulse by remember { mutableIntStateOf(0) }

            val animatedCardHeight by animateDpAsState(
                targetValue = if (isExpanded) WhiteCardHeight + SentExtraHeight else WhiteCardHeight,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                label = "cardHeight"
            )

            val sentAlpha by animateFloatAsState(
                targetValue = if (showMessageText) 1f else 0f,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                label = "sentAlpha"
            )

            val sentSlide by animateDpAsState(
                targetValue = if (showMessageText) 0.dp else 10.dp,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                label = "sentSlide"
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

            Column(
                modifier = Modifier
                    .height(animatedCardHeight)
                    .width(348.dp)
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(19.dp))

                LabeledPasswordField(
                    label = "Старый пароль",
                    value = uiState.oldPassword,
                    onValueChange = onOldPasswordChange,
                    isPasswordVisible = uiState.isOldPasswordVisible,
                    onToggleVisibility = onToggleOldPasswordVisibility,
                    placeholder = "Введите старый пароль",
                    isError = uiState.oldPasswordError != null,
                    errorMessage = uiState.oldPasswordError
                )

                Spacer(modifier = Modifier.height(11.dp))

                LabeledPasswordField(
                    label = "Новый пароль",
                    value = uiState.newPassword,
                    onValueChange = onNewPasswordChange,
                    isPasswordVisible = uiState.isNewPasswordVisible,
                    onToggleVisibility = onToggleNewPasswordVisibility,
                    placeholder = "Введите новый пароль",
                    isError = uiState.newPasswordError != null,
                    errorMessage = uiState.newPasswordError
                )

                Spacer(modifier = Modifier.height(11.dp))

                LabeledPasswordField(
                    label = "Подтверждение нового пароля",
                    value = uiState.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    isPasswordVisible = uiState.isConfirmPasswordVisible,
                    onToggleVisibility = onToggleConfirmPasswordVisibility,
                    placeholder = "Подтвердите новый пароль",
                    isError = uiState.confirmPasswordError != null,
                    errorMessage = uiState.confirmPasswordError
                )

                PrimaryButton(
                    modifier = Modifier
                        .padding(top = 19.dp)
                        .width(154.dp)
                        .height(32.dp)
                        .align(Alignment.CenterHorizontally),
                    text = "Изменить",
                    onClick = { messagePulse += 1 }, // ← триггер анимации
                    fontSize = 14.sp
                )

                if (isExpanded) {
                    Text(
                        text = "Пароль изменён",
                        fontSize = 14.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF26BC30),
                        modifier = Modifier
                            .padding(top = 14.dp)
                            .graphicsLayer { alpha = sentAlpha }
                            .offset(y = sentSlide)
                    )
                }
            }
        }
    }
}