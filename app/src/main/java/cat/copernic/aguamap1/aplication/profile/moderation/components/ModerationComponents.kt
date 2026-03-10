package cat.copernic.aguamap1.aplication.profile.moderation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.comment.ReportedComment
import cat.copernic.aguamap1.aplication.utils.VerdeHoja
import cat.copernic.aguamap1.ui.theme.Negro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tarjeta interactiva para la gestión de comentarios reportados.
 * Ahora incluye la opción de navegar a la fuente para ver el detalle global.
 */
@Composable
fun ReportedCommentCard(
    item: ReportedComment,
    onDelete: () -> Unit,
    onCensor: () -> Unit,
    onDismiss: () -> Unit,
    onGoToFountain: () -> Unit // NUEVO: Acción para navegar al detalle global
) {
    // Estados locales para controlar la visibilidad de los diálogos de confirmación
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCensorDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val reportDate = remember(item.timestamp) {
        if (item.timestamp > 0) dateFormatter.format(Date(item.timestamp)) else "—"
    }

    // Lógica de Diálogos
    if (showDeleteDialog) {
        ModerationConfirmDialog(
            title = stringResource(R.string.moderation_dialog_delete_title),
            text = stringResource(R.string.moderation_dialog_delete_desc),
            confirmLabel = stringResource(R.string.common_delete),
            confirmColor = Color(0xFFE53935),
            icon = Icons.Default.DeleteForever,
            onConfirm = { showDeleteDialog = false; onDelete() },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showCensorDialog) {
        ModerationConfirmDialog(
            title = stringResource(R.string.moderation_dialog_censor_title),
            text = stringResource(R.string.moderation_dialog_censor_desc),
            confirmLabel = stringResource(R.string.moderation_btn_censor),
            confirmColor = Color(0xFFE65100),
            icon = Icons.Default.VisibilityOff,
            onConfirm = { showCensorDialog = false; onCensor() },
            onDismiss = { showCensorDialog = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // CABECERA: Badge de Reportado y Fecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.moderation_badge_reported),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE53935)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(reportDate, fontSize = 11.sp, color = Color(0xFF9E9E9E))
            }

            // MOTIVO DEL REPORTE
            if (item.reason.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), Arrangement.spacedBy(6.dp)) {
                        Icon(
                            Icons.Default.WarningAmber,
                            null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = stringResource(R.string.moderation_reason_label, item.reason),
                            fontSize = 12.sp,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(12.dp))

            // CUERPO: Info del usuario y contenido del comentario
            if (item.comment != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFF0083B0),
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = item.comment.userName.firstOrNull()?.uppercase() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column {
                        Text(
                            item.comment.userName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Row {
                            repeat(5) {
                                Icon(
                                    imageVector = if (it < item.comment.rating) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.comment.comment.ifBlank { stringResource(R.string.reports_no_description) },
                    fontSize = 14.sp,
                    color = Color(0xFF424242),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = stringResource(R.string.moderation_comment_unavailable),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(10.dp))

            // ACCIÓN: IR A LA FUENTE (DETALLE GLOBAL)
            Button(
                onClick = onGoToFountain,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0083B0))
            ) {
                Icon(Icons.Default.Map, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.reports_go_to_fountain), // Asegúrate de tener este string o similar
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // ACCIONES: Panel de botones de moderación
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFBDBDBD))
            ) {
                Icon(Icons.Default.ThumbUp, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.moderation_btn_dismiss),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showCensorDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100)),
                    border = BorderStroke(1.dp, Color(0xFFE65100))
                ) {
                    Text(stringResource(R.string.moderation_btn_censor), fontSize = 13.sp)
                }
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text(stringResource(R.string.common_delete), fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * Diálogo de confirmación genérico para las acciones de moderación.
 */
@Composable
fun ModerationConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    confirmColor: Color,
    icon: ImageVector,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, null, tint = confirmColor) },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

/**
 * Estado vacío cuando no hay reportes pendientes de revisión.
 */
@Composable
fun EmptyModerationState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            null,
            tint = VerdeHoja,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.moderation_empty_title),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Negro
        )
        Text(stringResource(R.string.moderation_empty_desc), color = Negro)
    }
}