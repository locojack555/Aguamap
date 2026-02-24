package cat.copernic.aguamap1.presentation.fountain.detailFountain.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.ui.theme.GrisClaro
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.NegroMinimal
import cat.copernic.aguamap1.ui.theme.NegroMuySuave
import cat.copernic.aguamap1.ui.theme.NegroSuave
import cat.copernic.aguamap1.ui.theme.Rojo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommentItem(
    commentObj: Comment,
    isMyComment: Boolean,
    isAdmin: Boolean,
    onCensor: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onReport: () -> Unit // Este callback ahora activará el flujo de reporte al admin
) {
    // Formateador de fecha localizado
    val dateStr = remember(commentObj.timestamp) {
        SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(commentObj.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Información del autor y rating
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = commentObj.userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Negro
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    repeat(5) { i ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (i < commentObj.rating) Naranja else GrisClaro
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(text = dateStr, fontSize = 11.sp, color = NegroMuySuave)
                }
            }

            // Acciones (Editar, Borrar, Reportar, Censurar)
            Row {
                if (isMyComment) {
                    if (!commentObj.isCensored) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = NegroSuave,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = NegroSuave,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Botón de Reporte (Para usuarios que no son dueños del comentario ni admins)
                if (!isMyComment && !isAdmin) {
                    IconButton(onClick = onReport, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = stringResource(R.string.report_button),
                            // Si el comentario ya ha sido marcado por el usuario actual, cambiamos el color
                            tint = if (commentObj.isReported) Naranja else NegroSuave,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Botón de Censura (Solo Administradores)
                if (isAdmin && !commentObj.isCensored) {
                    IconButton(onClick = onCensor, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = stringResource(R.string.censor),
                            tint = Rojo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Contenido del comentario o mensaje de censura
        if (commentObj.isCensored) {
            Text(
                text = stringResource(R.string.comment_censored),
                color = Rojo.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 6.dp)
            )
        } else if (commentObj.comment.isNotEmpty()) {
            Text(
                text = commentObj.comment,
                color = NegroSuave,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp),
            thickness = 0.5.dp,
            color = NegroMinimal
        )
    }
}