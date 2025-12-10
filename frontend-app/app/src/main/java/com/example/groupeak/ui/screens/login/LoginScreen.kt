package com.example.groupeak.ui.screens.login

import androidx.compose.foundation.Image
import com.example.groupeak.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.components.GroupeakWordmark
import com.example.groupeak.ui.components.PrimaryButton
import com.example.groupeak.ui.components.InputField
import com.example.groupeak.ui.components.PasswordInputField
import com.example.groupeak.ui.theme.OnestFontFamily
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.GroupeakBlue

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onLogin: (String, String) -> Unit = { _, _ -> }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    GroupeakBackground(

    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Стрелка назад
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 65.dp, bottom = 50.dp, start = 8.dp)
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

            //Spacer(modifier = Modifier.height(164.dp))

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
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "",
                iconResId = R.drawable.ic_email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            //Spacer(modifier = Modifier.height(18.dp))

            // Пароль
            PasswordInputField(
                modifier = Modifier.padding(top = 18.dp),
                value = password,
                onValueChange = { password = it },
                label = "Пароль",
                placeholder = "",
                lockIconResId = R.drawable.ic_lock // или null, если пока нет иконки
            )

            //Spacer(modifier = Modifier.height(32.dp))

            // Кнопка "Войти"
            PrimaryButton(
                modifier = Modifier.padding(top = 30.dp),
                text = "Войти",
                onClick = { onLogin(email, password) }
            )

            //Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Забыли пароль?",
                fontSize = 20.sp,
                color = BluePrimary300,
                fontFamily = OnestFontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    //.fillMaxWidth()
                    .padding(top = 15.dp)
                    .clickable { onForgotPassword() }
            )
        }
    }
}
