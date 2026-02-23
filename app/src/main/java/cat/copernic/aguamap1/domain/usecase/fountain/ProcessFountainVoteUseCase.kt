package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.StateFountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class ProcessFountainVoteUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    // Si el usuario ya votó, devolvemos un error para no procesar nada

    suspend fun addPositiveVote(fountain: Fountain, userId: String): Result<Unit> {
        // 1. Verificar si el usuario ya ha votado (positivo o negativo)
        if (fountain.votedBy.contains(userId)) {
            return Result.failure(Exception("Ya has votado en esta fuente"))
        }

        val newVotes = fountain.positiveVotes + 1
        val newVoters = fountain.votedBy + userId

        val updates = mutableMapOf<String, Any>(
            "positiveVotes" to newVotes,
            "votedBy" to newVoters
        )

        if (newVotes >= 3) {
            updates["status"] = StateFountain.ACCEPTED.name
        }

        return repository.updateFountain(fountain.id, updates)
    }

    suspend fun addNegativeVote(fountain: Fountain, userId: String): Result<Unit> {
        // 1. Verificar si el usuario ya ha votado
        if (fountain.votedBy.contains(userId)) {
            return Result.failure(Exception("Ya has votado en esta fuente"))
        }

        val newVotes = fountain.negativeVotes + 1
        val newVoters = fountain.votedBy + userId

        return if (newVotes >= 3) {
            repository.deleteFountain(fountain.id)
        } else {
            repository.updateFountain(
                fountain.id, mapOf(
                    "negativeVotes" to newVotes,
                    "votedBy" to newVoters
                )
            )
        }
    }
}