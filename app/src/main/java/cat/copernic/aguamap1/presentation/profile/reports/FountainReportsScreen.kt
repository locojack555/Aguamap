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
import androidx.compose.material3.Scaffold
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
import cat.copernic.aguamap1.presentation.profile.reports.components.EmptyFountainReportsState
import cat.copernic.aguamap1.presentation.profile.reports.components.FountainReportCard
import kotlinx.coroutines.delay

/**
 * Pantalla principal de gestión de reportes de fuentes.
 * Permite a los administradores visualizar una lista de problemas reportados,
 * refrescar los datos mediante gestos y navegar a la ubicación de la fuente reportada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FountainReportsScreen(
    onBack: () -> Unit,
    onGoToFountain: (fountainId: String) -> Unit,
    viewModel: FountainReportsViewModel = hiltViewModel()
) {
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ReportsHeader(
                count = reports.size,
                onBack = onBack,
                onRefresh = { viewModel.loadReports() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF1F3F4))
        ) {
            // Componente de refresco nativo de Material 3
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.loadReports() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (!isLoading && reports.isEmpty()) {
                    // Estado vacío cuando no hay reportes pendientes
                    EmptyFountainReportsState()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reports, key = { it.id }) { report ->
                            FountainReportCard(
                                report = report,
                                reporterName = userNames[report.userId] ?: "...",
                                onResolve = { viewModel.resolveReport(report.id) },
                                onGoToFountain = { onGoToFountain(report.fountainId) }
                            )
                        }
                        // Espacio de cortesía para no tapar el contenido con botones flotantes
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
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

                // Uso de Plurals para adaptar el texto según la cantidad de reportes
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