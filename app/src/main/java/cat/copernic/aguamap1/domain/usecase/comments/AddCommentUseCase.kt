package cat.copernic.aguamap1.domain.usecase.comments

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject
import kotlin.math.round

class AddCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend operator fun invoke(fountain: Fountain, comment: Comment): Result<Unit> {
        // 1. Añadimos el comentario (el repo ya suma +1 a totalRatings)
        val result = repository.addComment(fountain.id, comment)

        return if (result.isSuccess) {
            // 2. Cálculo de la nueva media
            val newTotal = fountain.totalRatings + 1

            // Calculamos la suma actual de estrellas y sumamos la nueva
            val currentSum = fountain.ratingAverage * fountain.totalRatings
            val rawAverage = (currentSum + comment.rating) / newTotal

            // 3. Redondeo a 1 decimal (ejemplo: 4.333 -> 4.3)
            // Esto evita problemas visuales y de almacenamiento
            val newAverage = round(rawAverage * 10) / 10.0

            // 4. Actualizamos la fuente
            repository.updateFountain(
                fountain.id,
                mapOf("ratingAverage" to newAverage)
            )
        } else {
            result
        }
    }
}