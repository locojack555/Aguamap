package cat.copernic.aguamap1.domain.model

/**
 * Representa la entidad de usuario dentro del dominio de AguaMap.
 * Contiene los datos básicos de identidad y los permisos necesarios para
 * gestionar el acceso a funciones restringidas de la aplicación.
 *
 * @property uid Identificador único del usuario (proveniente de Firebase Authentication).
 * @property nom Nombre completo o alias del usuario para su visualización pública.
 * @property email Dirección de correo electrónico asociada a la cuenta del usuario.
 * @property role Nivel de privilegios del usuario (determina el acceso a funciones de moderación).
 */
data class User(
    val uid: String,
    val nom: String,
    val email: String,
    val role: UserRole = UserRole.USER
)