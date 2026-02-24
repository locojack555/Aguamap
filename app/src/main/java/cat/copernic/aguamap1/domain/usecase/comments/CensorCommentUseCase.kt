package cat.copernic.aguamap1.domain.usecase.comments

import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class CensorCommentUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend operator fun invoke(fountainId: String, commentId: String): Result<Unit> {
        val updates = mapOf(
            "isCensored" to true,
            "isReported" to false // Limpiamos el reporte porque el Admin ya tomó acción
        )
        return repository.updateComment(fountainId, commentId, updates)
    }
}