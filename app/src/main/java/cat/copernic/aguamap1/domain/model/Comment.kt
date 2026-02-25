package cat.copernic.aguamap1.domain.model

data class Comment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val censored: Boolean = false,
    val reported: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)