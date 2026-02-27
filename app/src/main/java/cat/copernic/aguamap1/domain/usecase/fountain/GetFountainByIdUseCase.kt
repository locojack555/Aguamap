package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class GetFountainByIdUseCase @Inject constructor(
    private val fountainRepository: FountainRepository
) {
    suspend operator fun invoke(fountainId: String): Result<Fountain> {
        return fountainRepository.getFountainById(fountainId)
    }
}