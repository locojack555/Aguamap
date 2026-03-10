package cat.copernic.aguamap1.domain.model.game

import java.util.Date

/**
 * Representa el resultado individual de una partida del mini-juego.
 * Se utiliza para registrar el desempeño del usuario, calcular los puntos
 * obtenidos y alimentar los rankings diarios, mensuales e históricos.
 *
 * @property userId Identificador único (UID) del jugador que realizó la sesión.
 * @property userName Nombre del jugador en el momento de finalizar la partida.
 * @property score Puntuación obtenida (calculada en base a la cercanía de la respuesta).
 * @property distance Distancia en metros entre la ubicación marcada por el usuario y la ubicación real de la fuente.
 * @property date Fecha y hora exactas en las que se completó el desafío.
 * @property fountainId Identificador de la fuente que fue objeto del desafío.
 * @property fountainName Nombre de la fuente para mostrar en el historial de partidas.
 */
data class GameSession(
    val userId: String = "",
    val userName: String = "",
    val score: Int = 0,
    val distance: Double = 0.0,
    val date: Date = Date(),
    val fountainId: String = "",
    val fountainName: String = ""
)