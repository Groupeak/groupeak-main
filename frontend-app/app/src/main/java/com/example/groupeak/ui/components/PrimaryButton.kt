package com.example.groupeak.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.ui.theme.OnestFontFamily
import com.example.groupeak.ui.theme.BluePrimary300
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.layout.PaddingValues
@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    fontSize: TextUnit = 24.sp
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(250.dp)   // 250 x 58
            .height(58.dp),
        shape = RoundedCornerShape(18.dp),      // скругление 18
        colors = ButtonDefaults.buttonColors(
            containerColor = BluePrimary300,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            fontFamily = OnestFontFamily
        )
    }
}
