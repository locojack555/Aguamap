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
class CensorCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    /**
     * Ejecuta la lógica de censura mediante una actualización atómica de campos.
     * * @param fountainId El identificador de la fuente donde reside el comentario.
     * @param commentId El identificador del comentario que se desea censurar.
     * @return [Result] que indica si la operación en la base de datos fue exitosa.
     */
    suspend operator fun invoke(fountainId: String, commentId: String): Result<Unit> {
        // Preparamos los cambios: censuramos el contenido y cerramos el reporte
        val updates = mapOf(
            "censored" to true,
            "reported" to false
        )

        return repository.updateComment(fountainId, commentId, updates)
    }
}