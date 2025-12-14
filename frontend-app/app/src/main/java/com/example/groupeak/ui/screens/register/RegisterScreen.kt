package com.example.groupeak.ui.screens.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
fun RegisterScreen(
    state: RegisterUiState,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onFioChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSecondPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
) {
    GroupeakBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // шапка с back и wordmark
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 65.dp, bottom = 50.dp, start = 8.dp, end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Назад",
                    tint = BluePrimary300,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(28.dp)
                        .clickable { onBack() }
                )

                GroupeakWordmark(
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(136.dp))

            // заголовок
            Text(
                text = "Регистрация",
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = OnestFontFamily,
                color = BluePrimary300,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // ФИО
            InputField(
                value = state.fio,
                onValueChange = onFioChange,
                label = "ФИО",
                placeholder = "ФИО",
                iconResId = R.drawable.ic_fio,
                isError = state.fioError != null,
                errorMessage = state.fioError
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Email
            InputField(
                value = state.email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "Email",
                iconResId = R.drawable.ic_email,
                isError = state.emailError != null,
                errorMessage = state.emailError
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Пароль
            PasswordInputField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Пароль",
                placeholder = "Пароль",
                lockIconResId = R.drawable.ic_lock,
                isError = state.passwordError != null || state.passwordsDoNotMatch,
                errorMessage = state.passwordError
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Подтверждение пароля
            PasswordInputField(
                value = state.secondPassword,
                label = "Подтвердите пароль",
                onValueChange = onSecondPasswordChange,
                placeholder = "Подтверждение пароля",
                lockIconResId = R.drawable.ic_lock,
                isError = state.passwordsDoNotMatch,
                errorMessage = if (state.passwordsDoNotMatch) "Пароли не совпадают" else null
            )

//            if (state.passwordsDoNotMatch) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "Пароли не совпадают",
//                    color = Color(0xFFFF4D4F),
//                    fontSize = 12.sp,
//                    fontFamily = OnestFontFamily,
//
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(start = 16.dp, top = 4.dp)
//                )
//            }

            Spacer(modifier = Modifier.height(30.dp))

            // Кнопка "Зарегистрироваться"
            PrimaryButton(
                text = if (state.isLoading) "Загрузка..." else "Зарегистрироваться",
                onClick = {
                    if (!state.isLoading) {
                        onRegisterClick()
                    }
                },
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Общая ошибка (если не связана с полем)
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = Color(0xFFFF4D4F),
                    fontSize = 14.sp,
                    fontFamily = OnestFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "Есть аккаунт?",
                fontSize = 20.sp,
                color = BluePrimary300,
                fontFamily = OnestFontFamily,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .clickable { onForgotPassword() }
            )
        }
    }
}