package cat.copernic.aguamap1.presentation.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // IMPORTANTE: Importar hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.language.LanguageViewModel
import cat.copernic.aguamap1.ui.theme.AguaMapGradient
import cat.copernic.aguamap1.ui.theme.Blanco

/**
 * Selector de idioma flotante reutilizable para AguaMap.
 * Permite cambiar el idioma de la aplicación en tiempo real mediante un menú desplegable.
 * Generalmente se ubica en la esquina superior derecha de las pantallas de bienvenida.
 */
@Composable
fun AguaMapLanguage(
    modifier: Modifier = Modifier,
    viewModel: LanguageViewModel = hiltViewModel() // CORRECCIÓN: Usar hiltViewModel() en lugar de viewModel()
) {
    // Estado para controlar si el menú desplegable está abierto
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Botón de activación con el icono de globo/idioma
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(id = R.drawable.language_24px),
                contentDescription = stringResource(R.string.select_language),
                tint = Blanco,
                modifier = Modifier.size(40.dp)
            )
        }


        // Menú desplegable con diseño personalizado usando el gradiente de la App
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.Transparent, // Hacemos el contenedor transparente para aplicar el gradiente
            modifier = Modifier.background(AguaMapGradient)
        ) {
            // Opción: Español
            DropdownMenuItem(
                text = { Text(stringResource(R.string.es), color = Blanco) },
                onClick = {
                    viewModel.onChangeLanguage("es")
                    expanded = false
                }
            )
            // Opción: Inglés
            DropdownMenuItem(
                text = { Text(stringResource(R.string.en), color = Blanco) },
                onClick = {
                    viewModel.onChangeLanguage("en")
                    expanded = false
                }
            )
            // Opción: Catalán
            DropdownMenuItem(
                text = { Text(stringResource(R.string.ca), color = Blanco) },
                onClick = {
                    viewModel.onChangeLanguage("ca")
                    expanded = false
                }
            )
        }
    }
}