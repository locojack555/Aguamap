package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.StateFountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class ProcessFountainVoteUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend fun addPositiveVote(fountain: Fountain, userId: String): Result<Unit> {
        // 1. Si ya tiene 3 o más votos positivos, no se puede votar más (ya está aceptada)
        if (fountain.positiveVotes >= 3) {
            return Result.failure(Exception("Esta fuente ya ha sido validada"))
        }

        // 2. Si el usuario ya votó positivo, no puede repetir
        if (fountain.votedByPositive.contains(userId)) {
            return Result.failure(Exception("Ya has validado esta fuente"))
        }

        val newVotes = fountain.positiveVotes + 1
        val newVoters = fountain.votedByPositive + userId

        val updates = mutableMapOf<String, Any>(
            "positiveVotes" to newVotes,
            "votedByPositive" to newVoters
        )

        // Si llega a 3, cambia a ACCEPTED
        if (newVotes >= 3) {
            updates["status"] = StateFountain.ACCEPTED.name
        }

        return repository.updateFountain(fountain.id, updates)
    }

    suspend fun addNegativeVote(fountain: Fountain, userId: String): Result<Unit> {
        // 1. Si el usuario ya votó negativo, no puede repetir
        if (fountain.votedByNegative.contains(userId)) {
            return Result.failure(Exception("Ya has reportado esta fuente"))
        }

        val newVotes = fountain.negativeVotes + 1
        val newVoters = fountain.votedByNegative + userId

        // Si llega a 3, se borra directamente
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
}