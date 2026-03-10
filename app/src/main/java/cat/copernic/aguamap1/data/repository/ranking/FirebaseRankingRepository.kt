package cat.copernic.aguamap1.data.repository.ranking

import cat.copernic.aguamap1.domain.model.game.GameSession
import cat.copernic.aguamap1.domain.model.ranking.RankingPeriod
import cat.copernic.aguamap1.domain.model.ranking.UserRanking
import cat.copernic.aguamap1.domain.repository.auth.AuthRepository
import cat.copernic.aguamap1.domain.repository.ranking.RankingRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio encargado de generar las clasificaciones (rankings) de los jugadores.
 * Implementa lógica de agregación manual para el ranking diario y anual, y consultas
 * directas para el mensual basándose en los documentos pre-calculados en Firestore.
 */
@Singleton
class FirebaseRankingRepository @Inject constructor(
    private val authRepository: AuthRepository, // Para identificar al usuario actual en la lista
    private val db: FirebaseFirestore // Para acceder a las colecciones de sesiones y rankings
) : RankingRepository {

    /**
     * Genera el ranking del día actual.
     * Recupera todas las sesiones de juego desde las 00:00 de hoy y agrupa los puntos por usuario.
     */
    override suspend fun getDailyRanking(): List<UserRanking> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val inicioHoy = calendar.time
        val idUsuarioActual = authRepository.getCurrentUserUid()

        // Consulta sesiones creadas a partir de la medianoche de hoy
        val snapshot = db.collection("gameSessions")
            .whereGreaterThanOrEqualTo("date", inicioHoy)
            .get()
            .await()

        return snapshot.documents
            .mapNotNull { doc ->
                try {
                    // Mapeo manual de documento a GameSession
                    GameSession(
                        userId = doc.getString("userId") ?: return@mapNotNull null,
                        userName = doc.getString("userName") ?: "",
                        score = doc.getLong("score")?.toInt() ?: 0,
                        date = doc.getDate("date") ?: Date(),
                        fountainId = doc.getString("fountainId") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            // Agrupamos sesiones por ID de usuario para sumar sus puntos diarios
            .groupBy { it.userId }
            .map { (userId, sesiones) ->
                UserRanking(
                    position = 0,
                    name = sesiones.firstOrNull()?.userName?.takeIf { it.isNotBlank() }
                        ?: "Jugador",
                    points = sesiones.sumOf { it.score },
                    discovered = sesiones.distinctBy { it.fountainId }.size, // Fuentes únicas descubiertas hoy
                    games = sesiones.size,
                    isCurrentUser = userId == idUsuarioActual
                )
            }
            .sortedByDescending { it.points } // Ordenamos de mayor a menor puntuación
            .take(10) // Limitamos a los 10 mejores resultados diarios para la UI
            .mapIndexed { index, usuario -> usuario.copy(position = index + 1) }
    }

    /**
     * Recupera el ranking mensual pre-calculado.
     * A diferencia del diario, este método consulta una colección específica donde los datos ya
     * están agregados, lo que mejora drásticamente el rendimiento y reduce lecturas de Firestore.
     */
    override suspend fun getMonthlyRanking(): List<UserRanking> {
        val calendar = Calendar.getInstance()
        val mes = calendar.get(Calendar.MONTH) + 1
        val año = calendar.get(Calendar.YEAR)
        val idUsuarioActual = authRepository.getCurrentUserUid()

        // Consulta directa sobre la colección de estadísticas mensuales
        val snapshot = db.collection("monthlyRanking")
            .whereEqualTo("month", mes)
            .whereEqualTo("year", año)
            .orderBy("totalScore", Query.Direction.DESCENDING)
            .limit(10) // Limitamos la consulta en el servidor
            .get()
            .await()

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

    /**
     * Genera el ranking anual.
     * Suma todos los documentos mensuales del año actual para cada usuario.
     */
    override suspend fun getYearlyRanking(): List<UserRanking> {
        val calendar = Calendar.getInstance()
        val añoActual = calendar.get(Calendar.YEAR)
        val idUsuarioActual = authRepository.getCurrentUserUid()

        // Obtenemos todos los registros mensuales del año en curso
        val snapshot = db.collection("monthlyRanking")
            .whereEqualTo("year", añoActual)
            .get()
            .await()

        return snapshot.documents
            .groupBy { it.getString("userId") ?: "" }
            .map { (userId, documentos) ->
                // Agregamos los datos de todos los meses del año para este usuario
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
            .take(10) // Top 10 anual
            .mapIndexed { index, usuario -> usuario.copy(position = index + 1) }
    }

    /**
     * Función de conveniencia que redirige al método de ranking correspondiente según el período.
     */
    override suspend fun getRankingByPeriod(period: RankingPeriod): List<UserRanking> {
        return when (period) {
            RankingPeriod.DAY -> getDailyRanking()
            RankingPeriod.MONTH -> getMonthlyRanking()
            RankingPeriod.YEAR -> getYearlyRanking()
        }
    }

    /**
     * Obtiene las estadísticas históricas globales de un usuario específico.
     * Se utiliza principalmente para mostrar el progreso total en la pantalla de perfil.
     */
    override suspend fun getCurrentUserHistoricRanking(userId: String): UserRanking? {
        return try {
            val doc = db.collection("historicRanking")
                .document(userId)
                .get()
                .await()

            if (!doc.exists()) return null

            UserRanking(
                position = 0,
                name = doc.getString("userName") ?: "Jugador",
                points = doc.getLong("totalScore")?.toInt() ?: 0,
                discovered = doc.getLong("totalDiscovered")?.toInt() ?: 0,
                games = doc.getLong("gamesCount")?.toInt() ?: 0,
                isCurrentUser = true // Identificamos que es el perfil del usuario actual
            )
        } catch (e: Exception) {
            null
        }
    }
}