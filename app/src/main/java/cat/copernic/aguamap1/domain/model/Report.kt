package cat.copernic.aguamap1.domain.model

data class Report(
    val id: String = "",
    val fountainId: String = "",
    val fountainName: String = "",
    val userId: String = "",
    val description: String = "",
    val timestamp: Long = 0L,
    val resolved: Boolean = false
)