package cat.copernic.aguamap1.domain.repository

import cat.copernic.aguamap1.domain.model.Fountain

interface FountainRepository {
    suspend fun fetchSources(): List<Fountain>
    suspend fun createFountain(fountain: Fountain): Result<Unit>
}