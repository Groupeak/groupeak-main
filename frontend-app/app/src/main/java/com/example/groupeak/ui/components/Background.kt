package com.example.groupeak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.example.groupeak.ui.theme.GroupeakGradientBlueLight
import com.example.groupeak.ui.theme.GroupeakGradientBlueDark
import com.example.groupeak.ui.theme.GroupeakGradientOrange
import com.example.groupeak.ui.theme.GroupeakGradientPurple
import com.example.groupeak.ui.theme.White

private val groupeakBackgroundBrush = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to GroupeakGradientBlueLight.copy(alpha = 0.07f),
        0.33f to GroupeakGradientBlueDark.copy(alpha = 0.07f),
        0.67f to GroupeakGradientPurple.copy(alpha = 0.07f),
        1.0f to GroupeakGradientOrange.copy(alpha = 0.07f)
    )
)

@Composable
fun GroupeakBackground(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(groupeakBackgroundBrush)
    )

    content()
}
