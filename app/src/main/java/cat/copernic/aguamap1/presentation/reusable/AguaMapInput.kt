package cat.copernic.aguamap1.presentation.reusable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.Negro

@Composable
fun AguaMapInput(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    isPasswordField: Boolean = false,
    color: Color = Negro
) {
    var passwordVisible by remember { mutableStateOf(false) }
    Column {
        Text(
            text = label,
            fontSize = 16.sp,
            color = color,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            isError = isError,
            placeholder = { Text(text = placeholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isPasswordField && !passwordVisible) {
                PasswordVisualTransformation() // Ocultar la contraseña
            } else {
                VisualTransformation.None
            },
            trailingIcon = {
                if (isPasswordField) {
                    PasswordTrailingIcon(
                        isVisible = passwordVisible,
                        onToggle = { passwordVisible = !passwordVisible }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPasswordField) KeyboardType.Password else KeyboardType.Email
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = color,
                unfocusedTextColor = color,
                cursorColor = color,
                errorTextColor = color,
                errorCursorColor = color,
                unfocusedBorderColor = color,
                focusedBorderColor = color,
                focusedLabelColor = color,
                unfocusedLabelColor = color,
                focusedPlaceholderColor = color,
                unfocusedPlaceholderColor = color,
                errorPlaceholderColor = color,
            )
        )
    }
}

@Composable
private fun PasswordTrailingIcon(
    isVisible: Boolean,
    onToggle: () -> Unit
) {
    val image = if (isVisible)
        painterResource(id = R.drawable.icono_on)
    else
        painterResource(id = R.drawable.icono_off)

    IconButton(onClick = onToggle) {
        Icon(
            painter = image,
            modifier = Modifier.size(28.dp),
            contentDescription = if (isVisible) stringResource(R.string.pass_off) else stringResource(
                R.string.pass_on
            ),
        )
    }
}