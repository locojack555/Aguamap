package cat.copernic.aguamap1.presentation.ranking

import com.google.firebase.Timestamp

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

data class GameSession(
    val userId: String = "",
    val userName: String = "",
    val score: Int = 0,
    val date: Timestamp? = null,
    val fountainId: String = ""
)