package cat.copernic.aguamap1.aplication.utils

import cat.copernic.aguamap1.domain.model.category.Category

/**
 * Representa el estado actual de los filtros aplicados en la búsqueda de fuentes.
 * @param selectedCategory Categoría seleccionada (Natural, Urbana, etc.) o null para todas.
 * @param onlyOperational Si es true, solo muestra fuentes que no estén marcadas como "Fuera de servicio".
 * @param minRating Puntuación mínima de las reseñas (0.0 a 5.0).
 * @param maxDistanceKm Radio máximo de búsqueda en kilómetros desde la posición del usuario.
 * @param sortBy Criterio de ordenación de los resultados.
 */
data class FilterState(
    val selectedCategory: Category? = null,
    val onlyOperational: Boolean = false,
    val minRating: Float = 0f,
    val maxDistanceKm: Float = 10f,
    val sortBy: SortOption = SortOption.DISTANCE_ASC
)

/**
 * Opciones disponibles para ordenar los resultados de las fuentes en AguaMap.
 */
enum class SortOption {
    DISTANCE_ASC,    // Más cercanas primero
    DISTANCE_DESC,   // Más lejanas primero
    RATING_DESC,     // Mejor puntuadas primero
    RATING_ASC,      // Peor puntuadas primero
    DATE_DESC,       // Añadidas recientemente primero
    DATE_ASC         // Más antiguas primero
}