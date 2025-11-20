package com.example.groupeak.ui.screens.start

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StartScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Groupeak")

            Spacer(Modifier.height(24.dp))

            Button(onClick = onLoginClick) {
                Text("Войти")
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = onRegisterClick) {
                Text("Регистрация")
            }
        }
    }
}
