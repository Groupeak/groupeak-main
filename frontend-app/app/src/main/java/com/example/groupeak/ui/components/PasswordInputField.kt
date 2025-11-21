package com.example.groupeak.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.groupeak.R
import com.example.groupeak.ui.theme.GroupeakBlue

@Composable
fun PasswordInputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Пароль",
    lockIconResId: Int? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    InputField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        iconResId = lockIconResId,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            Icon(
                painter = painterResource(
                    id = if (passwordVisible) R.drawable.ic_eye_on
                    else R.drawable.ic_eye_off
                ),
                contentDescription = if (passwordVisible)
                    "Скрыть пароль" else "Показать пароль",
                modifier = Modifier
                    .clickable { passwordVisible = !passwordVisible }
                    .then(Modifier)
                ,
                tint = GroupeakBlue
            )
        }
    )
}
