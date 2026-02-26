package cat.copernic.aguamap1.presentation.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.StateFountain
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Rojo
import kotlin.random.Random

/**
 * Genera un color único para una categoría basado en su ID.
 */
fun getRandomCategoryColor(categoryId: String): Color {
    val seed = categoryId.hashCode()
    val random = Random(seed)
    // Usamos rangos para evitar colores demasiado blancos o negros
    return Color(
        red = random.nextFloat().coerceIn(0.2f, 0.8f),
        green = random.nextFloat().coerceIn(0.2f, 0.8f),
        blue = random.nextFloat().coerceIn(0.2f, 0.8f),
        alpha = 1f
    )
}

/**
 * Lógica de prioridad de colores para el Marcador (Int ARGB).
 * Prioridad: Pendiente (Naranja) > Averiada (Rojo) > Categoría (Aleatorio)
 */
fun Fountain.getMarkerColor(): Int {
    return when {
        status == StateFountain.PENDING -> Naranja.toArgb()
        !operational -> Rojo.toArgb()
        else -> getRandomCategoryColor(this.category.id).toArgb()
    }
}

/**
 * Lógica de prioridad de colores para la UI de Compose (Color).
 */
fun Fountain.getStatusColor(): Color {
    return when {
        status == StateFountain.PENDING -> Naranja
        !operational -> Rojo
        else -> getRandomCategoryColor(this.category.id)
    }
}
fun Fountain.getStatusText(): String {
    return when {
        !operational -> "Averiada - ${category.name}"
        status == StateFountain.PENDING -> "Pendiente de revisión"
        else -> category.name
    }
}