package cat.copernic.aguamap1.domain.repository

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.GameSession
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Dominio que define el motor de juego de AguaMap.
 * Gestiona la lógica de las partidas diarias, el control de participación
 * única por jornada y la persistencia de estadísticas acumulativas.
 */
interface GameRepository {

    /**
     * Selecciona una fuente aleatoria del inventario total para el desafío.
     * @return Result con una [Fountain] al azar o null si no hay fuentes disponibles.
     */
    suspend fun getRandomFountain(): Result<Fountain?>

    /**
     * Verifica si el usuario ya ha completado su partida diaria.
     * Esta regla de negocio asegura que el ranking diario sea justo para todos.
     * @param userId Identificador del usuario a consultar.
     * @return Result con true si ya jugó hoy, false en caso contrario.
     */
    suspend fun hasPlayedToday(userId: String): Result<Boolean>

    /**
     * Registra el resultado de una partida finalizada.
     * Este método debe coordinar internamente la actualización de los rankings.
     * @param session Objeto con los datos de la sesión (puntos, fecha, usuario).
     */
    suspend fun saveGameSession(session: GameSession): Result<Unit>

    /**
     * Escucha en tiempo real las sesiones de juego realizadas por todos los usuarios hoy.
     * Permite mostrar un tablero de resultados del día actualizado al instante.
     * @return Flow con la lista de sesiones diarias ordenadas por puntuación.
     */
    fun getTodaysSessions(): Flow<Result<List<GameSession>>>

    /**
     * Acumula los puntos y descubrimientos del usuario en el ranking del mes actual.
     * @param userId UID del jugador.
     * @param userName Nombre para mostrar en el ranking.
     * @param score Puntos obtenidos en la sesión.
     * @param discovered Cantidad de fuentes descubiertas (usualmente 1 por sesión exitosa).
     */
    suspend fun updateMonthlyStats(
        userId: String,
        userName: String,
        score: Int,
        discovered: Int
    ): Result<Unit>

    /**
     * Actualiza el registro histórico global del usuario (sin límite de tiempo).
     * @param userId UID del jugador.
     * @param userName Nombre del jugador.
     * @param score Puntos a sumar al total histórico.
     * @param discovered Fuentes a sumar al total de descubrimientos históricos.
     */
    suspend fun updateHistoricStats(
        userId: String,
        userName: String,
        score: Int,
        discovered: Int
    ): Result<Unit>
}