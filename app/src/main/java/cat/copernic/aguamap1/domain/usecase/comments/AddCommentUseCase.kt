package cat.copernic.aguamap1.domain.usecase.comments

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend operator fun invoke(fountain: Fountain, comment: Comment): Result<Unit> {
        // 1. Añadimos el comentario.
        // Recuerda: El repositorio ya suma +1 a 'totalRatings' internamente.
        val result = repository.addComment(fountain.id, comment)

        return if (result.isSuccess) {
            // 2. Calculamos la nueva media de estrellas
            val newTotal = fountain.totalRatings + 1
            val newAverage =
                ((fountain.ratingAverage * fountain.totalRatings) + comment.rating) / newTotal

            // 3. Actualizamos solo el promedio.
            // NO enviamos "totalRatings" aquí porque ya lo hizo el repository.addComment
            repository.updateFountain(
                fountain.id,
                mapOf("ratingAverage" to newAverage)
            )
        } else {
            result
        }
    }
}