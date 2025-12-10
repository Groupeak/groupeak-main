package com.example.groupeak.ui.screens.start

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.components.GroupeakWordmark
import com.example.groupeak.ui.components.PrimaryButton
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.OnestFontFamily
import com.example.groupeak.ui.theme.GroupeakGray

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
            Spacer(modifier = Modifier.height(165.dp))

            // Логотип + надпись Groupeak
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_groupeak),
                    contentDescription = "Логотип Groupeak",
                    modifier = Modifier.size(230.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                GroupeakWordmark(
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Слоган
            Text(
                text = "Работа в команде и\nотслеживание задач",
                fontSize = 20.sp,
                fontFamily = OnestFontFamily,
                color = GroupeakGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(155.dp))

            // Кнопка "Войти"
            PrimaryButton(
                text = "Войти",
                onClick = onLoginClick
            )

            Spacer(modifier = Modifier.height(15.dp))

            // Ссылка "Нет аккаунта?"
            Text(
                text = "Нет аккаунта?",
                fontSize = 20.sp,
                fontFamily = OnestFontFamily,
                color = BluePrimary300,
                modifier = Modifier
                    .clickable { onRegisterClick() }
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}
