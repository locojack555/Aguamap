package cat.copernic.aguamap1.domain.model

/**
 * Representa el estado de la pantalla de clasificaciones (Ranking) en un momento dado.
 * Se utiliza en la capa de Presentación para gestionar la visualización de datos
 * y los estados de carga de forma reactiva.
 *
 * @property players Lista de usuarios posicionados en el ranking para el periodo seleccionado.
 * @property isLoading Indica si la aplicación está realizando una consulta a la base de datos (Firebase).
 */
data class RankingState(
    val players: List<UserRanking> = emptyList(),
    val isLoading: Boolean = false
)