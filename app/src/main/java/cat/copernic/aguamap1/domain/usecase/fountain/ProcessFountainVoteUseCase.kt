package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.StateFountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class ProcessFountainVoteUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend fun addPositiveVote(fountain: Fountain, userId: String): Result<Unit> {
        if (fountain.positiveVotes >= 3) {
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

        if (newVotes >= 3) {
            updates["status"] = StateFountain.ACCEPTED.name
        }

        return repository.updateFountain(fountain.id, updates)
    }

    suspend fun addNegativeVote(fountain: Fountain, userId: String): Result<Unit> {
        if (fountain.votedByNegative.contains(userId)) {
            return Result.failure(Exception("Ya has reportado esta fuente"))
        }

        val newVotes = fountain.negativeVotes + 1
        val newVoters = fountain.votedByNegative + userId

        return if (newVotes >= 3) {
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

    // --- NUEVA FUNCIÓN PARA DESMENTIR EL REPORTE ---
    suspend fun confirmExistence(fountain: Fountain, userId: String): Result<Unit> {
        // Solo actuamos si hay votos negativos que quitar
        if (fountain.negativeVotes <= 0) {
            return Result.success(Unit)
        }

        // 1. Calculamos el nuevo conteo (mínimo 0)
        val newVotes = (fountain.negativeVotes - 1).coerceAtLeast(0)

        // 2. Quitamos al usuario de la lista de negativos si estaba en ella
        // (Esto permite que si alguien se equivocó al reportar, pueda corregirse)
        val newVoters = fountain.votedByNegative.filter { it != userId }

        val updates = mapOf(
            "negativeVotes" to newVotes,
            "votedByNegative" to newVoters
        )

        return repository.updateFountain(fountain.id, updates)
    }
}