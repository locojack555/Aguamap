package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFountainsUseCase @Inject constructor(private val repository: FountainRepository) {

    // Para la lista de fuentes del mapa
    operator fun invoke(): Flow<Result<List<Fountain>>> {
        return repository.fetchSources()
    }

    // NUEVO: Para obtener los comentarios de una fuente específica
    fun fetchComments(fountainId: String): Flow<Result<List<Comment>>> {
        return repository.fetchComments(fountainId)
    }
}