package cat.copernic.aguamap1.domain.usecase.ranking

import cat.copernic.aguamap1.domain.model.ranking.RankingPeriod
import cat.copernic.aguamap1.domain.model.ranking.UserRanking
import cat.copernic.aguamap1.domain.repository.ranking.RankingRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de recuperar la tabla de clasificación filtrada por temporalidad.
 * Permite segmentar el éxito de los usuarios en diferentes ventanas de tiempo,
 * fomentando la participación recurrente mediante rankings diarios o semanales.
 *
 * @property repository Repositorio de rankings que gestiona la agregación de puntos según el periodo.
 */
class GetRankingByPeriodUseCase @Inject constructor(
    private val repository: RankingRepository
) {
    /**
     * Obtiene la lista de los mejores jugadores para un periodo específico.
     * * @param period El intervalo de tiempo deseado (diario, semanal, mensual o histórico).
     * @return Una lista de objetos [UserRanking] ordenada por puntuación descendente.
     */
    suspend operator fun invoke(period: RankingPeriod): List<UserRanking> =
        repository.getRankingByPeriod(period)
}