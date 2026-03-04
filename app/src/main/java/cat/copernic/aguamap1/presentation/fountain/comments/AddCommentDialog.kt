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

/**
 * Diálogo modal para la creación y edición de comentarios y valoraciones.
 * Implementa un selector interactivo de estrellas y un campo de texto con validación.
 * * @param initialRating Valoración por defecto (estrellas).
 * @param initialText Texto inicial del comentario.
 * @param isEditing Define si el diálogo está en modo edición o creación.
 * @param onDismiss Callback para cerrar el diálogo sin guardar.
 * @param onConfirm Callback que devuelve la valoración y el texto final.
 */
@Composable
fun AddCommentDialog(
    initialRating: Int = 5,
    initialText: String = "",
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(initialRating) }
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            /**
             * Título dinámico basado en el modo del diálogo.
             */
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

                /**
                 * Selector de valoración por estrellas.
                 */
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
                                contentDescription = stringResource(
                                    R.string.star_rating_description,
                                    currentStarValue
                                ),
                                tint = if (currentStarValue <= rating) Naranja else GrisClaro,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                /**
                 * Campo de entrada de texto para el cuerpo del comentario.
                 */
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.dialog_add_comment_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(stringResource(R.string.dialog_add_comment_placeholder))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Negro,
                        unfocusedTextColor = Negro,
                        focusedBorderColor = Negro,
                        unfocusedBorderColor = Negro,
                        cursorColor = Blue10
                    )
                )
            }
        },
        confirmButton = {
            /**
             * Botón de confirmación con lógica de habilitación proactiva.
             */
            Button(
                onClick = { onConfirm(rating, text) },
                enabled = rating > 0 && text.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue10,
                    disabledContainerColor = GrisClaro
                )
            ) {
                Text(
                    text = if (!isEditing) stringResource(R.string.dialog_add_comment_publish)
                    else stringResource(R.string.confirm_button),
                    color = Blanco
                )
            }
        },
        dismissButton = {
            /**
             * Botón de cancelación del diálogo.
             */
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.dialog_add_comment_cancel),
                    color = NegroSuave
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Blanco
    )
}