package com.example.groupeak.ui.screens.change_email

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.OnestFontFamily
import androidx.compose.foundation.clickable

@Composable
fun ChangeEmailScreen(
    onBack: () -> Unit
) {
    GroupeakBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 55.dp, start = 8.dp, end = 19.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        .align(Alignment.CenterStart)
                        .size(28.dp)
                        .clickable { onBack() }
                )

                Text(
                    text = "Изменить почту",
                    fontSize = 32.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = BluePrimary300,
                    modifier = Modifier
                        .padding(top = 32.dp)
                )
            }

            Text(
                text = "Заглушка: изменение email",
                fontSize = 16.sp,
                fontFamily = OnestFontFamily,
                color = BluePrimary300,
                modifier = Modifier.padding(top = 40.dp)
            )
        }
    }
}