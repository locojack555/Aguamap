package cat.copernic.aguamap1.presentation.profile.moderation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.profile.moderation.components.EmptyModerationState
import cat.copernic.aguamap1.presentation.profile.moderation.components.ReportedCommentCard
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.PerfilGradient

/**
 * Pantalla de moderación para administradores.
 * Permite gestionar los comentarios reportados por la comunidad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationScreen(
    userLat: Double?, // NUEVO: Recibido desde el NavigationGraph
    userLng: Double?, // NUEVO: Recibido desde el NavigationGraph
    onBack: () -> Unit,
    onGoToFountain: (String) -> Unit,
    viewModel: ModerationViewModel = hiltViewModel()
) {
    // Sincronización de ubicación idéntica a CategoriesScreen
    LaunchedEffect(userLat, userLng) {
        if (userLat != null && userLng != null) {
            viewModel.setLocation(userLat, userLng)
        }
    }

    // Suscripción al estado del ViewModel
    val reportedComments by viewModel.reportedComments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorResId by viewModel.errorResId.collectAsStateWithLifecycle()
    val successResId by viewModel.successResId.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // GESTIÓN DE MENSAJES: Snackbars
    LaunchedEffect(errorResId) {
        errorResId?.let { id ->
            val message = context.resources.getString(id)
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(successResId) {
        successResId?.let { id ->
            val message = context.resources.getString(id)
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Blanco)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Cabecera con PerfilGradient
            ModerationHeader(
                pendingCount = reportedComments.size,
                onBack = onBack,
                onRefresh = { viewModel.loadReportedComments() }
            )

            // Pull to Refresh
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.loadReportedComments() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (!isLoading && reportedComments.isEmpty()) {
                    EmptyModerationState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reportedComments, key = { it.reportId }) { item ->
                            ReportedCommentCard(
                                item = item,
                                onDelete = { viewModel.deleteComment(item) },
                                onCensor = { viewModel.censorComment(item) },
                                onDismiss = { viewModel.dismissReport(item) },
                                onGoToFountain = { onGoToFountain(item.fountainId) }
                            )
                        }
                        item {
                            Spacer(
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .height(80.dp)
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

/**
 * Cabecera personalizada con PerfilGradient.
 */
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
            .background(PerfilGradient)
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