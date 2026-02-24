package cat.copernic.aguamap1.presentation.game

import android.Manifest
import android.content.Context
import android.location.LocationManager
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
    val userLocation by viewModel.userLocation.collectAsState()

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
                // Intentamos obtener la última ubicación conocida de forma rápida
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

    // 2. INICIAR JUEGO: Solo cuando el permiso es concedido Y la ubicación está lista
    // Evitamos que se ejecute checkCanPlay si no hay coordenadas, evitando errores de "partida gastada"
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
                locationPermissionState.launchPermissionRequest()
            }
        } else {
            when (gameState) {
                GameViewModel.GameState.Initial -> {
                    if (!locationPermissionState.status.isGranted) {
                        PermissionRequestUI {
                            // Si ya se denegó antes y Android dice que no debemos mostrar el diálogo nativo,
                            // significa que el usuario marcó "No volver a preguntar" o denegó varias veces.
                            // En ese caso, lo enviamos a Ajustes.
                            if (locationPermissionState.status.shouldShowRationale) {
                                locationPermissionState.launchPermissionRequest()
                            } else {
                                locationPermissionState.launchPermissionRequest()
                                // Nota: Si launch no hace nada, es que está bloqueado.
                                // Podrías añadir un Intent a los ajustes aquí.
                            }
                        }
                    } else if (!isLocationReady) {
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
                                GeoPoint(
                                    pos.first,
                                    pos.second
                                )
                            },
                            hasLost = hasLost,
                            onBackToHome = {
                                viewModel.onBackToHomePressed()
                                onBackToHome()
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