package cat.copernic.aguamap1.aplication.fountain.addFountain.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.aplication.fountain.addFountain.AddFountainViewModel
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.GrisClaro
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.NegroMinimal
import cat.copernic.aguamap1.ui.theme.Rojo
import java.util.Locale

/**
 * Sección del formulario encargada de la gestión de coordenadas geográficas.
 * Permite alternar entre la obtención automática mediante GPS o la introducción manual de datos.
 * * @param viewModel Instancia del ViewModel que controla la lógica de ubicación y validación.
 */
@Composable
fun FountainLocationSection(viewModel: AddFountainViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        /**
         * Título principal de la sección de ubicación.
         */
        Text(
            text = stringResource(R.string.location_title),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Negro
        )

        /**
         * Selector de fuente de datos (GPS vs Manual).
         */
        Surface(color = NegroMinimal, shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.location_source_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Negro
                    )
                    Text(
                        text = if (viewModel.useGpsLocation) stringResource(R.string.location_gps)
                        else stringResource(R.string.location_manual),
                        fontSize = 12.sp, color = if (viewModel.useGpsLocation) Blue10 else Naranja
                    )
                }
                Switch(
                    checked = viewModel.useGpsLocation,
                    onCheckedChange = { viewModel.toggleLocationSource() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Blanco,
                        checkedTrackColor = Blue10
                    )
                )
            }
        }

        /**
         * Visualización de coordenadas automáticas o campos de entrada manual.
         */
        if (viewModel.useGpsLocation) {
            Surface(color = GrisClaro.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.pin_lleno),
                        null,
                        tint = Blue10,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format(
                            Locale.US, "Lat: %.6f, Lng: %.6f",
                            viewModel.selectedLocationForNewFountain?.latitude ?: 0.0,
                            viewModel.selectedLocationForNewFountain?.longitude ?: 0.0
                        ),
                        fontSize = 14.sp, color = Negro
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CoordInput(
                    value = viewModel.manualLatitude,
                    onValueChange = { viewModel.updateManualLatitude(it) },
                    label = stringResource(R.string.latitude_label),
                    error = viewModel.latitudeError,
                    modifier = Modifier.weight(1f)
                )
                CoordInput(
                    value = viewModel.manualLongitude,
                    onValueChange = { viewModel.updateManualLongitude(it) },
                    label = stringResource(R.string.longitude_label),
                    error = viewModel.longitudeError,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Componente interno reutilizable para la entrada de coordenadas individuales.
 * Incluye gestión de errores visuales y formato de borde dinámico.
 * * @param value Valor actual del campo.
 * @param onValueChange Callback para la actualización del estado.
 * @param label Etiqueta flotante del campo.
 * @param error Mensaje de error a mostrar si la validación falla.
 * @param modifier Modificador de diseño.
 */
@Composable
private fun CoordInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) Rojo else Negro,
                unfocusedBorderColor = if (error != null) Rojo else Negro,
                focusedTextColor = Negro,
            )
        )
        if (error != null) {
            Text(
                text = error,
                color = Rojo,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }
    }
}