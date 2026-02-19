package cat.copernic.aguamap1.domain.usecase.ranking

import cat.copernic.aguamap1.domain.model.RankingPeriod
import cat.copernic.aguamap1.domain.model.UserRanking
import cat.copernic.aguamap1.domain.repository.RankingRepository
import javax.inject.Inject

class GetRankingByPeriodUseCase @Inject constructor(
    private val repository: RankingRepository
) {
    suspend operator fun invoke(period: RankingPeriod): List<UserRanking> =
        repository.getRankingByPeriod(period)
}