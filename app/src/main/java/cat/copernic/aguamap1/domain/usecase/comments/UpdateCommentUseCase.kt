package cat.copernic.aguamap1.domain.usecase.comments

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject
import kotlin.math.round

/**
 * Caso de Uso responsable de editar un comentario existente.
 * Gestiona de forma inteligente la actualización del texto, la limpieza de estados
 * de reporte y el recálculo de la media de la fuente si el usuario modifica su valoración.
 *
 * @property repository Repositorio de fuentes para el acceso a Firestore.
 */
class UpdateCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    /**
     * Ejecuta la actualización del comentario y, si es necesario, de la fuente padre.
     *
     * @param fountain Objeto [Fountain] actual para obtener las estadísticas de rating.
     * @param oldComment Datos previos del comentario (necesarios para comparar la nota antigua).
     * @param newRating Nueva puntuación otorgada por el usuario.
     * @param newText Nuevo cuerpo del mensaje.
     * @return [Result] que indica el éxito o fallo de la operación coordinada.
     */
    suspend operator fun invoke(
        fountain: Fountain,
        oldComment: Comment,
        newRating: Int,
        newText: String
    ): Result<Unit> {
        // 1. Preparación: Actualizamos el contenido, la fecha y reseteamos el estado de reporte.
        // Si un usuario edita su comentario, se asume que puede haber corregido la causa de una denuncia.
        val updates = mapOf(
            "rating" to newRating,
            "comment" to newText,
            "reported" to false,
            "timestamp" to System.currentTimeMillis()
        )

        // 2. Persistencia: Actualizamos el comentario en la subcolección.
        val result = repository.updateComment(fountain.id, oldComment.id, updates)

        return if (result.isSuccess) {
            // 3. Lógica de Negocio: Solo recalculamos la media si la nota ha cambiado.
            // Si el usuario solo corrigió una falta de ortografía, ahorramos una escritura en Firestore.
            if (oldComment.rating != newRating) {
                val total = fountain.totalRatings
                if (total > 0) {
                    // Fórmula de actualización de media: Reemplazamos el valor antiguo por el nuevo en la suma total.
                    val currentSum = fountain.ratingAverage * total
                    val rawAverage = (currentSum - oldComment.rating + newRating) / total

                    // Normalización a 1 decimal
                    val newAverage = round(rawAverage * 10) / 10.0

                    repository.updateFountain(
                        fountain.id,
                        mapOf("ratingAverage" to newAverage)
                    )
                } else {
                    Result.success(Unit)
                }
            } else {
                Result.success(Unit)
            }
        } else {
            result
        }
    }
}