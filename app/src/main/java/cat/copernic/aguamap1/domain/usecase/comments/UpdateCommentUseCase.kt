package cat.copernic.aguamap1.domain.usecase.comments

import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class UpdateCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend operator fun invoke(
        fountainId: String,
        commentId: String,
        newRating: Int,
        newText: String
    ): Result<Unit> {
        val updates = mapOf(
            "rating" to newRating,
            "comment" to newText,
            "timestamp" to System.currentTimeMillis() // Opcional: actualizar fecha de edición
        )

        return repository.updateComment(fountainId, commentId, updates)
    }
}