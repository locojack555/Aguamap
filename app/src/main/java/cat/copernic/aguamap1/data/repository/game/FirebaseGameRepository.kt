package cat.copernic.aguamap1.data.repository.game

import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.domain.model.game.GameSession
import cat.copernic.aguamap1.domain.repository.game.GameRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject

/**
 * Implementación del repositorio de juego utilizando Firebase Firestore.
 * Gestiona la selección de fuentes aleatorias para el modo de juego, el control
 * de participación diaria y la actualización de los rankings mensuales e históricos.
 */
class FirebaseGameRepository @Inject constructor(
    private val db: FirebaseFirestore // Instancia de Firestore para acceder a las colecciones de juego
) : GameRepository {

    /**
     * Selecciona una fuente al azar de la base de datos para el desafío de juego.
     * @return Result con una fuente aleatoria o null si la colección está vacía.
     */
    override suspend fun getRandomFountain(): Result<Fountain?> {
        return try {
            val snapshot = db.collection("fountains").get().await()
            val fountains = snapshot.toObjects(Fountain::class.java)
            // Devuelve un elemento aleatorio de la lista obtenida
            Result.success(fountains.randomOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifica si un usuario ya ha participado en el juego durante el día actual.
     * Consulta las sesiones del usuario y compara la fecha del sistema con la guardada.
     * @param userId Identificador único del usuario.
     */
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
                // Comparamos año y día del año para determinar si es hoy
                cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            }
            Result.success(hasPlayed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Guarda una nueva sesión de juego y dispara la actualización de estadísticas.
     * Coordina la inserción en la colección de sesiones y las actualizaciones en los rankings.
     * @param session Objeto con los datos de la partida finalizada.
     */
    override suspend fun saveGameSession(session: GameSession): Result<Unit> {
        return try {
            // 1. Guardar el registro individual de la sesión diaria
            db.collection("gameSessions").add(session).await()

            // 2. Actualizar las estadísticas del mes en curso
            val monthlyResult = updateMonthlyStats(
                userId = session.userId,
                userName = session.userName,
                score = session.score,
                discovered = 1
            )

            // 3. Actualizar las estadísticas globales del histórico
            val historicResult = updateHistoricStats(
                userId = session.userId,
                userName = session.userName,
                score = session.score,
                discovered = 1
            )

            // Combinar los resultados de las tres operaciones
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

    /**
     * Actualiza el ranking mensual del usuario.
     * Utiliza un ID de documento compuesto (UID_MES_AÑO) para que cada usuario tenga
     * un único registro por mes y las puntuaciones se acumulen.
     */
    override suspend fun updateMonthlyStats(
        userId: String,
        userName: String,
        score: Int,
        discovered: Int
    ): Result<Unit> {
        return try {
            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)

            val docId = "${userId}_${month}_${year}"
            val monthlyRef = db.collection("monthlyRanking").document(docId)

            val data = mapOf(
                "userId" to userId,
                "userName" to userName,
                // FieldValue.increment garantiza que la suma sea atómica en el servidor
                "totalScore" to FieldValue.increment(score.toLong()),
                "gamesCount" to FieldValue.increment(1),
                "totalDiscovered" to FieldValue.increment(discovered.toLong()),
                "month" to month,
                "year" to year
            )

            // SetOptions.merge() permite actualizar campos o crear el documento si no existe
            monthlyRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza el ranking histórico global del usuario.
     * A diferencia del mensual, el documento está vinculado únicamente al UID del usuario.
     */
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

    /**
     * Recupera las sesiones de juego realizadas durante el día actual en tiempo real.
     * @return Flow con la lista de sesiones ordenadas por puntuación descendente.
     */
    override fun getTodaysSessions(): Flow<Result<List<GameSession>>> = callbackFlow {
        // Configuramos el rango temporal de "hoy" (desde las 00:00:00 hasta las 23:59:59)
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

        // Escuchamos cambios en la colección filtrando por el rango de fecha actual
        val subscription = db.collection("gameSessions")
            .whereGreaterThanOrEqualTo("date", today)
            .whereLessThanOrEqualTo("date", tomorrow)
            .orderBy("score", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val sessions = snapshot?.toObjects(GameSession::class.java) ?: emptyList()
                trySend(Result.success(sessions))
            }

        // Cerramos el listener de Firestore al cancelar el flujo
        awaitClose { subscription.remove() }
    }
}