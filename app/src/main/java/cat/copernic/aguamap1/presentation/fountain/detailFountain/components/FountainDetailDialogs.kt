package cat.copernic.aguamap1.presentation.fountain.detailFountain.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.presentation.fountain.comments.AddCommentDialog
import cat.copernic.aguamap1.ui.theme.Rojo

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
    if (showAddComment) {
        AddCommentDialog(isEditing = false, onDismiss = onDismiss, onConfirm = onAddCommentConfirm)
    }

    editingComment?.let { comment ->
        AddCommentDialog(initialRating = comment.rating, initialText = comment.comment, isEditing = true, onDismiss = onDismiss, onConfirm = onEditCommentConfirm)
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.delete_fountain_title)) },
            text = { Text(stringResource(R.string.delete_fountain_confirm_text)) },
            confirmButton = { TextButton(onClick = onDeleteFountainConfirm) { Text(stringResource(R.string.delete_confirm), color = Rojo, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
        )
    }

    commentToDelete?.let {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.delete_comment_title)) },
            text = { Text(stringResource(R.string.delete_comment_text)) },
            confirmButton = { TextButton(onClick = onDeleteCommentConfirm) { Text(stringResource(R.string.delete_confirm), color = Rojo, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
        )
    }
}