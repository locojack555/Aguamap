package cat.copernic.aguamap1.presentation.profile

data class ProfileState (
    val userName: String = "Administrador",
    val userEmail: String = "admin@admin.com",
    val userRole: String = "User",
    val fountainsCount: Int = 0,
    val ratingsCount: Int = 1,
    val points: Int = 0
)