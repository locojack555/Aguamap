package cat.copernic.aguamap1.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.copernic.aguamap1.domain.model.ReportedComment
import java.text.SimpleDateFormat
import java.util.*

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

    val pendingCount = reportedComments.count { !it.isResolved }
    val resolvedCount = reportedComments.count { it.isResolved }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                Column {
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
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Moderación",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Comentarios reportados",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                        IconButton(onClick = { viewModel.loadReportedComments() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = Color.White)
                        }
                    }

                    // Stats chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatChip(count = pendingCount, label = "Pendientes", containerColor = Color(0xFFFF6B6B))
                        StatChip(count = resolvedCount, label = "Resueltos", containerColor = Color(0xFF4CAF50))
                        StatChip(count = reportedComments.size, label = "Total", containerColor = Color.White.copy(alpha = 0.25f))
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
                        // Pending section
                        val pending = reportedComments.filter { !it.isResolved }
                        val resolved = reportedComments.filter { it.isResolved }

                        if (pending.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "Pendientes",
                                    count = pending.size,
                                    color = Color(0xFFE53935)
                                )
                            }
                            items(pending, key = { it.reportId }) { item ->
                                ReportedCommentCard(
                                    item = item,
                                    onDelete = { viewModel.deleteComment(item) },
                                    onDismiss = { viewModel.dismissReport(item) }
                                )
                            }
                        }

                        if (resolved.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(4.dp))
                                SectionHeader(
                                    title = "Resueltos",
                                    count = resolved.size,
                                    color = Color(0xFF43A047)
                                )
                            }
                            items(resolved, key = { it.reportId }) { item ->
                                ReportedCommentCard(
                                    item = item,
                                    onDelete = {},
                                    onDismiss = {},
                                    isResolved = true
                                )
                            }
                        }

                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}


@Composable
private fun StatChip(count: Int, label: String, containerColor: Color) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                count.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                label,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Surface(color = color, shape = CircleShape, modifier = Modifier.size(8.dp)) {}
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF616161)
        )
        Text(
            "($count)",
            fontSize = 13.sp,
            color = Color(0xFF9E9E9E)
        )
    }
}

@Composable
private fun ReportedCommentCard(
    item: ReportedComment,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    isResolved: Boolean = false
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val reportDate = remember(item.timestamp) {
        if (item.timestamp > 0) dateFormatter.format(Date(item.timestamp)) else "—"
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            onConfirm = { showDeleteDialog = false; onDelete() },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isResolved) Color(0xFFF9FBE7) else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (isResolved) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Report header ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isResolved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (isResolved) Icons.Default.CheckCircle else Icons.Default.Flag,
                            contentDescription = null,
                            tint = if (isResolved) Color(0xFF43A047) else Color(0xFFE53935),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            if (isResolved) "Resuelto" else "Pendiente",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isResolved) Color(0xFF43A047) else Color(0xFFE53935)
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Text(
                    reportDate,
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E)
                )
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
                            modifier = Modifier.size(16.dp).padding(top = 1.dp)
                        )
                        Text(
                            "Motivo: ${item.reason}",
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
                // Author row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFF0083B0),
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
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
                        // Stars
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

                // Comment text
                Text(
                    item.comment.comment.ifBlank { "(Sin texto)" },
                    fontSize = 14.sp,
                    color = Color(0xFF424242),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )

            } else {
                // Comment not found (deleted externally)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.HideSource, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(16.dp))
                    Text("Comentario no disponible", fontSize = 13.sp, color = Color(0xFFBDBDBD))
                }
            }

            // ── Action buttons ───────────────────────────────────────────────
            if (!isResolved) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dismiss button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF43A047)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF43A047))
                    ) {
                        Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Descartar", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    // Delete button
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Eliminar", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFE53935))
        },
        title = {
            Text("Eliminar comentario", fontWeight = FontWeight.Bold)
        },
        text = {
            Text("Esta acción es permanente. ¿Seguro que quieres eliminar este comentario?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
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
                "Sin reportes pendientes",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF424242)
            )
            Text(
                "Todo está limpio por aquí",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}