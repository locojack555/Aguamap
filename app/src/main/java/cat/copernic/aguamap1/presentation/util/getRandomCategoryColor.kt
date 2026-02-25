package cat.copernic.aguamap1.presentation.util

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * Genera un color aleatorio pero consistente basado en un seed
 * Esto asegura que cada categoría siempre tenga el mismo color
 */
fun getRandomCategoryColor(seed: Int): Color {
    val random = Random(seed)
    return Color(
        red = random.nextFloat(),
        green = random.nextFloat(),
        blue = random.nextFloat(),
        alpha = 1f
    )
}

/**
 * Versión con colores predefinidos más agradables
 */
fun getPredefinedCategoryColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF2196F3), // Azul
        Color(0xFF4CAF50), // Verde
        Color(0xFF9C27B0), // Púrpura
        Color(0xFFFF9800), // Naranja
        Color(0xFFE91E63), // Rosa
        Color(0xFF00BCD4), // Cian
        Color(0xFF8BC34A), // Verde claro
        Color(0xFFFF5722), // Naranja oscuro
        Color(0xFF607D8B), // Azul grisáceo
        Color(0xFF795548)  // Marrón
    )
    return colors[index % colors.size]
}

fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}