package cat.copernic.aguamap1.domain.model

data class Report(
    val id: String = "",
    val fountainId: String = "",     // Añadido = ""
    val fountainName: String = "",   // Añadido = ""
    val userId: String = "",         // Añadido = ""
    val description: String = "",    // Añadido = ""
    val timestamp: Long = System.currentTimeMillis(),
    val resolved: Boolean = false
)