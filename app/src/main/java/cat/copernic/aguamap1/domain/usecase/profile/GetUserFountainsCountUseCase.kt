package cat.copernic.aguamap1.domain.usecase.profile

import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class GetUserFountainsCountUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    suspend operator fun invoke(userId: String): Int {
        return repository.getUserFountainsCount(userId)
    }
}