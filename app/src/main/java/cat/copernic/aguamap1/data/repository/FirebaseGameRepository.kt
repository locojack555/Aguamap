package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.GameSession
import cat.copernic.aguamap1.domain.repository.GameRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class FirebaseGameRepository @Inject constructor(
    private val db: FirebaseFirestore
) : GameRepository {

    override suspend fun getRandomFountain(): Result<Fountain?> {
        return try {
            val snapshot = db.collection("fountains").get().await()
            val fountains = snapshot.toObjects(Fountain::class.java)
            Result.success(fountains.randomOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasPlayedToday(userId: String): Result<Boolean> {
        return try {
            val snapshot = db.collection("game_sessions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val today = Calendar.getInstance()
            val hasPlayed = snapshot.documents.any { doc ->
                val sessionDate = doc.getDate("date") ?: return@any false
                val cal = Calendar.getInstance().apply { time = sessionDate }
                cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            }
            Result.success(hasPlayed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveGameSession(session: GameSession): Result<Unit> {
        return try {
            db.collection("game_sessions").add(session).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override  fun getTodaysSessions(): Flow<Result<List<GameSession>>> = callbackFlow {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val tomorrow = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        val subscription = db.collection("game_sessions")
            .whereGreaterThanOrEqualTo("date", today)
            .whereLessThanOrEqualTo("date", tomorrow)
            .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val sessions = snapshot?.toObjects(GameSession::class.java) ?: emptyList()
                trySend(Result.success(sessions))
            }
        awaitClose { subscription.remove() }
    }
}