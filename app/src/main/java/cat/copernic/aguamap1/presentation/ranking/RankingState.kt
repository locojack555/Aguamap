package cat.copernic.aguamap1.presentation.ranking

data class UserRanking(
    val position: Int,
    val name: String,
    val points: Int,
    val discovered: Int,
    val games: Int,
    val isCurrentUser: Boolean = false
)

data class RankingState(
    val players: List<UserRanking> = emptyList(),
    val isLoading: Boolean = false
)