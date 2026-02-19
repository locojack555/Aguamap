package cat.copernic.aguamap1.domain.model

data class User(
    val uid: String,
    val nom: String,
    val email: String,
    val role: UserRole = UserRole.USER
)