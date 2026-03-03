package cat.copernic.aguamap1.presentation.game

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
import cat.copernic.aguamap1.domain.model.Fountain
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
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    userLat: Double,
    userLng: Double,
    onBackToHome: () -> Unit,
    onFountainClick: (Fountain) -> Unit
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
    val distanceToFountain by viewModel.distanceToFountain.collectAsState()

    val context = LocalContext.current

    // PRIORIDAD: Usamos las coordenadas que vienen del NavGraph (MapViewModel)
    var currentUserLat by remember { mutableDoubleStateOf(userLat) }
    var currentUserLng by remember { mutableDoubleStateOf(userLng) }

    // Bandera para saber si ya tenemos una ubicación válida
    var isLocationReady by remember {
        mutableStateOf(userLat != 0.0 && String.format("%.4f", userLat) != "41.5632")
    }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // CORRECCIÓN: Si cambian las coordenadas en el NavGraph, actualizamos aquí
    LaunchedEffect(userLat, userLng) {
        if (userLat != 0.0 && String.format("%.4f", userLat) != "41.5632") {
            currentUserLat = userLat
            currentUserLng = userLng
            isLocationReady = true
            Log.d("GAME_DEBUG", "Ubicación sincronizada desde Nav: $userLat, $userLng")
        }
    }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted && !isLocationReady) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                // Solo pedimos al GPS si el NavGraph no nos dio nada útil
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                lastLocation?.let {
                    currentUserLat = it.latitude
                    currentUserLng = it.longitude
                    isLocationReady = true
                    Log.d("GAME_DEBUG", "Ubicación fallback GPS local: ${it.latitude}, ${it.longitude}")
                }
            } catch (e: SecurityException) {
                Log.e("GAME_DEBUG", "Error de permisos", e)
            }
        }
    }

    // Disparar lógica de juego
    LaunchedEffect(isLocationReady) {
        if (isLocationReady && gameState == GameViewModel.GameState.Initial) {
            Log.d("GAME_DEBUG", "Iniciando checkCanPlay con: $currentUserLat, $currentUserLng")
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
                locationPermissionState.launchPermissionRequest()
            }
        } else {
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
                            userLocation = if (isLocationReady) GeoPoint(currentUserLat, currentUserLng) else null,
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
                            userGuessPos = userGuessPos?.let { pos -> GeoPoint(pos.first, pos.second) },
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