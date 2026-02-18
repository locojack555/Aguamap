package cat.copernic.aguamap1.domain.repository

import cat.copernic.aguamap1.domain.model.Fountain
import kotlinx.coroutines.flow.Flow

interface FountainRepository {
    fun fetchSources(): Flow<Result<List<Fountain>>>
    suspend fun createFountain(fountain: Fountain): Result<Unit>
}