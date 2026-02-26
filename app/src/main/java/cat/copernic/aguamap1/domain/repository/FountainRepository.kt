package cat.copernic.aguamap1.domain.repository

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.Report
import cat.copernic.aguamap1.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface FountainRepository {

    // --- GESTIÓN DE FUENTES ---

    /**
     * Escucha en tiempo real la lista de fuentes.
     */
    fun fetchSources(): Flow<Result<List<Fountain>>>

    /**
     * Crea una nueva fuente en la colección principal.
     */
    suspend fun createFountain(fountain: Fountain): Result<Unit>

    /**
     * Actualiza campos específicos de una fuente (Editar, Confirmar, Reportar avería).
     * @param updates Mapa con los pares campo-valor a modificar.
     */
    suspend fun updateFountain(fountainId: String, updates: Map<String, Any>): Result<Unit>

    /**
     * Borra una fuente (Admin o por acumulación de reportes "No existe").
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
     * Actualiza un comentario (Censurar texto por Admin o editar por el usuario).
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
     *
     */
    suspend fun updateUserFountainsCount(userId: String, userName: String, increment: Int): Result<Unit>

    /**
     *
     */
    suspend fun updateUserCommentsCount(userId: String, userName: String, increment: Int): Result<Unit>

    /**
     *
     */
    suspend fun getUserFountainsCount(userId: String): Int

    /**
     *
     */
    suspend fun getUserCommentsCount(userId: String): Int

    suspend fun reportComment(fountainId: String, commentId: String, userId: String): Result<Unit>

}