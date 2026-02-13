package cat.copernic.aguamap1.presentation.util

import androidx.compose.ui.graphics.toArgb
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Rojo
import cat.copernic.aguamap1.ui.theme.Verde

fun Fountain.getMarkerColor(
): Int {
    return when {
        !isOperational -> Rojo.toArgb()
        status == "PENDING" -> Naranja.toArgb()
        category == "Bebible" -> Blue10.toArgb()
        else -> Verde.toArgb()
    }
}