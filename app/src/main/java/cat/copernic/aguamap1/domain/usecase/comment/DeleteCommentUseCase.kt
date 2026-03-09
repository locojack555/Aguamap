package cat.copernic.aguamap1.domain.usecase.comment

import cat.copernic.aguamap1.domain.repository.fountain.FountainRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de eliminar un comentario de una fuente.
 * La transacción en el repositorio asegura que el totalRating y el ratingAverage
 * se actualicen o se reinicien a 0 de forma atómica.
 */
class DeleteCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    /**
     * Ejecuta el proceso de eliminación.
     * * @param fountainId ID de la fuente.
     * @param commentId ID del comentario a borrar.
     */
    suspend operator fun invoke(fountainId: String, commentId: String): Result<Unit> {
        return repository.deleteComment(fountainId, commentId)
    }
}