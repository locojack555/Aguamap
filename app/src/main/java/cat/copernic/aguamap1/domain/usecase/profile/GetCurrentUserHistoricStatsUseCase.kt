package cat.copernic.aguamap1.domain.usecase.profile

import cat.copernic.aguamap1.domain.model.UserRanking
import cat.copernic.aguamap1.domain.repository.RankingRepository
import javax.inject.Inject

class GetCurrentUserHistoricStatsUseCase @Inject constructor(
    private val repository: RankingRepository
) {
    suspend operator fun invoke(userId: String): UserRanking? {
        return repository.getCurrentUserHistoricRanking(userId)
    }
}