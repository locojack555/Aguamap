package cat.copernic.aguamap1.domain.usecase.comments

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject
import kotlin.math.round

class DeleteCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend operator fun invoke(fountain: Fountain, comment: Comment): Result<Unit> {
        // 1. Borramos el comentario (el repositorio ya resta -1 a totalRatings)
        val result = repository.deleteComment(fountain.id, comment.id)

        return if (result.isSuccess) {
            // 2. Calculamos los nuevos valores para la fuente
            val newTotal = (fountain.totalRatings - 1).coerceAtLeast(0)

            val newAverage = if (newTotal > 0) {
                // Revertimos la media: (Suma actual - nota borrada) / nuevo total
                val currentSum = fountain.ratingAverage * fountain.totalRatings
                val rawAverage = (currentSum - comment.rating) / newTotal
                // Redondeamos a 1 decimal
                round(rawAverage * 10) / 10.0
            } else {
                0.0 // Si no quedan comentarios, la media es 0
            }

            // 3. Actualizamos la fuente con la nueva media
            // Nota: totalRatings ya fue actualizado por el repositorio en el paso 1
            repository.updateFountain(
                fountain.id,
                mapOf("ratingAverage" to newAverage)
            )
        } else {
            result
        }
    }
}