package cat.copernic.aguamap1.presentation.fountain.detailFountain.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
    onReport: () -> Unit
) {
    val dateStr = remember(commentObj.timestamp) {
        SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(commentObj.timestamp))
    }

    // Usamos una Surface para dar un fondo sutil si es nuestro comentario
    Surface(
        color = if (isMyComment) cat.copernic.aguamap1.ui.theme.Celeste.copy(alpha = 0.3f) else cat.copernic.aguamap1.ui.theme.Blanco,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isMyComment) stringResource(R.string.you_label, commentObj.userName)
                        else commentObj.userName,
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
                                contentDescription = null, // Estrellas puramente visuales aquí
                                modifier = Modifier.size(14.dp),
                                tint = if (i < commentObj.rating) Naranja else GrisClaro
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(text = dateStr, fontSize = 11.sp, color = NegroSuave.copy(alpha = 0.7f))
                    }
                }

                Row {
                    // DUEÑO DEL COMENTARIO
                    if (isMyComment) {
                        if (!commentObj.censored) {
                            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit_comment),
                                    tint = cat.copernic.aguamap1.ui.theme.Blue10,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_confirm),
                                tint = Rojo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // OTRO USUARIO (Reportar)
                    if (!isMyComment && !isAdmin && !commentObj.censored) {
                        IconButton(onClick = onReport, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Flag,
                                contentDescription = stringResource(R.string.report_comment_label),
                                tint = if (commentObj.reported) Naranja else NegroSuave,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // ADMINISTRADOR (Censurar)
                    if (isAdmin && !commentObj.censored) {
                        IconButton(onClick = onCensor, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = stringResource(R.string.censor_comment_label),
                                tint = Rojo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // CONTENIDO
            if (commentObj.censored) {
                Surface(
                    color = Rojo.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.comment_censored),
                        color = Rojo.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Text(
                    text = commentObj.comment,
                    color = NegroSuave,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    lineHeight = 20.sp
                )
            }
        }
    }
    // Separador sutil fuera de la Surface para no cortar el diseño
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 8.dp),
        thickness = 0.5.dp,
        color = NegroMinimal
    )
}