package cat.copernic.aguamap1.aplication.fountain.detailFountain.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.comment.Comment
import cat.copernic.aguamap1.aplication.fountain.comments.AddCommentDialog
import cat.copernic.aguamap1.ui.theme.Rojo

/**
 * Gestor central de diálogos y ventanas emergentes para la pantalla de detalle de fuente.
 * Controla la visibilidad y las acciones de confirmación para reseñas, ediciones y eliminaciones.
 *
 * @param showAddComment Estado que activa el diálogo para crear una nueva reseña.
 * @param editingComment Objeto comentario cargado para su modificación; si no es nulo, activa el diálogo de edición.
 * @param commentToDelete Comentario seleccionado para ser eliminado; activa el diálogo de confirmación de borrado.
 * @param showDeleteConfirm Estado que activa la confirmación de eliminación de la fuente completa.
 * @param onDismiss Callback unificado para cerrar cualquier diálogo activo.
 * @param onAddCommentConfirm Procesa la creación de un comentario con su nota y texto.
 * @param onEditCommentConfirm Procesa la actualización de un comentario existente.
 * @param onDeleteCommentConfirm Ejecuta la eliminación definitiva de un comentario.
 * @param onDeleteFountainConfirm Ejecuta la eliminación definitiva de la fuente (solo administradores).
 */
@Composable
fun FountainDetailDialogs(
    showAddComment: Boolean,
    editingComment: Comment?,
    commentToDelete: Comment?,
    showDeleteConfirm: Boolean,
    onDismiss: () -> Unit,
    onAddCommentConfirm: (Int, String) -> Unit,
    onEditCommentConfirm: (Int, String) -> Unit,
    onDeleteCommentConfirm: () -> Unit,
    onDeleteFountainConfirm: () -> Unit
) {
    /**
     * Diálogo para la creación de una nueva valoración y reseña.
     */
    if (showAddComment) {
        AddCommentDialog(isEditing = false, onDismiss = onDismiss, onConfirm = onAddCommentConfirm)
    }

    /**
     * Diálogo de edición de reseña. Se dispara automáticamente al recibir un objeto de comentario.
     */
    editingComment?.let { comment ->
        AddCommentDialog(
            initialRating = comment.rating,
            initialText = comment.comment,
            isEditing = true,
            onDismiss = onDismiss,
            onConfirm = onEditCommentConfirm
        )
    }

    /**
     * Alerta de confirmación crítica para la eliminación de una fuente del sistema.
     */
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.delete_fountain_title)) },
            text = { Text(stringResource(R.string.delete_fountain_confirm_text)) },
            confirmButton = {
                TextButton(onClick = onDeleteFountainConfirm) {
                    Text(
                        stringResource(R.string.delete_confirm),
                        color = Rojo,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    /**
     * Alerta de confirmación para el borrado de un comentario individual.
     */
    commentToDelete?.let {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.delete_comment_title)) },
            text = { Text(stringResource(R.string.delete_comment_text)) },
            confirmButton = {
                TextButton(onClick = onDeleteCommentConfirm) {
                    Text(
                        stringResource(R.string.delete_confirm),
                        color = Rojo,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}