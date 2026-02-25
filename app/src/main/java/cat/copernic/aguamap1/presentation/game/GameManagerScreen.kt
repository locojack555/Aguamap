package cat.copernic.aguamap1.presentation.game

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.presentation.fountain.addFountain.detailFountain.DetailFountainViewModel
import cat.copernic.aguamap1.presentation.fountain.comments.FountainCommentsViewModel
import cat.copernic.aguamap1.presentation.fountain.detailFountain.DetailFountainScreen
import cat.copernic.aguamap1.presentation.game.components.ErrorScreen
import cat.copernic.aguamap1.presentation.game.components.LoadingPartida
import cat.copernic.aguamap1.presentation.game.screens.GameInstructionsScreen
import cat.copernic.aguamap1.presentation.game.screens.GamePlayScreen
import cat.copernic.aguamap1.presentation.game.screens.GameResultScreen
import cat.copernic.aguamap1.presentation.util.PermissionRequestUI
import cat.copernic.aguamap1.ui.theme.Blanco
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    detailFountainViewModel: DetailFountainViewModel = hiltViewModel(), // AÑADIDO
    commentsViewModel: FountainCommentsViewModel = hiltViewModel(), // AÑADIDO
    onBackToHome: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val currentFountain by viewModel.currentFountain.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val score by viewModel.score.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val userGuessPos by viewModel.userGuessPos.collectAsState()
    val hasLost by viewModel.hasLost.collectAsState()

    // Estado para controlar si se muestran los detalles de una fuente
    var showFountainDetails by remember { mutableStateOf<Fountain?>(null) }

    // Estado para la ubicación del usuario
    var currentUserLat by remember { mutableDoubleStateOf(0.0) }
    var currentUserLng by remember { mutableDoubleStateOf(0.0) }
    var isLocationReady by remember { mutableStateOf(false) }

    // Permiso de ubicación
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val context = LocalContext.current

    // Efecto para obtener la ubicación cuando se concede el permiso
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                val lastLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                lastLocation?.let {
                    currentUserLat = it.latitude
                    currentUserLng = it.longitude
                    isLocationReady = true
                }
            } catch (e: SecurityException) {
                isLocationReady = false
            }
        }
    }

    // INICIAR JUEGO
    LaunchedEffect(isLocationReady) {
        if (isLocationReady && gameState == GameViewModel.GameState.Initial) {
            viewModel.checkCanPlay(currentUserLat, currentUserLng)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onBackToHomePressed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Blanco)
    ) {
        if (!locationPermissionState.status.isGranted) {
            PermissionRequestUI {
                if (locationPermissionState.status.shouldShowRationale) {
                    locationPermissionState.launchPermissionRequest()
                } else {
                    locationPermissionState.launchPermissionRequest()
                }
            }
        } else {
            // Si hay una fuente seleccionada para ver detalles, mostramos DetailFountainScreen
            if (showFountainDetails != null) {
                DetailFountainScreen(
                    fountain = showFountainDetails!!,
                    viewModel = detailFountainViewModel,
                    commentsViewModel = commentsViewModel,
                    onBack = { showFountainDetails = null },
                    onDelete = {
                        // Aquí puedes manejar lo que pasa cuando se elimina
                        showFountainDetails = null
                    },
                    onConfirm = {
                        // Aquí puedes manejar la confirmación
                    },
                    onReportAveria = {
                        // Aquí puedes manejar el reporte de avería
                    },
                    onReportNoExiste = {
                        // Aquí puedes manejar el reporte de no existencia
                    }
                )
            } else {
                // Mostrar el juego normalmente
                when (gameState) {
                    GameViewModel.GameState.Initial -> {
                        if (!isLocationReady) {
                            LoadingPartida()
                        } else {
                            LoadingPartida()
                        }
                    }

                    GameViewModel.GameState.Instructions -> {
                        GameInstructionsScreen(onStart = { viewModel.onStartGameClicked() })
                    }

                    GameViewModel.GameState.Playing -> {
                        currentFountain?.let {
                            GamePlayScreen(
                                fountain = it,
                                remainingTime = remainingTime,
                                userLocation = if (isLocationReady) GeoPoint(
                                    currentUserLat,
                                    currentUserLng
                                ) else null,
                                onGuess = { lat, lng -> viewModel.setUserGuess(lat, lng) },
                                onFinish = { viewModel.finishGame() }
                            )
                        }
                    }

                    GameViewModel.GameState.Finished -> {
                        currentFountain?.let {
                            GameResultScreen(
                                fountain = it,
                                score = score,
                                distance = distance,
                                userGuessPos = userGuessPos?.let { pos ->
                                    GeoPoint(pos.first, pos.second)
                                },
                                hasLost = hasLost,
                                onBackToHome = {
                                    viewModel.onBackToHomePressed()
                                    onBackToHome()
                                },
                                onFountainClick = { fountain ->
                                    // Aquí mostramos los detalles de la fuente
                                    showFountainDetails = fountain
                                }
                            )
                        }
                    }

                    GameViewModel.GameState.DailyLimitReached -> ErrorScreen(
                        message = error ?: stringResource(R.string.game_error_daily_limit),
                        onRetry = null,
                        onBack = {
                            viewModel.onBackToHomePressed()
                            onBackToHome()
                        }
                    )

                    else -> ErrorScreen(
                        message = error ?: stringResource(R.string.game_error_unexpected),
                        onRetry = { viewModel.retryGame() },
                        onBack = {
                            viewModel.onBackToHomePressed()
                            onBackToHome()
                        }
                    )
                }
            }

            if (isLoading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blanco)
                }
            }
        }
    }
}