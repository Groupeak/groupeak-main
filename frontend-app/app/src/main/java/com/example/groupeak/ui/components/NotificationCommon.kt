package com.example.groupeak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.OnestFontFamily

private val Blue = Color(0xFF3988FE)
private val White = Color(0xFFFFFFFF)
private val Gray = Color(0xFFB0B3C7)
private val TextDark = Color(0xFF333B46)

@Composable
fun NotificationCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 18.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun InfoField(
    label: String,
    value: String,
    icon: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Текст 1: Label
        Text(
            text = label,
            color = Blue,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,      // <-- Добавлено
            fontWeight = FontWeight.Normal,     // <-- Добавлено
            modifier = Modifier.padding(start = 11.dp)
        )

        Row(
            modifier = Modifier
                .width(340.dp)
                .border(1.dp, Gray, RoundedCornerShape(16.dp))
                .background(White, RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 12.dp)
                .height(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Blue
            )
            Spacer(modifier = Modifier.width(6.dp))

            // Текст 2: Value
            Text(
                text = value,
                color = TextDark,
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,      // <-- Добавлено
                fontWeight = FontWeight.Normal,     // <-- Добавлено
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DescriptionBlock(
    title: String,
    text: String
) {
    Column {
        // Текст 3: Title
        Text(
            text = title,
            color = Blue,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,      // <-- Добавлено
            modifier = Modifier.padding(start = 11.dp),
            letterSpacing = 0.em
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(202.dp)
                .border(1.dp, Gray, RoundedCornerShape(20.dp))
                .background(White, RoundedCornerShape(20.dp))
        ) {
            // Текст 4: Description text
            Text(
                text = text,
                color = TextDark,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                fontFamily = OnestFontFamily,      // <-- Добавлено
                modifier = Modifier.padding(start = 11.dp, top = 6.dp, end = 11.dp),
                letterSpacing = 0.em
            )
        }
    }
}