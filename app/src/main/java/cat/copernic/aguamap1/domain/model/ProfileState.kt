package cat.copernic.aguamap1.domain.model

/**
 * Representa el estado integral del perfil de un usuario para su visualización en la UI.
 * Este modelo consolida datos de diferentes fuentes (Auth, Firestore Rankings y
 * estadísticas de actividad) para facilitar la reactividad en la pantalla de perfil.
 *
 * @property userName Nombre público del usuario extraído de su perfil.
 * @property userEmail Dirección de correo electrónico asociada a la cuenta.
 * @property userRole Rol actual del usuario en el sistema (ej. "USER", "ADMIN").
 * @property profilePictureUrl Enlace a la imagen de perfil alojada en la nube; null si usa la predeterminada.
 * @property fountainsCount Número total de fuentes que este usuario ha dado de alta en el sistema.
 * @property ratingsCount Cantidad total de comentarios y valoraciones realizadas por el usuario.
 * @property points Puntuación acumulada obtenida a través del mini-juego y aportaciones.
 */
data class ProfileState (
    val userName: String = "",
    val userEmail: String = "",
    val userRole: String = "",
    val profilePictureUrl: String? = null, // Foto de perfil opcional (Gestionada vía Cloudinary)
    val fountainsCount: Int = 0,
    val ratingsCount: Int = 0,
    val points: Int = 0
)