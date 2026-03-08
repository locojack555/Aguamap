package cat.copernic.aguamap1.aplication.game

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.aplication.game.components.ErrorScreen
import cat.copernic.aguamap1.aplication.game.components.LoadingPartida
import cat.copernic.aguamap1.aplication.game.screens.GameInstructionsScreen
import cat.copernic.aguamap1.aplication.game.screens.GamePlayScreen
import cat.copernic.aguamap1.aplication.game.screens.GameResultScreen
import cat.copernic.aguamap1.aplication.utils.PermissionRequestUI
import cat.copernic.aguamap1.ui.theme.Blanco
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.osmdroid.util.GeoPoint

/**
 * Orquestador principal del módulo de Juego.
 * Gestiona el ciclo de vida de la partida, los permisos de ubicación en tiempo real
 * y la navegación interna entre los diferentes estados del juego (Instrucciones -> Jugando -> Resultados).
 *
 * @param viewModel ViewModel encargado de la lógica de puntuación y estados.
 * @param userLat Latitud inicial proporcionada por el flujo de navegación.
 * @param userLng Longitud inicial proporcionada por el flujo de navegación.
 * @param onBackToHome Callback para cerrar el juego y volver al mapa principal.
 * @param onFountainClick Navegación al detalle de una fuente específica.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    userLat: Double,
    userLng: Double,
    onBackToHome: () -> Unit,
    onFountainClick: (Fountain) -> Unit
) {
    // Suscripción a los flujos de estado del ViewModel
    val gameState by viewModel.gameState.collectAsState()
    val currentFountain by viewModel.currentFountain.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val score by viewModel.score.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val userGuessPos by viewModel.userGuessPos.collectAsState()
    val hasLost by viewModel.hasLost.collectAsState()
    val distanceToFountain by viewModel.distanceToFountain.collectAsState()

    val context = LocalContext.current

    /**
     * GESTIÓN DE UBICACIÓN:
     * El juego requiere saber dónde está el usuario para seleccionar fuentes cercanas.
     * Prioriza las coordenadas del NavGraph, pero tiene un fallback al GPS local.
     */
    var currentUserLat by remember { mutableDoubleStateOf(userLat) }
    var currentUserLng by remember { mutableDoubleStateOf(userLng) }

    // Bandera para asegurar que no iniciamos la lógica sin coordenadas válidas
    var isLocationReady by remember {
        mutableStateOf(userLat != 0.0 && String.format("%.4f", userLat) != "41.5632")
    }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Sincronización de ubicación desde el grafo de navegación
    LaunchedEffect(userLat, userLng) {
        if (userLat != 0.0 && String.format("%.4f", userLat) != "41.5632") {
            currentUserLat = userLat
            currentUserLng = userLng
            isLocationReady = true
        }
    }

    // Lógica de recuperación (Fallback) si los permisos se conceden en caliente
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted && !isLocationReady) {
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
                Log.e("GAME_DEBUG", "Error de permisos", e)
            }
        }
    }

    /**
     * DISPARADOR DE LÓGICA DE NEGOCIO:
     * Una vez tenemos ubicación, comprobamos si el usuario puede jugar (límites diarios y proximidad).
     */
    LaunchedEffect(isLocationReady) {
        if (isLocationReady && gameState == GameViewModel.GameState.Initial) {
            viewModel.checkCanPlay(currentUserLat, currentUserLng)
        }
    }

    // Limpieza al salir de la pantalla
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
            /**
             * Estado: Bloqueo por Permisos.
             */
            PermissionRequestUI {
                locationPermissionState.launchPermissionRequest()
            }
        } else {
            /**
             * MÁQUINA DE ESTADOS DE LA UI:
             * Renderiza la pantalla correspondiente según el flujo del juego.
             */
            when (gameState) {
                GameViewModel.GameState.Initial -> {
                    LoadingPartida()
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
                                GeoPoint(
                                    pos.first,
                                    pos.second
                                )
                            },
                            hasLost = hasLost,
                            onBackToHome = {
                                viewModel.onBackToHomePressed()
                                onBackToHome()
                            },
                            onFountainClick = { f -> onFountainClick(f) }
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

                GameViewModel.GameState.TooFar -> {
                    ErrorScreen(
                        message = "${stringResource(R.string.game_error_too_far)} (Dist: ${distanceToFountain.toInt()}m)",
                        onRetry = {
                            if (isLocationReady) viewModel.retryGame(currentUserLat, currentUserLng)
                        },
                        onBack = {
                            viewModel.onBackToHomePressed()
                            onBackToHome()
                        }
                    )
                }

                else -> ErrorScreen(
                    message = error ?: stringResource(R.string.game_error_unexpected),
                    onRetry = {
                        if (isLocationReady) viewModel.retryGame(currentUserLat, currentUserLng)
                    },
                    onBack = {
                        viewModel.onBackToHomePressed()
                        onBackToHome()
                    }
                )
            }

            /**
             * Overlay de carga para procesos en segundo plano (ej: guardando puntuación en Firebase).
             */
            if (isLoading && gameState != GameViewModel.GameState.Initial) {
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