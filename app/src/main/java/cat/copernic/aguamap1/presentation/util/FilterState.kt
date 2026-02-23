package cat.copernic.aguamap1.presentation.util

import cat.copernic.aguamap1.domain.model.Category

data class FilterState(
    val selectedCategory: Category? = null,
    val onlyOperational: Boolean = false,
    val minRating: Float = 0f,
    val maxDistanceKm: Float = 10f,
    val sortBy: SortOption = SortOption.DISTANCE
)

enum class SortOption {
    DISTANCE, RATING, DATE
}