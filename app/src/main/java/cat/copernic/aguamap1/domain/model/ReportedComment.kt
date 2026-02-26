package cat.copernic.aguamap1.domain.model

data class ReportedComment(
    val reportId: String = "",
    val fountainId: String = "",
    val commentId: String = "",
    val reason: String = "",
    val timestamp: Long = 0L,
    val comment: Comment? = null,
    val isResolved: Boolean = false
)
