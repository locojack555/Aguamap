package cat.copernic.aguamap1.presentation.home.map

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.Rojo
import cat.copernic.aguamap1.ui.theme.Verde
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetailFountainScreen(
    fountain: Fountain,
    isAdmin: Boolean,
    currentUserId: String?,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onConfirm: () -> Unit,
    onReportAveria: () -> Unit,
    onReportNoExiste: () -> Unit,
    onAddComment: () -> Unit,
    onCensorComment: (String) -> Unit,
    onDeleteComment: (String) -> Unit,
    onEditComment: (Comment) -> Unit
) {
    var showReportDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDeleteCommentConfirm by remember { mutableStateOf<String?>(null) }

    // Formateador para la fecha de creación
    val creationDate = remember(fountain.dateCreated) {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        sdf.format(fountain.dateCreated)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Blanco) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // --- CABECERA ---
            Box(
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth()
            ) {
                AsyncImage(
                    model = fountain.imageUrl,
                    contentDescription = null,
                    placeholder = painterResource(R.drawable.pin_lleno),
                    error = painterResource(R.drawable.pin_lleno),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(Blanco.copy(0.7f), RoundedCornerShape(12.dp))
                    ) { Icon(Icons.Default.ArrowBack, "Atrás") }

                    if (isAdmin) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.background(
                                Blanco.copy(0.7f),
                                RoundedCornerShape(12.dp)
                            )
                        ) { Icon(Icons.Default.Delete, "Borrar", tint = Rojo) }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Título y Categoría
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        fountain.name,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f),
                        color = Negro
                    )
                    Surface(color = Verde, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            fountain.category.name,
                            color = Blanco,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }
                }

                // --- Puntuación Media ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(Icons.Default.Star, null, tint = Naranja, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", fountain.ratingAverage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Negro
                    )
                    Text(
                        text = " / 5",
                        fontSize = 14.sp,
                        color = Negro.copy(0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- CARDS DE VALIDACIÓN Y REPORTES ---
                if (fountain.status.name == "PENDING") {
                    ValidationCard(
                        "Pendiente de validación",
                        fountain.positiveVotes,
                        3,
                        Naranja,
                        Icons.Default.HourglassEmpty
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (fountain.negativeVotes > 0) {
                    ValidationCard(
                        "Reportada como inexistente",
                        fountain.negativeVotes,
                        3,
                        Rojo,
                        Icons.Default.Warning
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Descripción", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = if (fountain.description.isBlank()) "Sin descripción disponible." else fountain.description,
                    color = Negro.copy(0.7f), fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- INFO ROWS (Ubicación y Estado) ---
                val distanceText = fountain.distanceFromUser?.let {
                    if (it < 1000) "${it.toInt()}m" else String.format(
                        Locale.US,
                        "%.1fkm",
                        it / 1000.0
                    )
                } ?: "---"

                InfoRow(Icons.Default.LocationOn, "Distancia", distanceText, Blue10)
                InfoRow(
                    Icons.Default.Warning,
                    "Estado",
                    if (fountain.operational) "Funcionando" else "Averiada",
                    if (fountain.operational) Verde else Rojo
                )

                // --- NUEVAS FILAS: FECHA Y COORDENADAS ---
                InfoRow(
                    Icons.Default.CalendarMonth,
                    "Fecha de alta",
                    creationDate,
                    Negro.copy(0.7f)
                )
                InfoRow(
                    Icons.Default.Map,
                    "Coordenadas",
                    "${String.format("%.4f", fountain.latitude)}; ${
                        String.format(
                            "%.4f",
                            fountain.longitude
                        )
                    }",
                    Negro.copy(0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- BOTONES DE ACCIÓN ---
                if (isAdmin) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Editar fuente (Admin)")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (fountain.status.name == "PENDING") {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Confirmar", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = { showReportDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Rojo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Flag, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Reportar", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- COMENTARIOS ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Valoraciones (${fountain.comments.size})",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onAddComment) { Text("Añadir") }
                }

                if (fountain.comments.isEmpty()) {
                    Text(
                        "¡Sé el primero en comentar!",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        color = Negro.copy(0.5f),
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    fountain.comments.forEach { comment ->
                        CommentItem(
                            commentObj = comment,
                            isMyComment = comment.userId == currentUserId,
                            isAdmin = isAdmin,
                            onCensor = { onCensorComment(comment.id) },
                            onDelete = { showDeleteCommentConfirm = comment.id },
                            onEdit = { onEditComment(comment) }
                        )
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS ---
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Reportar incidencia") },
            text = { Text("¿Qué problema has encontrado en esta fuente?") },
            confirmButton = {
                TextButton(onClick = { onReportNoExiste(); showReportDialog = false }) {
                    Text("No existe", color = Rojo, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onReportAveria(); showReportDialog = false }) {
                    Text(
                        if (fountain.operational) "Está averiada" else "Ya funciona",
                        color = Blue10
                    )
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Borrar fuente") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Eliminar", color = Rojo)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    // --- DIÁLOGO PARA ELIMINAR COMENTARIO ---
    showDeleteCommentConfirm?.let { commentId ->
        AlertDialog(
            onDismissRequest = { showDeleteCommentConfirm = null },
            title = { Text("Eliminar valoración") },
            text = { Text("¿Estás seguro de que deseas eliminar esta valoración? Esta acción es permanente.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteComment(commentId)
                    showDeleteCommentConfirm = null
                }) {
                    Text("Eliminar", color = Rojo)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCommentConfirm = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ValidationCard(title: String, count: Int, target: Int, color: Color, icon: ImageVector) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(0.1f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.2f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = if (count == 1) "1 persona ha reportado esto." else "$count de $target personas han reportado esto.",
                    color = color.copy(0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CommentItem(
    commentObj: Comment,
    isMyComment: Boolean,
    isAdmin: Boolean,
    onCensor: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val dateStr =
        SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(commentObj.timestamp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(commentObj.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row {
                    repeat(5) { i ->
                        Icon(
                            Icons.Default.Star,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = if (i < commentObj.rating) Naranja else Color.LightGray
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(dateStr, fontSize = 10.sp, color = Negro.copy(0.4f))
                }
            }
            if (isMyComment && !commentObj.isCensored) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        "Editar",
                        tint = Negro.copy(0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (isMyComment || isAdmin) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        "Borrar",
                        tint = Negro.copy(0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (isAdmin && !commentObj.isCensored) {
                IconButton(
                    onClick = onCensor,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Block,
                        "Censurar",
                        tint = Rojo,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        if (commentObj.isCensored) {
            Text(
                "[Contenido eliminado por moderación]",
                color = Rojo.copy(0.6f),
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else if (commentObj.comment.isNotEmpty()) {
            Text(
                commentObj.comment,
                color = Negro.copy(0.8f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            thickness = 0.5.dp,
            color = Negro.copy(0.1f)
        )
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = Negro.copy(0.6f))
        Spacer(Modifier.width(8.dp))
        Text("$label: ", color = Negro.copy(0.6f), fontSize = 14.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}