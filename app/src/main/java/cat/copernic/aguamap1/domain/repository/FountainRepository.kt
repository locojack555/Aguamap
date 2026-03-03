package cat.copernic.aguamap1.domain.repository

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import kotlinx.coroutines.flow.Flow

interface FountainRepository {

    // --- GESTIÓN DE FUENTES ---

    /**
     * Escucha la lista de fuentes.
     * Si lat/lng son null, debería devolver la colección completa.
     */
    fun fetchSources(lat: Double?, lng: Double?): Flow<Result<List<Fountain>>>

    /**
     * NUEVO: Obtiene todas las fuentes de forma directa (suspendida).
     * Vital para la validación del juego sin errores de Flow transparency.
     */
    suspend fun getAllFountainsDirect(): Result<List<Fountain>>

    /**
     * Busca la fuente por su id.
     */
    suspend fun getFountainById(fountainId: String): Result<Fountain>

    /**
     * Crea una nueva fuente en la colección principal.
     */
    suspend fun createFountain(fountain: Fountain): Result<Unit>

    /**
     * Actualiza campos específicos de una fuente.
     */
    suspend fun updateFountain(fountainId: String, updates: Map<String, Any>): Result<Unit>

    /**
     * Borra una fuente.
     */
    suspend fun deleteFountain(fountainId: String): Result<Unit>


    // --- GESTIÓN DE COMENTARIOS (SUBCOLECCIÓN) ---

    /**
     * Añade una valoración a la subcolección de una fuente específica.
     */
    suspend fun addComment(fountainId: String, comment: Comment): Result<Unit>

    /**
     * Escucha en tiempo real las valoraciones de una fuente concreta.
     */
    fun fetchComments(fountainId: String): Flow<Result<List<Comment>>>

    /**
     * Actualiza un comentario.
     */
    suspend fun updateComment(
        fountainId: String,
        commentId: String,
        updates: Map<String, Any>
    ): Result<Unit>

    /**
     * Elimina un comentario permanentemente.
     */
    suspend fun deleteComment(fountainId: String, commentId: String): Result<Unit>

    /**
     * Reporta un comentario para revisión.
     */
    suspend fun reportComment(fountainId: String, commentId: String, reason: String): Result<Unit>

    // --- ESTADÍSTICAS DE USUARIO ---

    /**
     * Actualiza el contador de fuentes creadas por el usuario.
     */
    suspend fun updateUserFountainsCount(userId: String, userName: String, increment: Int): Result<Unit>

    /**
     * Actualiza el contador de comentarios realizados por el usuario.
     */
    suspend fun updateUserCommentsCount(userId: String, userName: String, increment: Int): Result<Unit>

    /**
     * Obtiene el número total de fuentes creadas por un usuario.
     */
    suspend fun getUserFountainsCount(userId: String): Int

    /**
     * Obtiene el número total de comentarios de un usuario.
     */
    suspend fun getUserCommentsCount(userId: String): Int
}