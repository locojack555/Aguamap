package cat.copernic.aguamap1.presentation.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import cat.copernic.aguamap1.R
import java.util.Calendar

class RankingViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(RankingState())
    val state: StateFlow<RankingState> = _state

    fun cargarRankingsReales(periodoResId: Int) {
        _state.value = RankingState(players = emptyList(), isLoading = true)
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                val listaRanking = when (periodoResId) {
                    R.string.ranking_day -> obtenerRankingDiario()
                    R.string.ranking_month -> obtenerRankingMensual()
                    R.string.ranking_year -> obtenerRankingAnual()
                    else -> emptyList()
                }

                _state.value = RankingState(players = listaRanking, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun obtenerRankingDiario(): List<UserRanking> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val inicioHoy = calendar.time

        val snapshot = db.collection("game_sessions")
            .whereGreaterThanOrEqualTo("date", inicioHoy)
            .get()
            .await()

        val idUsuarioActual = auth.currentUser?.uid

        return snapshot.toObjects(GameSession::class.java)
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

    private suspend fun obtenerRankingMensual(): List<UserRanking> {
        val calendar = Calendar.getInstance()
        val mes = calendar.get(Calendar.MONTH) + 1
        val año = calendar.get(Calendar.YEAR)

        val snapshot = db.collection("monthlyRanking")
            .whereEqualTo("month", mes)
            .whereEqualTo("year", año)
            .orderBy("totalScore", Query.Direction.DESCENDING)
            .limit(50) // Limitamos para mejorar el rendimiento
            .get()
            .await()

        val idUsuarioActual = auth.currentUser?.uid

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

    private suspend fun obtenerRankingAnual(): List<UserRanking> {
        val calendar = Calendar.getInstance()
        val añoActual = calendar.get(Calendar.YEAR)
        val idUsuarioActual = auth.currentUser?.uid

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
}