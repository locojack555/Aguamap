package cat.copernic.aguamap1.domain.usecase.game

import cat.copernic.aguamap1.domain.repository.GameRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de verificar si un usuario ya ha participado en el reto diario.
 * Esta validación es clave para el control de acceso a las recompensas y para evitar
 * el abuso de puntos en los rankings globales.
 *
 * @property repository Repositorio de juego que consulta el historial de sesiones en Firestore.
 */
class HasPlayedTodayUseCase @Inject constructor(
    private val repository: GameRepository
) {
    /**
     * Comprueba la existencia de sesiones registradas para el usuario en el día actual.
     *
     * @param userId El identificador único del usuario (UID) a consultar.
     * @return [Result] que contiene un [Boolean]: true si ya ha jugado, false en caso contrario.
     */
    suspend operator fun invoke(userId: String): Result<Boolean> {
        return repository.hasPlayedToday(userId)
    }
}