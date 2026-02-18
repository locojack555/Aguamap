package cat.copernic.aguamap1.domain.usecase.game

import cat.copernic.aguamap1.domain.model.GameSession
import cat.copernic.aguamap1.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodaysSessionsUseCase @Inject constructor(
    private val repository: GameRepository
) {
    operator fun invoke(): Flow<Result<List<GameSession>>> {
        return repository.getTodaysSessions()
    }
}