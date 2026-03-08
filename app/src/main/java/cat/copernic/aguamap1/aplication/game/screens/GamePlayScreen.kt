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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.aplication.game.components.GameMapView
import cat.copernic.aguamap1.aplication.utils.VerdeHoja
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Rojo
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.osmdroid.util.GeoPoint

/**
 * Pantalla activa de juego donde el usuario interactúa con el mapa para adivinar la posición.
 * Orquesta la visualización del mapa interactivo, el cronómetro de cuenta atrás y
 * la pista visual (imagen de la fuente).
 *
 * @param fountain La fuente objetivo que el usuario debe localizar.
 * @param remainingTime Segundos restantes antes de que la partida finalice automáticamente.
 * @param userLocation Ubicación actual para centrar el mapa al inicio.
 * @param onGuess Callback invocado cada vez que el usuario coloca o mueve el marcador.
 * @param onFinish Acción para confirmar la selección y pasar a la pantalla de resultados.
 */
@Composable
fun GamePlayScreen(
    fountain: Fountain,
    remainingTime: Int,
    userLocation: GeoPoint?,
    onGuess: (Double, Double) -> Unit,
    onFinish: () -> Unit
) {
    /**
     * Estado local para controlar si el usuario ya ha interactuado con el mapa.
     * Permite alternar entre el texto de ayuda y el botón de confirmación.
     */
    var hasPlacedPin by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {

        /**
         * 1. CAPA INFERIOR: Mapa Interactivo.
         * Se configura en modo "no finalizado" para permitir la colocación de marcadores.
         */
        GameMapView(
            fountain = fountain,
            userLocation = userLocation,
            isFinished = false,
            onMarkerPlaced = { lat, lng ->
                hasPlacedPin = true
                onGuess(lat, lng)
            }
        )

        /**
         * 2. CAPA SUPERIOR: Panel de Información.
         * Contiene el cronómetro y la pista visual (Imagen o Nombre).
         */
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            color = Color.White.copy(alpha = 0.95f),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Column(Modifier.padding(12.dp)) {
                // Fila de Estado: Tiempo y Título
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Text(
                        text = "${remainingTime}${stringResource(R.string.unit_seconds_short)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingTime < 10) Rojo else Color.Black
                    )
                    Text(
                        text = stringResource(R.string.game_instructions_title_app),
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Icon(
                        painter = painterResource(R.drawable.timer_24px),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Sección de Pista Visual
                if (fountain.imageUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.game_play_question),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(fountain.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = fountain.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    // Placeholder si la fuente no tiene imagen
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        color = Color.LightGray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.pin_lleno),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = fountain.name,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        /**
         * 3. CAPA INFERIOR: Acciones del Jugador.
         * Si ha puesto un pin, se muestra el botón de confirmar.
         * Si no, se muestra un mensaje instructivo flotante.
         */
        if (hasPlacedPin) {
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .fillMaxWidth(0.8f)
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeHoja)
            ) {
                Text(
                    text = stringResource(R.string.game_play_confirm_button),
                    color = Blanco,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        } else {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.game_play_hint_text),
                    color = Blanco,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    fontSize = 16.sp
                )
            }
        }
    }
}