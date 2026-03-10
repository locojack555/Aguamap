package cat.copernic.aguamap1.ui.map

import cat.copernic.aguamap1.domain.model.fountain.Fountain

/**
 * Representa el estado inmutable de la pantalla del Mapa en un momento dado.
 * * Sigue el patrón de Flujo de Datos Unidireccional (UDF), permitiendo que la
 * Vista (Compose) se renderice simplemente observando este objeto.
 *
 * @property fountains Lista de fuentes disponibles para mostrar como marcadores en el mapa.
 * @property isLoading Indica si hay una operación de red o base de datos en curso (para mostrar Spinners).
 * @property errorMessage Contiene el mensaje detallado en caso de que alguna operación falle.
 */
data class MapUiState(
    val fountains: List<Fountain> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)