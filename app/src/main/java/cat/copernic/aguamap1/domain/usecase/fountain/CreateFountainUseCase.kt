package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class CreateFountainUseCase @Inject constructor(private val repository: FountainRepository) {
    suspend operator fun invoke(fountain: Fountain, isUserAdmin: Boolean): Result<Unit> {
        val fountainToSave = if (isUserAdmin) {
            fountain.copy(
                status = "APPROVED",
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