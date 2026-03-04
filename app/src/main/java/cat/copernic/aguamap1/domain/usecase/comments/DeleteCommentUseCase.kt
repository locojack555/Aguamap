package cat.copernic.aguamap1.domain.usecase.comments

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject
import kotlin.math.round

/**
 * Caso de Uso encargado de eliminar un comentario de una fuente y actualizar
 * proporcionalmente el promedio de valoraciones (rating) de la misma.
 *
 * Implementa una lógica de reversión aritmética para asegurar que la media
 * de estrellas de la fuente refleje únicamente los comentarios persistentes.
 *
 * @property repository Repositorio de fuentes que gestiona las operaciones en Firestore.
 */
class DeleteCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    /**
     * Ejecuta el proceso de eliminación y recálculo de estadísticas.
     *
     * @param fountain El objeto [Fountain] actual que contiene el total de ratings previo.
     * @param comment El objeto [Comment] que se va a eliminar (necesario para conocer su puntuación).
     * @return [Result] indicando el éxito o fallo de la operación de borrado y actualización.
     */
    suspend operator fun invoke(fountain: Fountain, comment: Comment): Result<Unit> {
        // 1. Persistencia: Borramos el comentario de la subcolección.
        // El repositorio se encarga de decrementar el contador 'totalRatings' en el documento padre.
        val result = repository.deleteComment(fountain.id, comment.id)

        return if (result.isSuccess) {
            // 2. Lógica de Negocio: Cálculo de la nueva media tras la eliminación

            // Usamos coerceAtLeast(0) por seguridad aritmética para evitar totales negativos
            val newTotal = (fountain.totalRatings - 1).coerceAtLeast(0)

            val newAverage = if (newTotal > 0) {
                // Revertimos la media: reconstruimos la suma total y restamos la nota del comentario borrado
                val currentSum = fountain.ratingAverage * fountain.totalRatings
                val rawAverage = (currentSum - comment.rating) / newTotal

                // Redondeo a 1 decimal para consistencia visual (ej: 4.2)
                round(rawAverage * 10) / 10.0
            } else {
                // Si ya no quedan comentarios, reiniciamos el promedio a cero
                0.0
            }

            // 3. Actualización Atómica: Reflejamos la nueva media en el documento de la fuente
            repository.updateFountain(
                fountain.id,
                mapOf("ratingAverage" to newAverage)
            )
        } else {
            result
        }
    }
}