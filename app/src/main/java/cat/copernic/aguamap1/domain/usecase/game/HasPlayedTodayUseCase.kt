package cat.copernic.aguamap1.domain.usecase.game

import cat.copernic.aguamap1.domain.repository.GameRepository
import javax.inject.Inject

class HasPlayedTodayUseCase @Inject constructor(
    private val repository: GameRepository
) {
    suspend operator fun invoke(userId: String): Result<Boolean> {
        return repository.hasPlayedToday(userId)
    }
}