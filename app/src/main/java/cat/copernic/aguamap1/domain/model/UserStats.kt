package cat.copernic.aguamap1.domain.model

import com.google.firebase.Timestamp

/**
 * Representa las estadísticas de actividad acumuladas de un usuario.
 * Este modelo actúa como una caché de rendimiento (denormalización) para mostrar
 * rápidamente el impacto del usuario en la comunidad sin realizar consultas pesadas.
 *
 * @property userId Identificador único del usuario al que pertenecen las estadísticas.
 * @property userName Nombre del usuario (sincronizado para evitar joins en la UI).
 * @property fountainsCount Número total de fuentes que el usuario ha creado y han sido aprobadas.
 * @property commentsCount Cantidad total de reseñas y valoraciones publicadas por el usuario.
 * @property lastUpdated Marca de tiempo de la última vez que se actualizaron estos contadores.
 */
data class UserStats(
    val userId: String = "",
    val userName: String = "",
    val fountainsCount: Int = 0,
    val commentsCount: Int = 0,
    val lastUpdated: Timestamp? = null
)