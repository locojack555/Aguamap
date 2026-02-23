package cat.copernic.aguamap1.presentation.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.StateFountain
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Rojo
import cat.copernic.aguamap1.ui.theme.Verde

fun Fountain.getMarkerColor(
): Int {
    return when {
        !operational -> Rojo.toArgb()
        status == StateFountain.PENDING -> Naranja.toArgb()
        category.name.equals("Bebible", ignoreCase = true) -> Verde.toArgb()
        else -> Blue10.toArgb()
    }
}

fun Fountain.getStatusColor(): Color {
    return when {
        !operational -> Rojo
        status == StateFountain.PENDING -> Naranja
        category.name.equals("Bebible", ignoreCase = true) -> Verde
        else -> Blue10
    }
}

fun Fountain.getStatusText(): String {
    return when {
        !operational -> "Averiada - ${category.name}"
        status == StateFountain.PENDING -> "Pendiente de revisión"
        else -> category.name
    }
}