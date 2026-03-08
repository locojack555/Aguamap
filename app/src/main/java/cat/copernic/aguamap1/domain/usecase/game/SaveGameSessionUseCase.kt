package cat.copernic.aguamap1.domain.usecase.game

import cat.copernic.aguamap1.domain.model.game.GameSession
import cat.copernic.aguamap1.domain.repository.game.GameRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de persistir los resultados de una partida finalizada.
 * Registra la puntuación obtenida, la fuente localizada y la marca de tiempo
 * para su posterior consulta en el historial y rankings.
 *
 * @property repository Repositorio de juego que gestiona la inserción en la colección de sesiones.
 */
class SaveGameSessionUseCase @Inject constructor(
    private val repository: GameRepository
) {
    /**
     * Ejecuta el guardado de la sesión de juego.
     *
     * @param session El objeto [GameSession] con toda la información de la partida (puntos, UID, fecha).
     * @return [Result] que confirma si la sesión se ha guardado correctamente en Firestore.
     */
    suspend operator fun invoke(session: GameSession): Result<Unit> {
        return repository.saveGameSession(session)
    }
}