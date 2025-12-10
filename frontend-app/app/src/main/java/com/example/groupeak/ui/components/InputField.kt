package com.example.groupeak.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.ui.theme.Gray650
import com.example.groupeak.ui.theme.Gray700
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.Gray600
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
    trailingIcon: (@Composable (() -> Unit))? = null,
    isError: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value

    val errorColor = Color(0xFFFF4D4F)

    val borderColor = when {
        isError -> errorColor
        isFocused -> BluePrimary300
        else -> Color.White
    }
    val borderWidth = if (isError || isFocused) 1.dp else 0.dp

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .width(328.dp)
            .height(55.dp)
            .onFocusChanged { }
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            ),
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = OnestFontFamily,
                fontSize = 16.sp,
                color = when {
                    isError -> errorColor
                    isFocused -> Color.White
                    else -> Gray650
                }
            )
        },
        label = label?.let { labelText ->
            {
                Text(
                    text = labelText,
                    fontSize = 16.sp,
                    color = when {
                        isError -> errorColor
                        isFocused -> Gray600
                        else -> Gray650
                    }
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
            // фон
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            errorContainerColor = Color.White,

            // индикаторы (подчёркивание)
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,

            // курсор
            cursorColor = BluePrimary300,
            errorCursorColor = BluePrimary300
        ),
        interactionSource = interactionSource,
        isError = isError
    )
}
