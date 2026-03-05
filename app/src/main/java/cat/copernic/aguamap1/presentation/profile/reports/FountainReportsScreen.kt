package cat.copernic.aguamap1.presentation.profile.reports

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
import cat.copernic.aguamap1.presentation.profile.reports.components.EmptyFountainReportsState
import cat.copernic.aguamap1.presentation.profile.reports.components.FountainReportCard
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.PerfilGradient
import kotlinx.coroutines.delay

/**
 * Pantalla principal de gestión de reportes de fuentes.
 * Permite a los administradores visualizar una lista de problemas reportados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FountainReportsScreen(
    userLat: Double?, // NUEVO: Recibido desde el NavigationGraph
    userLng: Double?, // NUEVO: Recibido desde el NavigationGraph
    onBack: () -> Unit,
    onGoToFountain: (fountainId: String) -> Unit,
    viewModel: FountainReportsViewModel = hiltViewModel()
) {
    // Sincronización de ubicación igual que en CategoriesScreen
    // Esto asegura que el ViewModel tenga la última ubicación conocida
    LaunchedEffect(userLat, userLng) {
        if (userLat != null && userLng != null) {
            // Si tu ViewModel de reportes tiene setLocation, úsalo.
            // Si no, al menos garantiza que las variables se propaguen al navegar.
            viewModel.setLocation(userLat, userLng)
        }
    }

    // Suscripción reactiva a los estados del ViewModel
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val userNames by viewModel.userNames.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorResId by viewModel.errorResId.collectAsStateWithLifecycle(initialValue = null)
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle(initialValue = false)

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // MANEJO DE EFECTOS: Feedback visual ante errores
    LaunchedEffect(errorResId) {
        errorResId?.let { id ->
            val messageText = context.resources.getString(id)
            snackbarHostState.showSnackbar(message = messageText)
            viewModel.clearError()
        }
    }

    // MANEJO DE EFECTOS: Feedback visual ante éxito al resolver un reporte
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            val successText = context.resources.getString(R.string.success_report_resolved)
            snackbarHostState.showSnackbar(message = successText)
            delay(3000)
            viewModel.resetSuccess()
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
            // Cabecera manual
            ReportsHeader(
                count = reports.size,
                onBack = onBack,
                onRefresh = { viewModel.loadReports() }
            )

            // Componente de refresco nativo de Material 3
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.loadReports() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (!isLoading && reports.isEmpty()) {
                    EmptyFountainReportsState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reports, key = { it.id }) { report ->
                            FountainReportCard(
                                report = report,
                                reporterName = userNames[report.userId] ?: "...",
                                onResolve = { viewModel.resolveReport(report.id) },
                                onGoToFountain = { onGoToFountain(report.fountainId) }
                                // Si FountainReportCard muestra distancia, pásale userLat y userLng aquí
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
 * Cabecera estilizada con degradado y contador dinámico.
 */
@Composable
private fun ReportsHeader(
    count: Int,
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
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.reports_title),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                val pendingText = context.resources.getQuantityString(
                    R.plurals.pending_count_short,
                    count,
                    count
                )

                Text(
                    text = pendingText,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}