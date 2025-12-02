package com.example.groupeak.ui.screens.start

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.components.GroupeakWordmark
import com.example.groupeak.ui.components.PrimaryButton
import com.example.groupeak.ui.theme.OnestFontFamily
import com.example.groupeak.ui.theme.GroupeakBlue

@Composable
fun StartScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    GroupeakBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Логотип + надпись Groupeak
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_groupeak),
                    contentDescription = "Логотип Groupeak",
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                GroupeakWordmark(
                    fontSize = 36.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Слоган
            Text(
                text = "Создавайте и выполняйте\nпроекты вместе с нами!",
                fontSize = 18.sp,
                fontFamily = OnestFontFamily,
                color = Color(0xFF4F5152),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Кнопка "Войти"
            PrimaryButton(
                text = "Войти",
                onClick = onLoginClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ссылка "Нет аккаунта?"
            Text(
                text = "Нет аккаунта?",
                fontSize = 18.sp,
                fontFamily = OnestFontFamily,
                color = GroupeakBlue,
                modifier = Modifier
                    .clickable { onRegisterClick() }
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
