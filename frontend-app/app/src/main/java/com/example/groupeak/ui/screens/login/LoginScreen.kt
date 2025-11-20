package com.example.groupeak.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.ui.components.PrimaryButton

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onLogin: (String, String) -> Unit = { _, _ -> }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val primaryBlue = Color(0xFF00A9FF)
    val gradientTop = Color(0xFFF6FBFF)
    val gradientBottom = Color(0xFFFDF3F7)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(gradientTop, gradientBottom)
                )
            )
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // стрелка назад
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "←",
                    fontSize = 24.sp,
                    color = primaryBlue,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { onBack() }
                )
            }

            // лого + название
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 40.dp)
            ) {
                // сюда потом вставишь Image с логотипом
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0x3300A9FF), shape = MaterialTheme.shapes.large)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Groupeak",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryBlue
                )
            }

            // заголовок "Вход"
            Text(
                text = "Вход",
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                color = primaryBlue,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email
            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Пароль
            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Пароль") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    Text(
                        text = if (passwordVisible) "Скрыть" else "Показать",
                        color = primaryBlue,
                        modifier = Modifier.clickable {
                            passwordVisible = !passwordVisible
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // НАША КНОПКА 250x58 с #00A9FF
            PrimaryButton(
                text = "Войти",
                onClick = { onLogin(email, password) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Забыли пароль?",
                fontSize = 15.sp,
                color = primaryBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onForgotPassword() }
            )
        }
    }
}
