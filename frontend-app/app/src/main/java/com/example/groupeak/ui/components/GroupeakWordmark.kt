package com.example.groupeak.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.groupeak.ui.theme.GothamFontFamily
import com.example.groupeak.ui.theme.GroupeakGradientBlueLight
import com.example.groupeak.ui.theme.GroupeakGradientBlueDark
import com.example.groupeak.ui.theme.GroupeakGradientPurple
import com.example.groupeak.ui.theme.GroupeakGradientOrange

@Composable
fun GroupeakWordmark (
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 36.sp
) {
    val gradient = remember {
        Brush.horizontalGradient(
            colorStops = arrayOf(
                0.0f to GroupeakGradientBlueLight,
                0.33f to GroupeakGradientBlueDark,
                0.67f to GroupeakGradientPurple,
                1.0f to GroupeakGradientOrange
            )
        )
    }

    Text(
        text = "Groupeak",
        modifier = modifier,
        style = TextStyle(
            brush = gradient,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            fontFamily = GothamFontFamily
        )
    )
}