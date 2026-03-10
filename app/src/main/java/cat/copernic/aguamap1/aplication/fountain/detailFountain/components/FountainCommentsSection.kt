package cat.copernic.aguamap1.aplication.fountain.detailFountain.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.comment.Comment
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.NegroMuySuave

/**
 * Contenedor principal de la sección de feedback de usuarios para una fuente.
 * Orquesta la visualización de la lista de comentarios y proporciona el acceso
 * a la creación de nuevas reseñas.
 *
 * @param comments Lista de objetos [Comment] a renderizar.
 * @param currentUserId Identificador del usuario autenticado para determinar autoría.
 * @param isAdmin Flag de privilegios de administrador para opciones de moderación.
 * @param onAddClick Callback para abrir el diálogo de nuevo comentario.
 * @param onEditComment Callback para modificar un comentario existente.
 * @param onDeleteComment Callback para eliminar un comentario.
 * @param onReportComment Callback para notificar un comentario inapropiado.
 */
@Composable
fun FountainCommentsSection(
    comments: List<Comment>,
    currentUserId: String?,
    isAdmin: Boolean,
    onAddClick: () -> Unit,
    onEditComment: (Comment) -> Unit,
    onDeleteComment: (Comment) -> Unit,
    onReportComment: (Comment) -> Unit,
    onCensorComment: (Comment) -> Unit
) {
    Column {
        /**
         * Encabezado de la sección con contador dinámico y acción de añadir.
         */
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.ratings_title, comments.size),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f),
                color = Negro
            )
            TextButton(onClick = onAddClick) {
                Text(
                    text = stringResource(R.string.add_comment),
                    color = Blue10,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        /**
         * Renderizado condicional: mensaje de lista vacía o iteración de comentarios.
         */
        if (comments.isEmpty()) {
            Text(
                text = stringResource(R.string.first_comment_prompt),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                color = NegroMuySuave,
                fontStyle = FontStyle.Italic
            )
        } else {
            comments.forEach { comment ->
                CommentItem(
                    commentObj = comment,
                    isMyComment = comment.userId == currentUserId,
                    isAdmin = isAdmin,
                    onCensor = { onCensorComment(comment) },
                    onDelete = { onDeleteComment(comment) },
                    onEdit = { onEditComment(comment) },
                    onReport = { onReportComment(comment) }
                )
            }
        }
    }
}