package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.GameSession
import cat.copernic.aguamap1.domain.model.RankingPeriod
import cat.copernic.aguamap1.domain.model.UserRanking
import cat.copernic.aguamap1.domain.repository.RankingRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRankingRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : RankingRepository {

    override suspend fun getDailyRanking(): List<UserRanking> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val inicioHoy = calendar.time

        val snapshot = db.collection("gameSessions")
            .whereGreaterThanOrEqualTo("date", inicioHoy)
            .get()
            .await()

        val idUsuarioActual = getCurrentUserId()

        return snapshot.documents
            .mapNotNull { doc ->
                try {
                    GameSession(
                        userId = doc.getString("userId") ?: return@mapNotNull null,
                        userName = doc.getString("userName") ?: "",
                        score = doc.getLong("score")?.toInt() ?: 0,
                        date = doc.getDate("date")?: Date(),
                        fountainId = doc.getString("fountainId") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            .groupBy { it.userId }
            .map { (userId, sesiones) ->
                UserRanking(
                    position = 0,
                    name = sesiones.firstOrNull()?.userName?.takeIf { it.isNotBlank() } ?: "Jugador",
                    points = sesiones.sumOf { it.score },
                    discovered = sesiones.distinctBy { it.fountainId }.size,
                    games = sesiones.size,
                    isCurrentUser = userId == idUsuarioActual
                )
            }
            .sortedByDescending { it.points }
            .mapIndexed { index, usuario -> usuario.copy(position = index + 1) }
    }

    override suspend fun getMonthlyRanking(): List<UserRanking> {
        val calendar = Calendar.getInstance()
        val mes = calendar.get(Calendar.MONTH) + 1
        val año = calendar.get(Calendar.YEAR)

        val snapshot = db.collection("monthlyRanking")
            .whereEqualTo("month", mes)
            .whereEqualTo("year", año)
            .orderBy("totalScore", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()

        val idUsuarioActual = getCurrentUserId()

        return snapshot.documents.mapIndexed { index, doc ->
            UserRanking(
                position = index + 1,
                name = doc.getString("userName")?.takeIf { it.isNotBlank() } ?: "Jugador",
                points = doc.getLong("totalScore")?.toInt() ?: 0,
                discovered = doc.getLong("totalDiscovered")?.toInt() ?: 0,
                games = doc.getLong("gamesCount")?.toInt() ?: 0,
                isCurrentUser = doc.getString("userId") == idUsuarioActual
            )
        }
    }

    override suspend fun getYearlyRanking(): List<UserRanking> {
        val calendar = Calendar.getInstance()
        val añoActual = calendar.get(Calendar.YEAR)
        val idUsuarioActual = getCurrentUserId()

        val snapshot = db.collection("monthlyRanking")
            .whereEqualTo("year", añoActual)
            .get()
            .await()

        return snapshot.documents
            .groupBy { it.getString("userId") ?: "" }
            .map { (userId, documentos) ->
                val puntosTotales = documentos.sumOf { it.getLong("totalScore") ?: 0L }.toInt()
                val partidasTotales = documentos.sumOf { it.getLong("gamesCount") ?: 0L }.toInt()
                val fuentesDescubiertas = documentos.sumOf { it.getLong("totalDiscovered") ?: 0L }.toInt()
                val nombre = documentos.firstOrNull()?.getString("userName")?.takeIf { it.isNotBlank() } ?: "Jugador"

                UserRanking(
                    position = 0,
                    name = nombre,
                    points = puntosTotales,
                    discovered = fuentesDescubiertas,
                    games = partidasTotales,
                    isCurrentUser = userId == idUsuarioActual
                )
            }
            .sortedByDescending { it.points }
            .mapIndexed { index, usuario -> usuario.copy(position = index + 1) }
    }

    override suspend fun getRankingByPeriod(period: RankingPeriod): List<UserRanking> {
        return when (period) {
            RankingPeriod.DAY -> getDailyRanking()
            RankingPeriod.MONTH -> getMonthlyRanking()
            RankingPeriod.YEAR -> getYearlyRanking()
        }
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid
}