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
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.language.LanguageViewModel
import cat.copernic.aguamap1.ui.theme.AguaMapGradient
import cat.copernic.aguamap1.ui.theme.Blanco

@Composable
fun AguaMapLanguage(
    modifier: Modifier = Modifier,
    viewModel: LanguageViewModel = viewModel()
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(id = R.drawable.language_24px),
                contentDescription = "Seleccionar idioma",
                tint = Blanco,
                modifier = Modifier.size(40.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.Transparent,
            modifier = Modifier.background(AguaMapGradient)
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.es), color = Blanco) },
                onClick = {
                    viewModel.onChangeLanguage("es")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.en), color = Blanco) },
                onClick = {
                    viewModel.onChangeLanguage("en")
                    expanded = false
                }
            )
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