package cat.copernic.aguamap1.presentation.profile

data class ProfileState (
    val userName: String = "",
    val userEmail: String = "",
    val userRole: String = "",
    val fountainsCount: Int = 0,
    val ratingsCount: Int = 0,
    val points: Int = 0
)