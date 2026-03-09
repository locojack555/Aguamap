package cat.copernic.aguamap1.domain.usecase.comment

import cat.copernic.aguamap1.domain.model.comment.Comment
import cat.copernic.aguamap1.domain.repository.fountain.FountainRepository
import javax.inject.Inject

/**
 * Caso de Uso responsable de registrar un nuevo comentario.
 * Delega la lógica de integridad y promedio al repositorio mediante transacciones.
 */
class AddCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    /**
     * Ejecuta el guardado del comentario.
     * * @param fountainId ID de la fuente.
     * @param comment Objeto comentario a insertar.
     */
    suspend operator fun invoke(fountainId: String, comment: Comment): Result<Unit> {
        return repository.addComment(fountainId, comment)
    }
}