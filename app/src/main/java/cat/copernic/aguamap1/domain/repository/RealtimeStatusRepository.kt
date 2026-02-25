package cat.copernic.aguamap1.domain.repository

import kotlinx.coroutines.flow.Flow

interface RealtimeStatusRepository {
    fun getFountainStatus(fountainId: String): Flow<Boolean>
    fun updateFountainStatus(fountainId: String, isOperational: Boolean)
}