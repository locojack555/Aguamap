package cat.copernic.aguamap1.domain.usecase.profile

import cat.copernic.aguamap1.domain.model.ranking.UserRanking
import cat.copernic.aguamap1.domain.repository.ranking.RankingRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de recuperar las estadísticas históricas acumuladas de un usuario.
 * Proporciona la información necesaria para la pantalla de Perfil, permitiendo al usuario
 * ver su puntuación total, nivel de participación y posición en la comunidad.
 *
 * @property repository El repositorio de rankings que consolida los datos de sesiones y perfiles.
 */
class GetCurrentUserHistoricStatsUseCase @Inject constructor(
    private val repository: RankingRepository
) {
    /**
     * Ejecuta la consulta para obtener el resumen de logros del usuario.
     *
     * @param userId El identificador único del usuario del cual queremos las estadísticas.
     * @return Un objeto [UserRanking] con los datos agregados, o null si el usuario
     * aún no tiene registros de actividad en el sistema.
     */
    suspend operator fun invoke(userId: String): UserRanking? {
        return repository.getCurrentUserHistoricRanking(userId)
    }
}