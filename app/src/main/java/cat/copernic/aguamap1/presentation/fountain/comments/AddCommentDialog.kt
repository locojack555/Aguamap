package cat.copernic.aguamap1.presentation.fountain.comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.GrisClaro
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.NegroSuave

@Composable
fun AddCommentDialog(
    initialRating: Int = 5,
    initialText: String = "",
    isEditing: Boolean = false, // <-- NUEVO PARÁMETRO
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(initialRating) }
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (!isEditing) stringResource(R.string.dialog_add_comment_title)
                else stringResource(R.string.edit_comment),
                fontWeight = FontWeight.Bold,
                color = Negro
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.dialog_add_comment_prompt),
                    fontSize = 14.sp,
                    color = NegroSuave
                )

                // Selector de estrellas
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(5) { index ->
                        val currentStarValue = index + 1
                        IconButton(
                            onClick = { rating = currentStarValue },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (currentStarValue <= rating) Naranja else GrisClaro,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // Campo de texto
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.dialog_add_comment_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text(stringResource(R.string.dialog_add_comment_publish)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Negro,
                        unfocusedTextColor = Negro,
                        focusedLabelColor = Negro,
                        unfocusedLabelColor = Negro,
                        focusedBorderColor = Negro,
                        unfocusedBorderColor = Negro,
                        cursorColor = Negro
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(rating, text) },
                enabled = rating > 0,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue10)
            ) {
                Text(
                    text = if (!isEditing) stringResource(R.string.dialog_add_comment_publish)
                    else stringResource(R.string.confirm_button)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_add_comment_cancel), color = NegroSuave)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Blanco
    )
}