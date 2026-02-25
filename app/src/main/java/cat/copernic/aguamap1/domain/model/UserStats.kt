package cat.copernic.aguamap1.domain.model

import com.google.firebase.Timestamp

data class UserStats(
    val userId: String = "",
    val userName: String = "",
    val fountainsCount: Int = 0,
    val commentsCount: Int = 0,
    val lastUpdated: Timestamp? = null
)
