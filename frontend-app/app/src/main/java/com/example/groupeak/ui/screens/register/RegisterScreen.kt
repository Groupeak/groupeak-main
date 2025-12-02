package com.example.groupeak.ui.screens.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.components.GroupeakWordmark
import com.example.groupeak.ui.components.InputField
import com.example.groupeak.ui.components.PasswordInputField
import com.example.groupeak.ui.components.PrimaryButton
import com.example.groupeak.ui.theme.OnestFontFamily
import com.example.groupeak.ui.theme.GroupeakBlue
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.Gray600
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onLogin: (String, String) -> Unit = { _, _ -> }
) {
    var FIO by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var secondPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    GroupeakBackground(

    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 70.dp, bottom = 50.dp, start = 20.dp, end = 20.dp),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    painter = painterResource(id = com.example.groupeak.R.drawable.ic_back_arrow),
                    contentDescription = "Назад",
                    tint = GroupeakBlue,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(24.dp)
                        .clickable { onBack() }
                )


                GroupeakWordmark(
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(164.dp))

            // Заголовок "Регистрация"
            Text(
                text = "Регистрация",
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = OnestFontFamily,
                color = BluePrimary300,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // FIO
            InputField(
                value = FIO,
                onValueChange = { FIO = it },
                placeholder = "ФИО",
                iconResId = com.example.groupeak.R.drawable.ic_fio,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // email
            InputField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                iconResId = com.example.groupeak.R.drawable.ic_email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))
            // Пароль
            PasswordInputField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Пароль",
                lockIconResId = R.drawable.ic_lock // или null, если пока нет иконки
            )

            Spacer(modifier = Modifier.height(16.dp))
            // Потверждение Пароля
            PasswordInputField(
                value = secondPassword,
                onValueChange = { secondPassword = it },
                placeholder = "Потверждение Пароля",
                lockIconResId = R.drawable.ic_lock // или null, если пока нет иконки
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка "Зарегистрироваться"
            PrimaryButton(
                text = "Зарегистрироваться",
                onClick = { onLogin(email, password) },
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
