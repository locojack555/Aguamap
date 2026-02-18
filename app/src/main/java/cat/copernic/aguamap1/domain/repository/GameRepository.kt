package cat.copernic.aguamap1.domain.repository

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.GameSession
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    suspend fun getRandomFountain(): Result<Fountain?>
    suspend fun hasPlayedToday(userId: String): Result<Boolean>
    suspend fun saveGameSession(session: GameSession): Result<Unit>
    fun getTodaysSessions(): Flow<Result<List<GameSession>>>
}