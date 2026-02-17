package cat.copernic.aguamap1.domain.model

import java.util.Date

data class GameSession(
    val userId: String = "",
    val userName: String = "",
    val score: Int = 0,
    val distance: Double = 0.0,
    val date: Date = Date(),
    val fountainId: String = "",
    val fountainName: String = ""
)