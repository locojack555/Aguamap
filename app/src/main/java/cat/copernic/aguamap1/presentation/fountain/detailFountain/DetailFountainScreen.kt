package cat.copernic.aguamap1.presentation.fountain.addFountain.detailFountain

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.StateFountain
import cat.copernic.aguamap1.presentation.fountain.comments.AddCommentDialog
import cat.copernic.aguamap1.presentation.fountain.comments.FountainCommentsViewModel
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.BlancoTranslucido
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.GrisClaro
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.NegroMinimal
import cat.copernic.aguamap1.ui.theme.NegroMuySuave
import cat.copernic.aguamap1.ui.theme.NegroSuave
import cat.copernic.aguamap1.ui.theme.Rojo
import cat.copernic.aguamap1.ui.theme.Verde
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetailFountainScreen(
    fountain: Fountain,
    viewModel: DetailFountainViewModel,
    commentsViewModel: FountainCommentsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onConfirm: () -> Unit,
    onReportAveria: () -> Unit,
    onReportNoExiste: () -> Unit
) {
    // --- ESTADOS LOCALES PARA DIÁLOGOS ---
    var showReportDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddCommentDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    var editingComment by remember { mutableStateOf<Comment?>(null) }

    val currentUserId = viewModel.currentUserId
    val hasVotedPositive = currentUserId != null && fountain.votedByPositive.contains(currentUserId)
    val hasVotedNegative = currentUserId != null && fountain.votedByNegative.contains(currentUserId)

    LaunchedEffect(fountain.id) {
        commentsViewModel.observeComments(fountain.id)
    }

    BackHandler {
        commentsViewModel.stopObserving()
        onBack()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Blanco
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // --- CABECERA CON IMAGEN ---
            Box(
                modifier = Modifier
                    .height(280.dp)
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
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { commentsViewModel.stopObserving(); onBack() },
                        modifier = Modifier.background(BlancoTranslucido, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Negro)
                    }

                    if (viewModel.isAdmin) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.background(
                                BlancoTranslucido,
                                RoundedCornerShape(12.dp)
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Rojo)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .fillMaxWidth()
            ) {
                // Título y Categoría
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = fountain.name,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f),
                        color = Negro
                    )
                    Surface(color = Verde, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = fountain.category.name,
                            color = Blanco,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Star, null, tint = Naranja, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", fountain.ratingAverage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Negro
                    )
                    Text(text = " / 5.0", fontSize = 14.sp, color = NegroSuave)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- TARJETAS DE VALIDACIÓN ---
                if (fountain.status == StateFountain.PENDING && fountain.positiveVotes < 3) {
                    ValidationCard(
                        stringResource(R.string.pending_validation),
                        fountain.positiveVotes,
                        3,
                        Naranja,
                        Icons.Default.HourglassEmpty
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (fountain.negativeVotes > 0) {
                    ValidationCard(
                        stringResource(R.string.reported_non_existent),
                        fountain.negativeVotes,
                        3,
                        Rojo,
                        Icons.Default.Warning
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Text(
                    text = stringResource(R.string.description_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Negro
                )
                Text(
                    text = fountain.description.ifBlank { stringResource(R.string.no_description) },
                    color = NegroSuave,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                InfoRow(
                    Icons.Default.LocationOn,
                    stringResource(R.string.distancia_label),
                    viewModel.getDistanceText(fountain.distanceFromUser),
                    Blue10
                )
                InfoRow(
                    Icons.Default.Build,
                    stringResource(R.string.estado_label),
                    if (fountain.operational) stringResource(R.string.funcionando) else stringResource(
                        R.string.averiada
                    ),
                    if (fountain.operational) Verde else Rojo
                )
                InfoRow(
                    Icons.Default.CalendarMonth,
                    stringResource(R.string.fecha_alta_label),
                    viewModel.getFormattedDate(fountain.dateCreated),
                    NegroSuave
                )
                InfoRow(
                    Icons.Default.Map,
                    stringResource(R.string.coordenadas_label),
                    viewModel.getFormattedCoordinates(fountain.latitude, fountain.longitude),
                    NegroSuave
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- BOTONES DE ACCIÓN ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (fountain.positiveVotes < 3) {
                        Button(
                            onClick = onConfirm,
                            enabled = !hasVotedPositive,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Blue10,
                                disabledContainerColor = GrisClaro
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                if (hasVotedPositive) Icons.Default.Check else Icons.Default.CheckCircle,
                                null,
                                modifier = Modifier.size(18.dp), tint = Blanco
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (hasVotedPositive) "Votado" else stringResource(R.string.confirm_button),
                                fontSize = 14.sp,
                                color = Blanco
                            )
                        }
                    }

                    Button(
                        onClick = { showReportDialog = true },
                        enabled = !hasVotedNegative,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Rojo,
                            disabledContainerColor = GrisClaro,
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            null,
                            modifier = Modifier.size(18.dp),
                            tint = Blanco
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (hasVotedNegative) "Reportado" else stringResource(R.string.report_button),
                            fontSize = 14.sp,
                            color = Blanco
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // --- SECCIÓN COMENTARIOS ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(
                            R.string.ratings_title,
                            commentsViewModel.comments.size
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        modifier = Modifier.weight(1f),
                        color = Negro
                    )
                    TextButton(onClick = { showAddCommentDialog = true }) {
                        Text(
                            stringResource(R.string.add_comment),
                            color = Blue10,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (commentsViewModel.comments.isEmpty()) {
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
                    commentsViewModel.comments.forEach { comment ->
                        CommentItem(
                            commentObj = comment,
                            isMyComment = comment.userId == currentUserId,
                            isAdmin = viewModel.isAdmin,
                            onCensor = { commentsViewModel.censorComment(fountain.id, comment.id) },
                            onDelete = { commentToDelete = comment },
                            onEdit = { editingComment = comment },
                            onReport = {
                                commentsViewModel.onReportComment(
                                    fountain.id,
                                    comment.id
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // --- LÓGICA DE DIÁLOGOS DE COMENTARIOS ---

    // --- PARA AÑADIR ---
    if (showAddCommentDialog) {
        AddCommentDialog(
            isEditing = false, // Forzamos que salga "Añadir"
            onDismiss = { showAddCommentDialog = false },
            onConfirm = { rating, text ->
                commentsViewModel.addComment(fountain, rating, text)
                showAddCommentDialog = false
            }
        )
    }

// --- PARA EDITAR ---
    editingComment?.let { comment ->
        AddCommentDialog(
            initialRating = comment.rating,
            initialText = comment.comment,
            isEditing = true, // Forzamos que salga "Editar" aunque el texto sea ""
            onDismiss = { editingComment = null },
            onConfirm = { newRating, newText ->
                commentsViewModel.editComment(fountain, comment, newRating, newText)
                editingComment = null
            }
        )
    }

    // --- OTROS DIÁLOGOS ---
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(stringResource(R.string.report_dialog_title)) },
            text = { Text(stringResource(R.string.report_dialog_text)) },
            confirmButton = {
                TextButton(onClick = { onReportNoExiste(); showReportDialog = false }) {
                    Text(
                        stringResource(R.string.report_not_exists),
                        color = Rojo,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onReportAveria(); showReportDialog = false }) {
                    Text(
                        if (fountain.operational) stringResource(R.string.report_broken) else stringResource(
                            R.string.report_fixed
                        ), color = Blue10
                    )
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_fountain_title)) },
            text = { Text(stringResource(R.string.delete_fountain_confirm_text)) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text(
                        stringResource(R.string.delete_confirm),
                        color = Rojo,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    commentToDelete?.let { comment ->
        AlertDialog(
            onDismissRequest = { commentToDelete = null },
            title = { Text(stringResource(R.string.delete_comment_title)) },
            confirmButton = {
                TextButton(onClick = {
                    commentsViewModel.deleteComment(fountain, comment)
                    commentToDelete = null
                }) {
                    Text(
                        stringResource(R.string.delete_confirm),
                        color = Rojo,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    commentToDelete = null
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

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
    val dateStr =
        SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(commentObj.timestamp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    commentObj.userName,
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
                            Icons.Default.Star, null,
                            modifier = Modifier.size(14.dp),
                            tint = if (i < commentObj.rating) Naranja else GrisClaro
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(dateStr, fontSize = 11.sp, color = NegroMuySuave)
                }
            }
            Row {
                if (isMyComment) {
                    if (!commentObj.isCensored) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                null,
                                tint = NegroSuave,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            tint = NegroSuave,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (!isMyComment && !isAdmin) {
                    IconButton(onClick = onReport, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Flag,
                            null,
                            tint = if (commentObj.isReported) Naranja else NegroSuave,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (isAdmin && !commentObj.isCensored) {
                    IconButton(onClick = onCensor, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Block,
                            null,
                            tint = Rojo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        if (commentObj.isCensored) {
            Text(
                stringResource(R.string.comment_censored),
                color = Rojo.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 6.dp)
            )
        } else if (commentObj.comment.isNotEmpty()) {
            Text(
                commentObj.comment,
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

@Composable
fun ValidationCard(title: String, count: Int, target: Int, color: Color, icon: ImageVector) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Text(
                    text = stringResource(R.string.validation_count_plural, count, target),
                    color = color.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = NegroMuySuave)
        Spacer(Modifier.width(12.dp))
        Text("$label: ", color = NegroSuave, fontSize = 15.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}