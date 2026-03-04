package cat.copernic.aguamap1.presentation.fountain.detailFountain

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.StateFountain
import cat.copernic.aguamap1.presentation.fountain.comments.FountainCommentsViewModel
import cat.copernic.aguamap1.presentation.fountain.detailFountain.components.FountainActionButtons
import cat.copernic.aguamap1.presentation.fountain.detailFountain.components.FountainCommentsSection
import cat.copernic.aguamap1.presentation.fountain.detailFountain.components.FountainDetailDialogs
import cat.copernic.aguamap1.presentation.fountain.detailFountain.components.FountainDetailHeader
import cat.copernic.aguamap1.presentation.fountain.detailFountain.components.FountainSpecs
import cat.copernic.aguamap1.presentation.fountain.detailFountain.components.MainReportDialog
import cat.copernic.aguamap1.presentation.fountain.detailFountain.components.OtherReportDialog
import cat.copernic.aguamap1.presentation.fountain.detailFountain.components.ValidationCard
import cat.copernic.aguamap1.presentation.util.getStatusColor
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.NegroSuave
import cat.copernic.aguamap1.ui.theme.Rojo

/**
 * Pantalla de detalle de una fuente.
 * Gestiona la visualización de información, validación por votos, reportes y comentarios.
 */
@Composable
fun DetailFountainScreen(
    fountain: Fountain,
    viewModel: DetailFountainViewModel,
    commentsViewModel: FountainCommentsViewModel = hiltViewModel(), // ViewModel especializado en comentarios
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onConfirm: () -> Unit,
    onReportNoExiste: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current

    // Observa el estado operativo en tiempo real
    val isOpRealtime by viewModel.isOperationalRealtime.collectAsState()

    // Prioriza la fuente cargada en el VM (actualizada) sobre la recibida por parámetro
    val uiFountain = viewModel.selectedFountain ?: fountain
    val currentUserId = viewModel.currentUserId

    // Obtenemos el color de estado/categoría dinámico desde util
    val dynamicStatusColor = uiFountain.getStatusColor()

    // Comprueba si el usuario actual ya ha reportado que esta fuente no existe
    val hasVotedNegative =
        currentUserId != null && uiFountain.votedByNegative.contains(currentUserId)

    // --- ESTADOS LOCALES PARA CONTROLAR DIÁLOGOS ---
    var showReportDialog by remember { mutableStateOf(false) }
    var showOtherReportDialog by remember { mutableStateOf(false) }
    var otherReportText by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddCommentDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    var editingComment by remember { mutableStateOf<Comment?>(null) }

    // Al iniciar, selecciona la fuente en el VM e inicia la escucha de comentarios en tiempo real
    LaunchedEffect(fountain.id) {
        viewModel.selectFountain(fountain)
        commentsViewModel.observeComments(fountain.id)
    }

    // Gestiona el botón de retroceso para limpiar los listeners de Firebase
    BackHandler {
        commentsViewModel.stopObserving()
        onBack()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Blanco) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // 1. CABECERA
            FountainDetailHeader(
                imageUrl = uiFountain.imageUrl,
                isAdmin = viewModel.isAdmin,
                isOwner = currentUserId == uiFountain.createdBy,
                isPending = uiFountain.status == StateFountain.PENDING,
                onBack = { commentsViewModel.stopObserving(); onBack() },
                onEdit = onEdit,
                onDelete = { showDeleteConfirm = true }
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .fillMaxWidth()
            ) {

                // 2. INFO BÁSICA: Nombre y categoría con COLOR DINÁMICO
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        uiFountain.name,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f),
                        color = Negro
                    )
                    Surface(
                        color = dynamicStatusColor, // Aplicamos el color de la categoría aquí
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            uiFountain.category.name,
                            color = Blanco,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. TARJETAS DE VALIDACIÓN
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (uiFountain.status == StateFountain.PENDING && uiFountain.positiveVotes < 3) {
                        ValidationCard(
                            stringResource(R.string.pending_validation),
                            uiFountain.positiveVotes,
                            3,
                            Naranja,
                            Icons.Default.HourglassEmpty
                        )
                    }
                    if (uiFountain.negativeVotes > 0) {
                        ValidationCard(
                            stringResource(R.string.reported_non_existent),
                            uiFountain.negativeVotes,
                            3,
                            Rojo,
                            Icons.Default.Warning
                        )
                    }
                }

                // 4. DESCRIPCIÓN
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.description_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Negro
                )
                Text(
                    uiFountain.description.ifBlank { stringResource(R.string.no_description) },
                    color = NegroSuave
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 5. ESPECIFICACIONES TÉCNICAS: Pasa el estado de funcionamiento real
                FountainSpecs(uiFountain, viewModel, isOpRealtime)

                Spacer(modifier = Modifier.height(32.dp))

                // 6. BOTONES DE ACCIÓN
                FountainActionButtons(
                    uiFountain = uiFountain,
                    hasVotedPositive = currentUserId != null && uiFountain.votedByPositive.contains(
                        currentUserId
                    ),
                    onConfirm = onConfirm,
                    onReport = { showReportDialog = true }
                )

                Spacer(modifier = Modifier.height(40.dp))

                // 7. SECCIÓN DE COMENTARIOS
                FountainCommentsSection(
                    comments = commentsViewModel.comments,
                    currentUserId = currentUserId,
                    isAdmin = viewModel.isAdmin,
                    onAddClick = { showAddCommentDialog = true },
                    onEditComment = { editingComment = it },
                    onDeleteComment = { commentToDelete = it },
                    onReportComment = { commentsViewModel.onReportComment(uiFountain.id, it.id) }
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // --- 8. GESTOR DE DIÁLOGOS ---
    FountainDetailDialogs(
        showAddComment = showAddCommentDialog,
        editingComment = editingComment,
        commentToDelete = commentToDelete,
        showDeleteConfirm = showDeleteConfirm,
        onDismiss = {
            showAddCommentDialog = false
            showDeleteConfirm = false
            commentToDelete = null
            editingComment = null
        },
        onAddCommentConfirm = { r, t ->
            commentsViewModel.addComment(
                uiFountain,
                r,
                t
            ); showAddCommentDialog = false
        },
        onEditCommentConfirm = { r, t ->
            commentsViewModel.editComment(
                uiFountain,
                editingComment!!,
                r,
                t
            ); editingComment = null
        },
        onDeleteCommentConfirm = {
            commentsViewModel.deleteComment(
                uiFountain,
                commentToDelete!!
            ); commentToDelete = null
        },
        onDeleteFountainConfirm = { onDelete(); showDeleteConfirm = false }
    )

    if (showReportDialog) {
        MainReportDialog(
            negativeVotes = uiFountain.negativeVotes,
            hasVotedNegative = hasVotedNegative,
            isOperational = isOpRealtime,
            onDismiss = { showReportDialog = false },
            onConfirmExistence = { viewModel.confirmExistence { showReportDialog = false } },
            onReportNoExiste = { onReportNoExiste(); showReportDialog = false },
            onReportAveria = { viewModel.toggleOperationalStatus { showReportDialog = false } },
            onShowOther = { showOtherReportDialog = true; showReportDialog = false }
        )
    }

    if (showOtherReportDialog) {
        OtherReportDialog(
            textValue = otherReportText,
            onValueChange = { otherReportText = it },
            onDismiss = { showOtherReportDialog = false },
            onSend = {
                viewModel.reportOtherIssue(otherReportText) {
                    showOtherReportDialog = false
                    otherReportText = ""
                }
            }
        )
    }
}