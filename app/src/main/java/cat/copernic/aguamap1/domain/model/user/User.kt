package cat.copernic.aguamap1.domain.model.user

import cat.copernic.aguamap1.domain.model.user.UserRole

/**
 * Representa la entidad de usuario dentro del dominio de AguaMap.
 * * @property uid Identificador único del usuario.
 * @property nom Nombre completo o alias del usuario.
 * @property email Dirección de correo electrónico.
 * @property role Nivel de privilegios del usuario.
 * @property language Código del idioma preferido (es, ca, en).
 */
data class User(
    val uid: String = "",
    val nom: String = "",
    val email: String = "",
    val role: UserRole = UserRole.USER,
    val language: String = "es"
)