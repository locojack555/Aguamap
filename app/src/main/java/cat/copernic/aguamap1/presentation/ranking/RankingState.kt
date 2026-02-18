package cat.copernic.aguamap1.presentation.ranking

import cat.copernic.aguamap1.domain.model.UserRanking

data class RankingState(
    val players: List<UserRanking> = emptyList(),
    val isLoading: Boolean = false
)