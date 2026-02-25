package cat.copernic.aguamap1.domain.usecase.comments

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject
import kotlin.math.round

class UpdateCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend operator fun invoke(
        fountain: Fountain,
        oldComment: Comment,
        newRating: Int,
        newText: String
    ): Result<Unit> {
        // 1. Preparamos los cambios del comentario
        val updates = mapOf(
            "rating" to newRating,
            "comment" to newText,
            "reported" to false, // Limpiamos el reporte si el usuario edita
            "timestamp" to System.currentTimeMillis()
        )

        // 2. Actualizamos el comentario en la subcolección
        val result = repository.updateComment(fountain.id, oldComment.id, updates)

        return if (result.isSuccess) {
            // 3. Si la nota ha cambiado, recalculamos la media de la fuente
            if (oldComment.rating != newRating) {
                val total = fountain.totalRatings
                if (total > 0) {
                    // Fórmula: (Suma actual - nota vieja + nota nueva) / total
                    val currentSum = fountain.ratingAverage * total
                    val rawAverage = (currentSum - oldComment.rating + newRating) / total
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