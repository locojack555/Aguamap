package cat.copernic.aguamap1.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.Rojo

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
        // Icono de error
        Icon(
            painter = painterResource(R.drawable.error_24px), // Asegúrate de que el ID sea correcto
            contentDescription = stringResource(R.string.error_icon_desc), // Multiidioma
            modifier = Modifier.size(64.dp),
            tint = Rojo
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mensaje de error (ya viene como String, probablemente del ViewModel con stringResource)
        Text(
            text = message,
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            color = Negro // O el color que uses para texto principal
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de Reintentar
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
                    text = stringResource(R.string.game_error_retry_button), // Multiidioma
                    color = Blanco,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón de Volver (Opcional, pero lo tienes en los parámetros)
        TextButton(onClick = onBack) {
            Text(
                text = stringResource(R.string.back), // Multiidioma
                color = Blue10, // O Blue10
                fontWeight = FontWeight.Medium
            )
        }
    }
}