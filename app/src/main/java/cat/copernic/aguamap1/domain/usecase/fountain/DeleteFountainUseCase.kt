package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class DeleteFountainUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend operator fun invoke(fountainId: String): Result<Unit> {
        return repository.deleteFountain(fountainId)
    }
}