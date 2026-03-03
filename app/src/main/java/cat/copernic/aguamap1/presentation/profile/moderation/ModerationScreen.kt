package cat.copernic.aguamap1.presentation.profile.moderation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.profile.moderation.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationScreen(
    onBack: () -> Unit,
    viewModel: ModerationViewModel = hiltViewModel()
) {
    val reportedComments by viewModel.reportedComments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorResId by viewModel.errorResId.collectAsStateWithLifecycle()
    val successResId by viewModel.successResId.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Gestión de Errores
    LaunchedEffect(errorResId) {
        errorResId?.let { id ->
            // Usamos context.resources para evitar el warning del IDE
            val message = context.resources.getString(id)
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    // Gestión de Éxito
    LaunchedEffect(successResId) {
        successResId?.let { id ->
            val message = context.resources.getString(id)
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            ModerationHeader(
                pendingCount = reportedComments.size,
                onBack = onBack,
                onRefresh = { viewModel.loadReportedComments() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF1F3F4))
        ) {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.loadReportedComments() },
                modifier = Modifier.fillMaxSize()
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

@Composable
private fun ModerationHeader(
    pendingCount: Int,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF00B4DB), Color(0xFF0083B0))
                )
            )
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = Color.White
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.moderation_title),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = context.resources.getQuantityString(
                        R.plurals.moderation_pending_count,
                        pendingCount,
                        pendingCount
                    ),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.common_refresh),
                    tint = Color.White
                )
            }
        }
    }
}