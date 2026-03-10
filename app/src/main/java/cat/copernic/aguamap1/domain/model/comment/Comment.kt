package cat.copernic.aguamap1.domain.model.comment

/**
 * Representa una valoración o reseña realizada por un usuario sobre una fuente.
 * Contiene metadatos de autoría, puntuación y estados para la moderación de contenido.
 *
 * @property id Identificador único del comentario (ID del documento en la subcolección de Firestore).
 * @property userId Identificador único (UID) del autor del comentario.
 * @property userName Nombre para mostrar del autor en el momento de la publicación.
 * @property rating Puntuación otorgada a la fuente (generalmente en una escala de 1 a 5).
 * @property comment Contenido textual de la reseña.
 * @property censored Indica si el contenido ha sido ocultado por un administrador debido a infracciones.
 * @property reported Indica si el comentario ha sido marcado por otros usuarios para su revisión.
 * @property timestamp Marca de tiempo en milisegundos de la creación del comentario para ordenación cronológica.
 */
data class Comment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val censored: Boolean = false,
    val reported: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)