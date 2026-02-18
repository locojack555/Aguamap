package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFountainsUseCase @Inject constructor(private val repository: FountainRepository) {

    operator fun invoke(): Flow<Result<List<Fountain>>> {
        return repository.fetchSources()
    }
}