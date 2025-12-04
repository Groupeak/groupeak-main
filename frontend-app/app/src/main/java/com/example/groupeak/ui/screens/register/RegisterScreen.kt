package com.example.groupeak.ui.screens.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.example.groupeak.ui.theme.GroupeakBlue
import com.example.groupeak.ui.theme.OnestFontFamily

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onLogin: (String, String) -> Unit = { _, _ -> }
) {
    var fio by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var secondPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // показывать ли ошибку "пароли не совпадают"
    var showPasswordError by remember { mutableStateOf(false) }

    val passwordsDoNotMatch = showPasswordError

    GroupeakBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // шапка с back и wordmark
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 70.dp, bottom = 50.dp, start = 20.dp, end = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Назад",
                    tint = GroupeakBlue,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(24.dp)
                        .clickable { onBack() }
                )

                GroupeakWordmark(
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(164.dp))

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
                value = fio,
                onValueChange = { fio = it },
                label = "ФИО",
                placeholder = "ФИО",
                iconResId = R.drawable.ic_fio
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            InputField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "Email",
                iconResId = R.drawable.ic_email
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Пароль
            PasswordInputField(
                value = password,
                onValueChange = {
                    password = it
                    // как только юзер меняет пароль — прячем старую ошибку
                    if (showPasswordError) showPasswordError = false
                },
                label = "Пароль",
                placeholder = "Пароль",
                lockIconResId = R.drawable.ic_lock
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Подтверждение пароля
            PasswordInputField(
                value = secondPassword,
                label = "Подтвердите пароль",
                onValueChange = {
                    secondPassword = it
                    if (showPasswordError) showPasswordError = false
                },
                placeholder = "Подтверждение пароля",
                lockIconResId = R.drawable.ic_lock,
                isError = passwordsDoNotMatch
            )

            if (passwordsDoNotMatch) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Пароли не совпадают",
                    color = Color(0xFFFF4D4F),
                    fontSize = 14.sp,
                    fontFamily = OnestFontFamily,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка "Зарегистрироваться"
            PrimaryButton(
                text = "Зарегистрироваться",
                onClick = {
                    val hasError =
                        password.isNotEmpty() &&
                                secondPassword.isNotEmpty() &&
                                password != secondPassword

                    showPasswordError = hasError

                    if (!hasError) {
                        onLogin(email, password)
                    }
                },
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Есть аккаунт?",
                fontSize = 20.sp,
                color = BluePrimary300,
                fontFamily = OnestFontFamily,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onForgotPassword() }
            )
        }
    }
}
