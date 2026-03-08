package cat.copernic.aguamap1.aplication.game.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.aguamap1.R

/**
 * Componente de estado de carga específico para el inicio de una partida.
 * Se utiliza mientras el sistema selecciona una fuente aleatoria y descarga
 * los recursos necesarios (imágenes, coordenadas) para el modo de juego.
 * * Presenta un indicador de progreso circular y un mensaje informativo
 * centrado en pantalla.
 */
@Composable
fun LoadingPartida() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            /**
             * Indicador visual de progreso con el color azul corporativo.
             */
            CircularProgressIndicator(color = Color(0xFF007BFF))

            Spacer(Modifier.height(16.dp))

            /**
             * Texto informativo multiidioma que indica al usuario que la
             * sesión de juego se está preparando.
             */
            Text(
                text = stringResource(R.string.game_loading_message),
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }
    }
}