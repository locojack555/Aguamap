package cat.copernic.aguamap1.presentation.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.StateFountain
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Rojo
import kotlin.random.Random

// 1. DEFINIR TUS COLORES PERSONALIZADOS
val VerdeHoja = Color(0xFF4CAF50)
val AzulAgua = Color(0xFF2196F3)

/**
 * 2. CAMBIA ESTO PARA TUS CATEGORÍAS ACTUALES
 * Sustituye "NombreDeTuCategoria1" por el nombre exacto de tu categoría
 */
private val categoryColorMap = mapOf(
    "Ornamental" to VerdeHoja, // Cámbialo por el nombre real que pusiste
    "Potable" to AzulAgua    // Cámbialo por el nombre real que pusiste
)

/**
 * Obtiene el color de la categoría. Si no coincide con el mapa, genera uno aleatorio.
 */
fun getCategoryColor(categoryName: String, categoryId: String): Color {
    // Busca por nombre en el mapa de arriba
    return categoryColorMap[categoryName] ?: run {
        // Fallback: Si no existe el nombre, genera uno aleatorio suave
        val seed = categoryId.hashCode()
        val random = Random(seed)
        Color(
            red = random.nextFloat().coerceIn(0.3f, 0.7f),
            green = random.nextFloat().coerceIn(0.3f, 0.7f),
            blue = random.nextFloat().coerceIn(0.3f, 0.7f),
            alpha = 1f
        )
    }
}

/**
 * Color para el Marcador del Mapa (Int ARGB).
 */
fun Fountain.getMarkerColor(): Int {
    return when {
        status == StateFountain.PENDING -> Naranja.toArgb()
        !operational -> Rojo.toArgb()
        else -> getCategoryColor(this.category.name, this.category.id).toArgb()
    }
}

/**
 * Color para la UI de Compose (Color).
 */
fun Fountain.getStatusColor(): Color {
    return when {
        status == StateFountain.PENDING -> Naranja
        !operational -> Rojo
        else -> getCategoryColor(this.category.name, this.category.id)
    }
}

/**
 * Texto de estado.
 */
fun Fountain.getStatusText(): String {
    return when {
        !operational -> "Averiada - ${category.name}"
        status == StateFountain.PENDING -> "Pendiente de revisión"
        else -> category.name
    }
}