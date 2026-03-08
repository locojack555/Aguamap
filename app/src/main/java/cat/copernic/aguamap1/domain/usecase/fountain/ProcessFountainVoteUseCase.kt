package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.domain.model.fountain.StateFountain
import cat.copernic.aguamap1.domain.repository.fountain.FountainRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de gestionar la lógica de validación y reporte comunitario.
 * Permite que los usuarios voten sobre la veracidad de una fuente, activando
 * cambios de estado automáticos o eliminaciones basadas en el consenso.
 */
class ProcessFountainVoteUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    /**
     * Añade un voto positivo a una fuente. Si alcanza el umbral de 3 votos,
     * la fuente pasa automáticamente a estado ACCEPTED.
     * * @param fountain La fuente a validar.
     * @param userId ID del usuario que emite el voto.
     * @return [Result] con éxito o error si el usuario ya votó o la fuente ya es oficial.
     */
    suspend fun addPositiveVote(fountain: Fountain, userId: String): Result<Unit> {
        if (fountain.status == StateFountain.ACCEPTED) {
            return Result.failure(Exception("Esta fuente ya ha sido validada"))
        }

        if (fountain.votedByPositive.contains(userId)) {
            return Result.failure(Exception("Ya has validado esta fuente"))
        }

        val newVotes = fountain.positiveVotes + 1
        val newVoters = fountain.votedByPositive + userId

        val updates = mutableMapOf<String, Any>(
            "positiveVotes" to newVotes,
            "votedByPositive" to newVoters
        )

        // Umbral de validación automática
        if (newVotes >= 3) {
            updates["status"] = StateFountain.ACCEPTED.name
        }

        return repository.updateFountain(fountain.id, updates)
    }

    /**
     * Añade un voto negativo (reporte). Si alcanza los 3 votos negativos,
     * la fuente se elimina automáticamente del sistema.
     * * @param fountain La fuente reportada.
     * @param userId ID del usuario que reporta.
     */
    suspend fun addNegativeVote(fountain: Fountain, userId: String): Result<Unit> {
        if (fountain.votedByNegative.contains(userId)) {
            return Result.failure(Exception("Ya has reportado esta fuente"))
        }

        val newVotes = fountain.negativeVotes + 1
        val newVoters = fountain.votedByNegative + userId

        return if (newVotes >= 3) {
            // Consenso de eliminación alcanzado
            repository.deleteFountain(fountain.id)
        } else {
            repository.updateFountain(
                fountain.id, mapOf(
                    "negativeVotes" to newVotes,
                    "votedByNegative" to newVoters
                )
            )
        }
    }

    /**
     * Permite desmentir un reporte previo. Útil para corregir errores de usuarios
     * o confirmar que una fuente reportada como "inexistente" realmente sí está ahí.
     * * @param fountain La fuente cuyo reporte se quiere mitigar.
     * @param userId ID del usuario que confirma la existencia.
     */
    suspend fun confirmExistence(fountain: Fountain, userId: String): Result<Unit> {
        if (fountain.negativeVotes <= 0) {
            return Result.success(Unit)
        }

        val newVotes = (fountain.negativeVotes - 1).coerceAtLeast(0)
        // Eliminamos al usuario de la lista de detractores si decide retractarse
        val newVoters = fountain.votedByNegative.filter { it != userId }

        val updates = mapOf(
            "negativeVotes" to newVotes,
            "votedByNegative" to newVoters
        )

        return repository.updateFountain(fountain.id, updates)
    }
}