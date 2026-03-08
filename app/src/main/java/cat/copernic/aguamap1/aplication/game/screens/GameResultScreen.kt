package cat.copernic.aguamap1.aplication.game.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.aplication.game.components.GameMapView
import cat.copernic.aguamap1.ui.theme.AzulClaro
import cat.copernic.aguamap1.ui.theme.AzulGrisaceo
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.BlancoClaro
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Rojo
import cat.copernic.aguamap1.ui.theme.Verde
import org.osmdroid.util.GeoPoint

/**
 * Pantalla de resultados del juego.
 * Muestra el desenlace de la partida con una comparativa visual en el mapa y
 * el desglose de la puntuación obtenida.
 *
 * @param fountain La fuente objetivo.
 * @param score Puntos totales calculados.
 * @param distance Distancia final en metros entre el usuario y el objetivo.
 * @param userGuessPos Coordenadas marcadas por el usuario.
 * @param hasLost Define si el usuario perdió (por tiempo o distancia excesiva).
 * @param onBackToHome Callback para regresar al menú principal.
 * @param onFountainClick Permite navegar al detalle de la fuente desde el mapa de resultados.
 */
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
        /**
         * 1. SECCIÓN DE MAPA (40% de la pantalla)
         * Muestra la ubicación real frente a la estimada con una línea de conexión.
         */
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

        /**
         * 2. TARJETA DE PUNTUACIÓN (60% de la pantalla)
         * Desglose detallado del éxito o fracaso de la partida.
         */
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
                        /**
                         * --- ESTADO: DERROTA ---
                         */
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
                        /**
                         * --- ESTADO: VICTORIA ---
                         */
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

                        // Puntuación destacada
                        Text(
                            text = stringResource(R.string.game_result_points_format, score),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Blue10
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Formateo dinámico de la distancia (m/km)
                        val formattedDistance = if (distance >= 1000) {
                            String.format("%.1f km", distance / 1000.0)
                        } else {
                            "${distance.toInt()} m"
                        }

                        Surface(
                            color = Blanco,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.game_result_distance_format,
                                    formattedDistance
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Blue10
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    /**
                     * Instrucción interactiva para explorar la fuente tras el juego.
                     */
                    Text(
                        text = stringResource(R.string.game_result_click_red_marker),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    /**
                     * Mensaje de persistencia: Fomenta el juego diario.
                     */
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