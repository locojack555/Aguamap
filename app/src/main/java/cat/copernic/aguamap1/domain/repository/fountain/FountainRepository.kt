package cat.copernic.aguamap1.domain.repository.fountain

import cat.copernic.aguamap1.domain.model.comment.Comment
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Dominio para la gestión integral de fuentes de agua.
 * Define las operaciones para el mapa (geolocalización), la interacción social
 * (comentarios/valoraciones) y el seguimiento de actividad del usuario.
 */
interface FountainRepository {

    // --- GESTIÓN DE FUENTES ---

    /**
     * Recupera y escucha fuentes cercanas o la colección completa.
     * @param lat Latitud para búsqueda por proximidad (opcional).
     * @param lng Longitud para búsqueda por proximidad (opcional).
     * @return Flow reactivo que emite la lista de fuentes filtradas por radio (ej. 8km) o todas.
     */
    fun fetchSources(lat: Double?, lng: Double?): Flow<Result<List<Fountain>>>

    /**
     * Recupera todas las fuentes de forma atómica y única.
     * Diseñado específicamente para evitar problemas de concurrencia o 'Flow transparency'
     * en procesos críticos como la validación del mini-juego.
     * @return Result con la lista completa de fuentes.
     */
    suspend fun getAllFountainsDirect(): Result<List<Fountain>>

    /**
     * Obtiene los detalles de una fuente específica.
     * @param fountainId Identificador del documento de la fuente.
     */
    suspend fun getFountainById(fountainId: String): Result<Fountain>

    /**
     * Registra una nueva fuente en el sistema.
     * La implementación debe encargarse de generar el GeoHash y vincular el creador.
     * @param fountain Objeto con los datos de la nueva fuente.
     */
    suspend fun createFountain(fountain: Fountain): Result<Unit>

    /**
     * Permite la edición parcial de los datos de una fuente.
     * @param fountainId ID de la fuente a modificar.
     * @param updates Mapa con los pares clave-valor de los campos a actualizar.
     */
    suspend fun updateFountain(fountainId: String, updates: Map<String, Any>): Result<Unit>

    /**
     * Elimina una fuente del sistema.
     * @param fountainId ID del documento a borrar.
     */
    suspend fun deleteFountain(fountainId: String): Result<Unit>


    // --- GESTIÓN DE COMENTARIOS (SUBCOLECCIÓN) ---

    /**
     * Añade una reseña o comentario a una fuente.
     * Debe ejecutarse idealmente como una transacción para actualizar el contador de ratings de la fuente.
     * @param fountainId ID de la fuente donde se comenta.
     * @param comment Objeto comentario con el contenido y autoría.
     */
    suspend fun addComment(fountainId: String, comment: Comment): Result<Unit>

    /**
     * Suscripción en tiempo real a los comentarios de una fuente.
     * @param fountainId ID de la fuente de la cual obtener reseñas.
     * @return Flow con la lista de comentarios ordenada cronológicamente.
     */
    fun fetchComments(fountainId: String): Flow<Result<List<Comment>>>

    /**
     * Modifica el contenido de un comentario existente.
     * @param fountainId ID de la fuente contenedora.
     * @param commentId ID del comentario específico.
     * @param updates Campos a modificar (ej. texto, puntuación).
     */
    suspend fun updateComment(
        fountainId: String,
        commentId: String,
        updates: Map<String, Any>
    ): Result<Unit>

    /**
     * Elimina un comentario y actualiza las estadísticas de la fuente.
     * @param fountainId ID de la fuente contenedora.
     * @param commentId ID del comentario a eliminar.
     */
    suspend fun deleteComment(fountainId: String, commentId: String): Result<Unit>

    /**
     * Registra una denuncia sobre un comentario inapropiado.
     * @param fountainId ID de la fuente.
     * @param commentId ID del comentario reportado.
     * @param reason Motivo del reporte para moderación.
     */
    suspend fun reportComment(fountainId: String, commentId: String, reason: String): Result<Unit>

    // --- ESTADÍSTICAS DE USUARIO ---

    /**
     * Modifica el contador histórico de fuentes aportadas por un usuario.
     * @param userId UID del usuario.
     * @param userName Nombre para mostrar (para redundancia en estadísticas).
     * @param increment Valor positivo para añadir o negativo para restar.
     */
    suspend fun updateUserFountainsCount(userId: String, userName: String, increment: Int): Result<Unit>

    /**
     * Modifica el contador histórico de comentarios realizados por el usuario.
     * @param userId UID del usuario.
     * @param userName Nombre para mostrar.
     * @param increment Valor de ajuste del contador.
     */
    suspend fun updateUserCommentsCount(userId: String, userName: String, increment: Int): Result<Unit>

    /**
     * Consulta cuántas fuentes ha creado un usuario específico.
     */
    suspend fun getUserFountainsCount(userId: String): Int

    /**
     * Consulta cuántos comentarios ha redactado un usuario específico.
     */
    suspend fun getUserCommentsCount(userId: String): Int
}