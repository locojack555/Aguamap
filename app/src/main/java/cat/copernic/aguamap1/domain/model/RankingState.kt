package cat.copernic.aguamap1.domain.model

data class RankingState(
    val players: List<UserRanking> = emptyList(),
    val isLoading: Boolean = false
)