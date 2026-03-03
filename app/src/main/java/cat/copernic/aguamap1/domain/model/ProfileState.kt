package cat.copernic.aguamap1.domain.model

data class ProfileState (
    val userName: String = "",
    val userEmail: String = "",
    val userRole: String = "",
    val profilePictureUrl: String? = null,  // NUEVO: foto de perfil (puede ser null)
    val fountainsCount: Int = 0,
    val ratingsCount: Int = 0,
    val points: Int = 0
)