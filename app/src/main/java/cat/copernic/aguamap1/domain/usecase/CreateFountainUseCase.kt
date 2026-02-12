package cat.copernic.aguamap1.domain.usecase

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository

class CreateFountainUseCase(private val repository: FountainRepository) {
    suspend operator fun invoke(fountain: Fountain, isUserAdmin: Boolean): Result<Unit> {
        val fountainToSave = if (isUserAdmin) {
            fountain.copy(
                status = "APPROVED",
                isAdminVerified = true,
                positiveVotes = 3,
                createdBy = "ADMINISTRADOR"
            )
        } else {
            fountain.copy(
                status = "PENDING",
                positiveVotes = 1
            )
        }
        return repository.createFountain(fountainToSave)
    }
}