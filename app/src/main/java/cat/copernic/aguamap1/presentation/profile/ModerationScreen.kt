package cat.copernic.aguamap1.presentation.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import cat.copernic.aguamap1.R
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationScreen(
    onBack: () -> Unit,
    viewModel: ModerationViewModel = hiltViewModel()
) {
    val reportedComments by viewModel.reportedComments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(successMessage) {
        successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // ── Header ──────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF00B4DB), Color(0xFF0083B0))
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.content_description_return),
                            tint = Color.White
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(id = R.string.mod_comments_title),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(id = R.string.mod_comments_subtitle, reportedComments.size),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                    IconButton(onClick = { viewModel.loadReportedComments() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(id = R.string.content_description_reload), tint = Color.White)
                    }
                }
            }

            // ── Content ─────────────────────────────────────────────────────────
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.loadReportedComments() },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1F3F4))
            ) {
                if (!isLoading && reportedComments.isEmpty()) {
                    EmptyModerationState()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reportedComments, key = { it.reportId }) { item ->
                            ReportedCommentCard(
                                item = item,
                                onDelete = { viewModel.deleteComment(item) },
                                onCensor = { viewModel.censorComment(item) },
                                onDismiss = { viewModel.dismissReport(item) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

// ── Card ─────────────────────────────────────────────────────────────────────

@Composable
private fun ReportedCommentCard(
    item: cat.copernic.aguamap1.domain.model.ReportedComment,
    onDelete: () -> Unit,
    onCensor: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCensorDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val reportDate = remember(item.timestamp) {
        if (item.timestamp > 0) dateFormatter.format(Date(item.timestamp)) else "—"
    }

    if (showDeleteDialog) {
        ConfirmActionDialog(
            title = "Eliminar comentario",
            text = "El comentario será eliminado permanentemente.",
            confirmLabel = "Eliminar",
            confirmColor = Color(0xFFE53935),
            icon = Icons.Default.DeleteForever,
            onConfirm = { showDeleteDialog = false; onDelete() },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showCensorDialog) {
        ConfirmActionDialog(
            title = "Censurar comentario",
            text = "El comentario quedará oculto para todos los usuarios.",
            confirmLabel = "Censurar",
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

            // ── Report metadata ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            stringResource(id = R.string.mod_comments_reported),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE53935)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(reportDate, fontSize = 11.sp, color = Color(0xFF9E9E9E))
            }

            // ── Reason ──────────────────────────────────────────────────────
            if (item.reason.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier
                                .size(15.dp)
                                .padding(top = 1.dp)
                        )
                        Text(
                            stringResource(id = R.string.mod_comments_reason, item.reason),
                            fontSize = 12.sp,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
            }

            // ── Comment content ──────────────────────────────────────────────
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(12.dp))

            if (item.comment != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Avatar
                    Surface(
                        color = Color(0xFF0083B0),
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                item.comment.userName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Column {
                        Text(
                            item.comment.userName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF212121)
                        )
                        Row {
                            repeat(5) { index ->
                                Icon(
                                    if (index < item.comment.rating) Icons.Default.Star else Icons.Default.StarOutline,
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
                    item.comment.comment.ifBlank { stringResource(id = R.string.mod_comments_no_text) },
                    fontSize = 14.sp,
                    color = Color(0xFF424242),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )

            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.HideSource, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(16.dp))
                    Text(stringResource(id = R.string.mod_comments_no_available), fontSize = 13.sp, color = Color(0xFFBDBDBD))
                }
            }

            // ── Action buttons ───────────────────────────────────────────────
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(10.dp))

            // Fila 1: Descartar (ancho completo)
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF757575)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBDBDBD))
            ) {
                Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = R.string.mod_comments_discard), fontSize = 13.sp)
            }

            Spacer(Modifier.height(8.dp))

            // Fila 2: Censurar + Eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showCensorDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE65100))
                ) {
                    Icon(Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(id = R.string.mod_comments_censor), fontSize = 13.sp)
                }

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(id = R.string.mod_comments_remove), fontSize = 13.sp)
                }
            }
        }
    }
}

// ── Dialogs & Empty state ─────────────────────────────────────────────────────

@Composable
private fun ConfirmActionDialog(
    title: String,
    text: String,
    confirmLabel: String,
    confirmColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, contentDescription = null, tint = confirmColor) },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun EmptyModerationState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = Color(0xFFE8F5E9),
                shape = CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF43A047),
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
            Text(
                stringResource(id = R.string.mod_comments_no_comments),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF424242)
            )
            Text(
                stringResource(id = R.string.mod_comments_all_clean),
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}