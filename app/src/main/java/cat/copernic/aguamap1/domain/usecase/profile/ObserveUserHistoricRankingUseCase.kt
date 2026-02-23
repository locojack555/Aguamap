package cat.copernic.aguamap1.domain.usecase.profile

import cat.copernic.aguamap1.domain.model.UserRanking
import cat.copernic.aguamap1.domain.repository.RankingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserHistoricRankingUseCase @Inject constructor(
    private val repository: RankingRepository
) {
    operator fun invoke(userId: String): Flow<UserRanking?> {
        return repository.observeUserHistoricRanking(userId)
    }
}