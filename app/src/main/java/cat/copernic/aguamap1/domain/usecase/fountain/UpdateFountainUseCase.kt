package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class UpdateFountainUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend operator fun invoke(fountainId: String, updates: Map<String, Any>): Result<Unit> {
        return repository.updateFountain(fountainId, updates)
    }
}