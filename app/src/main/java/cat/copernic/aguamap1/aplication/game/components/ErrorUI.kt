package cat.copernic.aguamap1.aplication.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import cat.copernic.aguamap1.ui.theme.AzulGradient
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.Rojo

/**
 * Pantalla de estado de error genérica para el módulo de juegos.
 * Proporciona una interfaz visual clara cuando ocurre un fallo en la carga de datos
 * o en la lógica del juego, permitiendo al usuario reintentar la acción.
 *
 * @param message Mensaje descriptivo del error (procedente del ViewModel o recursos).
 * @param onRetry Callback opcional para volver a ejecutar la lógica que falló.
 * @param onBack Callback para navegar hacia atrás y salir del estado de error.
 */
@Composable
fun ErrorScreen(
    message: String,
    onRetry: (() -> Unit)?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /**
         * Icono visual de alerta.
         */
        Icon(
            painter = painterResource(R.drawable.error_24px),
            contentDescription = stringResource(R.string.error_icon_desc),
            modifier = Modifier.size(64.dp),
            tint = Rojo
        )

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Cuerpo del mensaje de error.
         */
        Text(
            text = message,
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            color = Negro
        )

        Spacer(modifier = Modifier.height(24.dp))

        /**
         * Acción de recuperación: Botón de reintento con gradiente corporativo.
         * Solo se renderiza si se proporciona una función de reintento.
         */
        onRetry?.let {
            Button(
                onClick = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(AzulGradient, RoundedCornerShape(25.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Text(
                    text = stringResource(R.string.game_error_retry_button),
                    color = Blanco,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}