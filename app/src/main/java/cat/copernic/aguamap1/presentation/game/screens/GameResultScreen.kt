package cat.copernic.aguamap1.presentation.game.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.presentation.game.components.GameMapView
import cat.copernic.aguamap1.ui.theme.*
import org.osmdroid.util.GeoPoint

@Composable
fun GameResultScreen(
    fountain: Fountain,
    score: Int,
    distance: Double,
    userGuessPos: GeoPoint?,
    hasLost: Boolean,
    onBackToHome: () -> Unit,
    onFountainClick: (Fountain) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(Modifier.fillMaxSize()) {
        // Mapa con los marcadores
        Box(modifier = Modifier.weight(0.4f)) {
            GameMapView(
                fountain = fountain,
                userLocation = null,
                isFinished = true,
                userGuessPos = if (hasLost) null else userGuessPos,
                onMarkerPlaced = { _, _ -> },
                distance = if (hasLost) 0.0 else distance,
                onFountainClick = onFountainClick
            )
        }

        // Tarjeta con la puntuación
        Card(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (hasLost) {
                        // --- ESTADO: DERROTA ---
                        Surface(
                            color = Rojo.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.error_24px),
                                    contentDescription = null,
                                    tint = Rojo,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.game_result_lost_title),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Rojo
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.game_result_lost_message),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            color = BlancoClaro,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.game_result_lost_points),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Rojo
                            )
                        }

                    } else {
                        // --- ESTADO: VICTORIA ---
                        Surface(
                            color = Verde.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Verde,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.game_result_won_title),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Verde
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.game_result_your_score),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        // Uso de argumento dinámico para puntos
                        Text(
                            text = stringResource(R.string.game_result_points_format, score),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Blue10
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = Blanco,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            // Uso de argumento dinámico para distancia formateada
                            val formattedDistance = if (distance >= 1000) {
                                String.format("%.1f km", distance / 1000.0)
                            } else {
                                "${distance.toInt()} m"
                            }

                            Text(
                                text = stringResource(R.string.game_result_distance_format, formattedDistance),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Blue10
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Texto común sobre el marcador
                    Text(
                        text = stringResource(R.string.game_result_click_red_marker),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        color = AzulClaro,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.game_result_daily_message),
                            fontWeight = FontWeight.Bold,
                            color = AzulGrisaceo,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}