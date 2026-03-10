package cat.copernic.aguamap1.domain.repository.ranking

import cat.copernic.aguamap1.domain.model.ranking.RankingPeriod
import cat.copernic.aguamap1.domain.model.ranking.UserRanking

/**
 * Interfaz de Dominio que define la lógica de consulta para las clasificaciones.
 * Proporciona los métodos necesarios para visualizar el rendimiento de los usuarios
 * en diferentes ventanas temporales (diario, mensual, anual e histórico).
 */
interface RankingRepository {

    /**
     * Obtiene la lista de los mejores jugadores del día actual.
     * Los datos se calculan sumando las sesiones de juego realizadas desde las 00:00h.
     * @return Lista de [cat.copernic.aguamap1.domain.model.ranking.UserRanking] ordenada por puntuación descendente.
     */
    suspend fun getDailyRanking(): List<UserRanking>

    /**
     * Obtiene la clasificación acumulada del mes en curso.
     * Utiliza documentos pre-calculados para optimizar el rendimiento de la consulta.
     * @return Lista de [UserRanking] con el Top 10 mensual.
     */
    suspend fun getMonthlyRanking(): List<UserRanking>

    /**
     * Obtiene la clasificación total del año actual.
     * Agrega los resultados de todos los meses finalizados y el mes en curso para cada usuario.
     * @return Lista de [UserRanking] ordenada por éxito anual.
     */
    suspend fun getYearlyRanking(): List<UserRanking>

    /**
     * Método de conveniencia para obtener un ranking basado en un periodo dinámico.
     * @param period El periodo seleccionado (DAY, MONTH, YEAR).
     * @return Lista de ranking correspondiente al [cat.copernic.aguamap1.domain.model.ranking.RankingPeriod] solicitado.
     */
    suspend fun getRankingByPeriod(period: RankingPeriod): List<UserRanking>

    /**
     * Recupera el resumen de toda la actividad histórica de un usuario específico.
     * Se utiliza para mostrar las medallas y estadísticas totales en el perfil personal.
     * @param userId Identificador del usuario a consultar.
     * @return Objeto [UserRanking] con totales históricos o null si el usuario no tiene registros.
     */
    suspend fun getCurrentUserHistoricRanking(userId: String): UserRanking?
}