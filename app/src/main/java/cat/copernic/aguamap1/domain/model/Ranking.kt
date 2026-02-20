package cat.copernic.aguamap1.domain.model

data class UserRanking(
    val position: Int = 0,
    val name: String = "",
    val points: Int = 0,
    val discovered: Int = 0,
    val games: Int = 0,
    val isCurrentUser: Boolean = false
)