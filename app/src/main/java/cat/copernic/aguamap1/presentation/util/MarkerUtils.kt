package cat.copernic.aguamap1.presentation.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Rojo
import cat.copernic.aguamap1.ui.theme.Verde

fun Fountain.getMarkerColor(
): Int {
    return when {
        !operational -> Rojo.toArgb()
        status == "PENDING" -> Naranja.toArgb()
        category == "BEBIBLE" -> Verde.toArgb()
        else -> Blue10.toArgb()
    }
}

fun Fountain.getStatusColor(): Color {
    return when {
        !operational -> Rojo
        status == "PENDING" -> Naranja
        category == "BEBIBLE" -> Verde
        else -> Blue10
    }
}

fun Fountain.getStatusText(): String {
    return when {
        !operational -> "Averiada - $category"
        status == "PENDING" -> "Pendiente de revisión"
        else -> category
    }
}