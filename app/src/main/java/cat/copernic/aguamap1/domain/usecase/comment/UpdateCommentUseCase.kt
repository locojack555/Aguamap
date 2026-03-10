package cat.copernic.aguamap1.domain.usecase.comment

import cat.copernic.aguamap1.domain.model.comment.Comment
import cat.copernic.aguamap1.domain.repository.fountain.FountainRepository
import javax.inject.Inject

/**
 * Caso de Uso responsable de editar un comentario existente.
 * Gestiona de forma inteligente la actualización del texto, la limpieza de estados
 * de reporte y el recálculo de la media de la fuente si el usuario modifica su valoración.
 *
 * @property repository Repositorio de fuentes para el acceso a Firestore.
 */
class UpdateCommentUseCase @Inject constructor(private val repository: FountainRepository) {
    suspend operator fun invoke(
        fountainId: String,
        oldComment: Comment,
        newRating: Int,
        newText: String
    ): Result<Unit> {
        val updates = mapOf(
            "rating" to newRating,
            "comment" to newText,
            "reported" to false,
            "timestamp" to System.currentTimeMillis()
        )

        return repository.updateComment(
            fId = fountainId,
            cId = oldComment.id,
            updates = updates,
            oldRating = oldComment.rating, // Enviamos para recalcular
            newRating = newRating          // Enviamos para recalcular
        )
    }
}