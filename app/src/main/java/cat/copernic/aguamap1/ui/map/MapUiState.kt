package cat.copernic.aguamap1.ui.map

import cat.copernic.aguamap1.domain.model.Fountain

data class MapUiState(
    val fountains: List<Fountain> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
