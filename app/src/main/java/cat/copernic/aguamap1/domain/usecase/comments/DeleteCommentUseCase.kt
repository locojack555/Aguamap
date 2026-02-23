package cat.copernic.aguamap1.domain.usecase.comments

import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

/**
 * UseCase para eliminar un comentario de una fuente.
 * Orquesta la eliminación del documento y la actualización de los contadores.
 */
class DeleteCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend operator fun invoke(fountainId: String, commentId: String): Result<Unit> {
        // 1. Intentamos borrar el comentario.
        // Con la nueva versión del repositorio, este método ya decrementa 'totalRatings'.
        val deleteResult = repository.deleteComment(fountainId, commentId)

        return if (deleteResult.isSuccess) {
            // Si en el futuro quisieras hacer algo extra tras el borrado exitoso,
            // como registrar un log o actualizar otro contador, lo harías aquí.
            Result.success(Unit)
        } else {
            // Si el borrado falla (ej. sin internet o permisos), devolvemos el error.
            deleteResult
        }
    }
}