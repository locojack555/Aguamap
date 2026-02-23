package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.GameSession
import cat.copernic.aguamap1.domain.repository.GameRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
            val snapshot = db.collection("gameSessions")
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
            // Guardar la sesión diaria
            db.collection("gameSessions").add(session).await()

            // Actualizar estadísticas mensuales
            val monthlyResult = updateMonthlyStats(
                userId = session.userId,
                userName = session.userName,
                score = session.score,
                discovered = 1
            )

            // Actualizar histórico
            val historicResult = updateHistoricStats(
                userId = session.userId,
                userName = session.userName,
                score = session.score,
                discovered = 1
            )

            if (monthlyResult.isSuccess && historicResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(
                    monthlyResult.exceptionOrNull()
                        ?: historicResult.exceptionOrNull()
                        ?: Exception("Error actualizando estadísticas")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMonthlyStats(
        userId: String,
        userName: String,
        score: Int,
        discovered: Int
    ): Result<Unit> {
        return try {
            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1 // Enero es 0, por eso +1
            val year = calendar.get(Calendar.YEAR)

            // El ID único asegura que cada usuario tenga solo UN doc por mes
            val docId = "${userId}_${month}_${year}"
            val monthlyRef = db.collection("monthlyRanking").document(docId)

            val data = mapOf(
                "userId" to userId,
                "userName" to userName,
                "totalScore" to FieldValue.increment(score.toLong()),
                "gamesCount" to FieldValue.increment(1),
                "totalDiscovered" to FieldValue.increment(discovered.toLong()),
                "month" to month,
                "year" to year
            )

            // Si no existe el doc lo crea, si existe solo suma los valores
            monthlyRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateHistoricStats(
        userId: String,
        userName: String,
        score: Int,
        discovered: Int
    ): Result<Unit> {
        return try {

            val docId = userId
            val historicRef = db.collection("historicRanking").document(docId)

            val data = mapOf(
                "userId" to userId,
                "userName" to userName,
                "totalScore" to FieldValue.increment(score.toLong()),
                "gamesCount" to FieldValue.increment(1),
                "totalDiscovered" to FieldValue.increment(discovered.toLong())
            )

            historicRef.set(data, SetOptions.merge()).await()

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

        val subscription = db.collection("gameSessions")
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