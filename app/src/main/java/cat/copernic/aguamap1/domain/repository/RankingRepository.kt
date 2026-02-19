package cat.copernic.aguamap1.domain.repository

import cat.copernic.aguamap1.domain.model.UserRanking
import cat.copernic.aguamap1.domain.model.RankingPeriod
import kotlinx.coroutines.flow.Flow

interface RankingRepository {
    suspend fun getDailyRanking(): List<UserRanking>
    suspend fun getMonthlyRanking(): List<UserRanking>
    suspend fun getYearlyRanking(): List<UserRanking>
    suspend fun getRankingByPeriod(period: RankingPeriod): List<UserRanking>
    fun getCurrentUserId(): String?
}