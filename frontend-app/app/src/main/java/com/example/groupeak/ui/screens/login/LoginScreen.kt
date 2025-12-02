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
                    .padding(top = 70.dp, bottom = 50.dp, start = 20.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Назад",
                    tint = GroupeakBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterStart)
                        .clickable { onBack() }
                )
            }

            // Лого + название
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 40.dp)
            ) {
                // Логотип
                Image(
                    painter = painterResource(id = R.drawable.logo_groupeak),
                    contentDescription = "Логотип Groupeak",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                GroupeakWordmark()
            }

            // Заголовок "Авторизация"
            Text(
                text = "Авторизация",
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = OnestFontFamily,
                color = BluePrimary300,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email
            InputField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                iconResId = R.drawable.ic_email,
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

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка "Войти"
            PrimaryButton(
                text = "Войти",
                onClick = { onLogin(email, password) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Забыли пароль?",
                fontSize = 20.sp,
                color = BluePrimary300,
                fontFamily = OnestFontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onForgotPassword() }
            )
        }
    }
}
