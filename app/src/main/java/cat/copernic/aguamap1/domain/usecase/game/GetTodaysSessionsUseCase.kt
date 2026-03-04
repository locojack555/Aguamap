package cat.copernic.aguamap1.domain.usecase.game

import cat.copernic.aguamap1.domain.model.GameSession
import cat.copernic.aguamap1.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de Uso encargado de recuperar todas las sesiones de juego completadas por el usuario
 * durante el día en curso.
 * * Se utiliza para mostrar el progreso diario, el total de puntos acumulados en la jornada
 * y para limitar o gestionar el acceso a nuevas partidas si existiera un cupo diario.
 *
 * @property repository Repositorio de juego que filtra las sesiones por fecha y usuario.
 */
class GetTodaysSessionsUseCase @Inject constructor(
    private val repository: GameRepository
) {
    /**
     * Obtiene un flujo reactivo de las sesiones de hoy.
     * * Al ser un [Flow], cualquier nueva partida guardada se emitirá automáticamente
     * hacia los colectores de la UI (ViewModels).
     * * @return [Flow] que emite una lista de [GameSession] filtrada por la fecha actual.
     */
    operator fun invoke(): Flow<Result<List<GameSession>>> {
        return repository.getTodaysSessions()
    }
}