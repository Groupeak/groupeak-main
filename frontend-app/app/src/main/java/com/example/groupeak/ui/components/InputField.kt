package com.example.groupeak.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.ui.theme.Gray650
import com.example.groupeak.ui.theme.Gray700
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.OnestFontFamily

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    iconResId: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    trailingIcon: (@Composable () -> Unit)? = null, // Добавляем этот параметр
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .width(328.dp)
                .height(55.dp)
                .border(
                    width = if (isError) 1.dp else 0.dp,
                    color = if (isError) Color(0xFFFF4D4F) else Color.Transparent,
                    shape = RoundedCornerShape(18.dp)
                ),
            label = label?.let { labelText ->
                {
                    Text(
                        text = labelText,
                        fontSize = 16.sp,
                        color = if (isError) Color(0xFFFF4D4F) else Gray650
                    )
                }
            },
            leadingIcon = {
                if (iconResId != null) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp),
                        tint = Gray700
                    )
                }
            },
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(18.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = BluePrimary300,
                errorCursorColor = Color(0xFFFF4D4F)
            ),
            isError = isError
        )

        // Добавляем текст ошибки если есть
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFFF4D4F),
                fontSize = 12.sp,
                fontFamily = OnestFontFamily,
                modifier = Modifier
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}