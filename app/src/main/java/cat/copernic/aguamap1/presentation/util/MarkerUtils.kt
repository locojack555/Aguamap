package cat.copernic.aguamap1.presentation.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.StateFountain
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Rojo
import kotlin.random.Random

// 1. DEFINICIÓN DE COLORES TEMÁTICOS POR CATEGORÍA
val VerdeHoja = Color(0xFF4CAF50)
val AzulAgua = Color(0xFF2196F3)

/**
 * Mapa que vincula los nombres de las categorías con sus colores representativos.
 * Asegúrate de que los nombres coincidan con los definidos en tu base de datos Firestore.
 */
private val categoryColorMap = mapOf(
    "Ornamental" to VerdeHoja,
    "Potable" to AzulAgua
)

/**
 * Determina el color asociado a una categoría.
 * Si la categoría no está mapeada, genera un color determinista basado en el ID
 * para que la misma categoría siempre tenga el mismo color, pero evitando tonos muy oscuros o claros.
 */
fun getCategoryColor(categoryName: String, categoryId: String): Color {
    return categoryColorMap[categoryName] ?: run {
        // Generador de color aleatorio basado en el hash del ID para consistencia visual
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
 * Obtiene el color del marcador para Google Maps.
 * Devuelve un Int ARGB, que es el formato requerido por BitmapDescriptorFactory.
 * * Prioridad de color:
 * 1. Naranja si está pendiente de validación.
 * 2. Rojo si la fuente no está operativa (averiada).
 * 3. Color de categoría si todo es correcto.
 */
fun Fountain.getMarkerColor(): Int {
    return when {
        status == StateFountain.PENDING -> Naranja.toArgb()
        !operational -> Rojo.toArgb()
        else -> getCategoryColor(this.category.name, this.category.id).toArgb()
    }
}

/**
 * Obtiene el color de estado para componentes de Compose (Chips, Iconos, Textos).
 * Devuelve un objeto Color de Compose.
 */
fun Fountain.getStatusColor(): Color {
    return when {
        status == StateFountain.PENDING -> Naranja
        !operational -> Rojo
        else -> getCategoryColor(this.category.name, this.category.id)
    }
}

/**
 * Genera el texto descriptivo del estado actual de la fuente.
 * Útil para etiquetas de información y BottomSheets.
 */
fun Fountain.getStatusText(): String {
    return when {
        !operational -> "Averiada - ${category.name}"
        status == StateFountain.PENDING -> "Pendiente de revisión"
        else -> category.name
    }
}