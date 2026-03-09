package cat.copernic.aguamap1.domain.usecase.comment

import cat.copernic.aguamap1.domain.repository.fountain.FountainRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de aplicar la censura administrativa sobre un comentario.
 * Esta acción oculta el comentario de la vista pública y marca la incidencia
 * de reporte como resuelta.
 *
 * @property repository El repositorio de fuentes, que gestiona las subcolecciones de comentarios en Firestore.
 */
class CensorCommentUseCase @Inject constructor(private val repository: FountainRepository) {
    suspend operator fun invoke(fountainId: String, commentId: String): Result<Unit> {
        val updates = mapOf(
            "censored" to true,
            "reported" to false
        )
        // Al no pasar oldRating ni newRating, el repo solo actualiza el comentario
        return repository.updateComment(fountainId, commentId, updates)
    }
}