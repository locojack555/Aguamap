package cat.copernic.aguamap1.domain.usecase.comments

import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class CensorCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend operator fun invoke(fountainId: String, commentId: String): Result<Unit> {
        val updates = mapOf(
            "censored" to true,
            "reported" to false
        )
        return repository.updateComment(fountainId, commentId, updates)
    }
}