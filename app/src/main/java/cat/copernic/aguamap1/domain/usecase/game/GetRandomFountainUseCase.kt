package cat.copernic.aguamap1.domain.usecase.game

import cat.copernic.aguamap1.domain.repository.GameRepository
import javax.inject.Inject

class GetRandomFountainUseCase @Inject constructor(
    private val repository: GameRepository
) {
    suspend operator fun invoke(): Result<cat.copernic.aguamap1.domain.model.Fountain?> {
        return repository.getRandomFountain()
    }
}