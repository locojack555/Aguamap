package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.StateFountain
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class CreateFountainUseCase @Inject constructor(
    private val repository: FountainRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(fountain: Fountain, isUserAdmin: Boolean): Result<Unit> {
        val currentUser = authRepository.getCurrentUserUid()

        // Creamos la lista inicial con el ID del creador si existe
        val initialVoterList = if (currentUser != null) listOf(currentUser) else emptyList()

        // 1. Campos base
        val baseFountain = fountain.copy(
            dateCreated = java.util.Date(),
            ratingAverage = 0.0,
            totalRatings = 0,
            negativeVotes = 0,
            id = ""
        )

        // 2. Lógica de roles con registro en 'votedBy'
        val fountainToSave = if (isUserAdmin) {
            baseFountain.copy(
                status = StateFountain.ACCEPTED,
                positiveVotes = 3,
                votedByPositive = initialVoterList, // El admin cuenta como votante
                createdBy = currentUser ?: "ADMIN"
            )
        } else {
            baseFountain.copy(
                status = StateFountain.PENDING,
                positiveVotes = 1,
                votedByPositive = initialVoterList, // <--- AQUÍ: El creador se añade a la lista de votos
                createdBy = currentUser ?: "ANONYMOUS"
            )
        }

        return repository.createFountain(fountainToSave)
    }
}