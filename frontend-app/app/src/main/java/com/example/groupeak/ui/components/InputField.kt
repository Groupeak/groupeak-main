package com.example.groupeak.ui.components

import androidx.compose.foundation.border
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.ui.theme.GroupeakBlue
import com.example.groupeak.ui.theme.Gray650
import com.example.groupeak.ui.theme.Gray700
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.material3.OutlinedTextField

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
    trailingIcon: (@Composable (() -> Unit))? = null
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .width(328.dp)
            .height(55.dp)
            .onFocusChanged {  }
            .then(
                if (isFocused) {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.Cyan,
                        shape = RoundedCornerShape(18.dp)
                    )
                } else {
                    Modifier.border(
                        width = 0.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(18.dp)
                    )
                }
            ),
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 16.sp,
                color = if (isFocused) Color.White else Gray650
            )
        },
        label = label?.let { labelText ->
            {
                Text(
                    text = labelText,
                    fontSize = 16.sp,

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
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = GroupeakBlue,
            focusedLabelColor = Gray650,
        ),
        interactionSource = interactionSource
    )
}