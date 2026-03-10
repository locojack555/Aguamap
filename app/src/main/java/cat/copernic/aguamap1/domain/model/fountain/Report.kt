package cat.copernic.aguamap1.domain.model.fountain

/**
 * Representa una denuncia o reporte de incidencia sobre una fuente de agua.
 * Se utiliza para que los usuarios notifiquen datos erróneos, fuentes inexistentes
 * o contenido inapropiado que deba ser revisado por un administrador.
 *
 * @property id Identificador único del reporte en la colección de moderación de Firestore.
 * @property fountainId ID de la fuente que está siendo reportada.
 * @property fountainName Nombre de la fuente (almacenado para facilitar la lectura del administrador sin consultas extra).
 * @property userId ID del usuario que ha emitido la denuncia.
 * @property description Explicación detallada del motivo del reporte brindada por el usuario.
 * @property timestamp Marca de tiempo en milisegundos que indica cuándo se creó la denuncia.
 * @property resolved Estado del reporte: 'true' si ya ha sido gestionado por un administrador, 'false' si está pendiente.
 */
data class Report(
    val id: String = "",
    val fountainId: String = "",
    val fountainName: String = "",
    val userId: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val resolved: Boolean = false
)