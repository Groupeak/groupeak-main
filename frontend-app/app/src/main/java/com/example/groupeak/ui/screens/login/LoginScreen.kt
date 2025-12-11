package com.example.groupeak.ui.screens.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.components.GroupeakWordmark
import com.example.groupeak.ui.components.InputField
import com.example.groupeak.ui.components.PasswordInputField
import com.example.groupeak.ui.components.PrimaryButton
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.OnestFontFamily

@Composable
fun LoginScreen(
    state: LoginUiState,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    GroupeakBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Стрелка назад + логотип
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 65.dp, bottom = 50.dp, start = 8.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Назад",
                    tint = BluePrimary300,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.CenterStart)
                        .clickable { onBack() }
                )

                GroupeakWordmark(
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 32.sp
                )
            }

            // Заголовок "Авторизация"
            Text(
                text = "Авторизация",
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = OnestFontFamily,
                color = BluePrimary300,
                modifier = Modifier.padding(top = 136.dp)
            )

            // Email
            InputField(
                modifier = Modifier.padding(top = 30.dp),
                value = state.email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "",
                iconResId = R.drawable.ic_email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Пароль
            PasswordInputField(
                modifier = Modifier.padding(top = 18.dp),
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Пароль",
                placeholder = "",
                lockIconResId = R.drawable.ic_lock
            )

            // Кнопка "Войти"
            PrimaryButton(
                modifier = Modifier.padding(top = 30.dp),
                text = if (state.isLoading) "Загрузка..." else "Войти",
                onClick = {
                    if (!state.isLoading) {
                        onLoginClick()
                    }
                }
            )

            // Ошибка (если есть)
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    fontSize = 14.sp,
                    color = androidx.compose.ui.graphics.Color(0xFFFF4D4F),
                    fontFamily = OnestFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                )
            }

            // "Забыли пароль?"
            Text(
                text = "Забыли пароль?",
                fontSize = 20.sp,
                color = BluePrimary300,
                fontFamily = OnestFontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 15.dp)
                    .clickable { onForgotPassword() }
            )
        }
    }
}
