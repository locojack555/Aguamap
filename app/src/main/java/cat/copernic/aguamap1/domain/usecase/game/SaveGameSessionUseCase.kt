package cat.copernic.aguamap1.domain.usecase.game

import cat.copernic.aguamap1.domain.model.GameSession
import cat.copernic.aguamap1.domain.repository.GameRepository
import javax.inject.Inject

class SaveGameSessionUseCase @Inject constructor(
    private val repository: GameRepository
) {
    suspend operator fun invoke(session: GameSession): Result<Unit> {
        return repository.saveGameSession(session)
    }
}