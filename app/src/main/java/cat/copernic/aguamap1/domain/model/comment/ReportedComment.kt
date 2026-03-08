package cat.copernic.aguamap1.domain.model.comment

/**
 * Representa una denuncia específica dirigida a un comentario de un usuario.
 * Este modelo consolida la información de la queja junto con el contenido original
 * del comentario para facilitar las tareas de moderación en el panel administrativo.
 *
 * @property reportId Identificador único del reporte generado en la base de datos.
 * @property fountainId ID de la fuente donde se encuentra alojado el comentario denunciado.
 * @property commentId ID del comentario específico que ha sido marcado como inapropiado.
 * @property reason Motivo o descripción de la queja proporcionada por el denunciante.
 * @property timestamp Marca de tiempo en milisegundos que indica cuándo se realizó la denuncia.
 * @property comment Objeto [Comment] completo que contiene el texto original, el autor y la valoración.
 */
data class ReportedComment(
    val reportId: String = "",
    val fountainId: String = "",
    val commentId: String = "",
    val reason: String = "",
    val timestamp: Long = 0L,
    val comment: Comment? = null
)