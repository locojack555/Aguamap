package cat.copernic.aguamap1.domain.model

/**
 * Representa la entrada de un usuario en una tabla de clasificación (Ranking).
 * Este modelo se utiliza para visualizar el rendimiento comparativo entre jugadores
 * en diferentes periodos de tiempo.
 *
 * @property position Lugar que ocupa el usuario en la lista (1º, 2º, 3º, etc.).
 * @property name Nombre para mostrar del jugador.
 * @property points Puntuación acumulada en el periodo seleccionado (Día, Mes, Año o Histórico).
 * @property discovered Cantidad de fuentes únicas que el usuario ha encontrado con éxito en el juego.
 * @property games Número total de partidas jugadas por el usuario en dicho periodo.
 * @property isCurrentUser Flag booleano para resaltar la fila del usuario que consulta el ranking en la interfaz.
 */
data class UserRanking(
    val position: Int = 0,
    val name: String = "",
    val points: Int = 0,
    val discovered: Int = 0,
    val games: Int = 0,
    val isCurrentUser: Boolean = false
)