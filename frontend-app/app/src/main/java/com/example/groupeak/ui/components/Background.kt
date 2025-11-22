package com.example.groupeak.ui.components
// тест гит хаба
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
import com.example.groupeak.ui.theme.GroupeakGradientWhite85

private val groupeakBackgroundBrush = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to GroupeakGradientBlueLight,
        0.33f to GroupeakGradientBlueDark,
        0.67f to GroupeakGradientPurple,
        1.0f to GroupeakGradientOrange
    )
)

@Composable
fun GroupeakBackground(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(groupeakBackgroundBrush)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GroupeakGradientWhite85)
    )
    content()

}
