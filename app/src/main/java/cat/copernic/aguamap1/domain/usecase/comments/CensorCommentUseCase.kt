package cat.copernic.aguamap1.domain.usecase.comments

import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class CensorCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend operator fun invoke(fountainId: String, commentId: String): Result<Unit> {
        val updates = mapOf(
            "comment" to "",
            "isCensored" to true
        )
        return repository.updateComment(fountainId, commentId, updates)
    }
}