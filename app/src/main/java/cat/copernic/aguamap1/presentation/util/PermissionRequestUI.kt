package cat.copernic.aguamap1.presentation.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.NegroSuave

/**
 * Interfaz de usuario informativa para solicitar permisos de ubicación.
 * Se muestra cuando la aplicación no tiene los permisos necesarios para centrar el mapa
 * o calcular distancias a las fuentes.
 *
 * @param onPermissionRequest Callback que dispara el sistema de solicitud de permisos de Android.
 */
@Composable
fun PermissionRequestUI(onPermissionRequest: () -> Unit) {
    // Usamos Surface para forzar el fondo blanco y elevar el contenido visualmente
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Blanco
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ICONO VISUAL: Refuerza la idea de localización
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = Blue10
            )

            Spacer(modifier = Modifier.height(32.dp))


            // TÍTULO: Directo y con peso visual
            Text(
                text = stringResource(R.string.ubi_on),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Negro,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // EXPLICACIÓN: Justifica por qué AguaMap necesita la ubicación (UX de confianza)
            Text(
                text = stringResource(R.string.txt_permiss),
                fontSize = 16.sp,
                color = NegroSuave,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(56.dp))

            // BOTÓN DE ACCIÓN: Dispara el launcher de permisos del sistema
            Button(
                onClick = onPermissionRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(R.string.permiss_on),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blanco
                )
            }
        }
    }
}