package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFountainsUseCase @Inject constructor(private val repository: FountainRepository) {

    operator fun invoke(lat: Double?, lng: Double?): Flow<Result<List<Fountain>>> {
        return repository.fetchSources(lat, lng)
    }

    // Llamada directa sin Flow para el juego
    suspend fun executeOnce(): Result<List<Fountain>> {
        return repository.getAllFountainsDirect()
    }

    fun fetchComments(fountainId: String): Flow<Result<List<Comment>>> {
        return repository.fetchComments(fountainId)
    }
}