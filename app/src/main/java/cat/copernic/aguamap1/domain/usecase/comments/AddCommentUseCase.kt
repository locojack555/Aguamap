package cat.copernic.aguamap1.domain.usecase.comments

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject
import kotlin.math.round

/**
 * Caso de Uso responsable de registrar un nuevo comentario en una fuente y
 * recalcular dinámicamente el promedio de valoraciones (rating) de la misma.
 *
 * Implementa una lógica de actualización atómica para mantener la integridad
 * entre la subcolección de comentarios y el documento principal de la fuente.
 *
 * @property repository Repositorio de fuentes que gestiona la persistencia en Firestore.
 */
class AddCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    /**
     * Ejecuta el proceso de guardado y recálculo de puntuación.
     *
     * @param fountain El objeto [Fountain] actual antes de añadir el comentario.
     * @param comment El nuevo [Comment] que el usuario desea publicar.
     * @return [Result] indicando el éxito o fallo de la operación completa.
     */
    suspend operator fun invoke(fountain: Fountain, comment: Comment): Result<Unit> {
        // 1. Añadimos el comentario a la subcolección (el repo incrementa totalRatings internamente)
        val result = repository.addComment(fountain.id, comment)

        return if (result.isSuccess) {
            // 2. Lógica de Negocio: Cálculo de la nueva media de estrellas
            val newTotal = fountain.totalRatings + 1

            // Reconstruimos la suma total de puntos antes de este comentario
            val currentSum = fountain.ratingAverage * fountain.totalRatings

            // Calculamos el nuevo promedio bruto
            val rawAverage = (currentSum + comment.rating) / newTotal

            // 3. Normalización: Redondeo a 1 decimal (ejemplo: 4.3333 -> 4.3)
            // Esto garantiza una visualización limpia en la UI (estrellas).
            val newAverage = round(rawAverage * 10) / 10.0

            // 4. Persistencia: Actualizamos solo el campo ratingAverage en el documento de la fuente
            repository.updateFountain(
                fountain.id,
                mapOf("ratingAverage" to newAverage)
            )
        } else {
            result
        }
    }
}