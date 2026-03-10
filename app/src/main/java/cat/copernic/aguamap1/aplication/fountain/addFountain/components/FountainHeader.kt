package cat.copernic.aguamap1.aplication.fountain.addFountain.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.Negro

/**
 * Componente de cabecera simplificado para el formulario de añadir fuente.
 * * @param onDismiss Callback para cerrar el formulario o diálogo actual.
 */
@Composable
fun FountainHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.category_close),
                tint = Negro
            )
        }
    }
}