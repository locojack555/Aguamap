package cat.copernic.aguamap1.domain.usecase.profile

import cat.copernic.aguamap1.domain.model.UserStats
import cat.copernic.aguamap1.domain.repository.FountainRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserStatsUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    operator fun invoke(userId: String): Flow<UserStats?> {
        return repository.observeUserStats(userId)
    }
}