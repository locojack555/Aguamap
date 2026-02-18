package cat.copernic.aguamap1.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.GameSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.math.*

class GameViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _gameState = MutableStateFlow<GameState>(GameState.Initial)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _currentFountain = MutableStateFlow<Fountain?>(null)
    val currentFountain: StateFlow<Fountain?> = _currentFountain.asStateFlow()

    private val _remainingTime = MutableStateFlow(60)
    val remainingTime: StateFlow<Int> = _remainingTime.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _distance = MutableStateFlow(0.0)
    val distance: StateFlow<Double> = _distance.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _userGuessPos = MutableStateFlow<Pair<Double, Double>?>(null)
    val userGuessPos: StateFlow<Pair<Double, Double>?> = _userGuessPos.asStateFlow()

    fun checkCanPlay(userLat: Double, userLng: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _error.value = "Inicia sesión para jugar"
                _gameState.value = GameState.Error
                _isLoading.value = false
                return@launch
            }

            try {
                val sessionsSnapshot = db.collection("game_sessions").get().await()
                val today = Calendar.getInstance()

                val hasPlayedToday = sessionsSnapshot.documents.any { doc ->
                    val sessionUserId = doc.getString("userId")
                    val sessionDate = doc.getDate("date")

                    if (sessionUserId == userId && sessionDate != null) {
                        val cal = Calendar.getInstance().apply { time = sessionDate }
                        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                    } else false
                }

                /*if (hasPlayedToday) {
                    _error.value = "Ya has jugado hoy. ¡Vuelve mañana!"
                    _gameState.value = GameState.DailyLimitReached
                    _isLoading.value = false
                    return@launch
                }*/

                val fountainsSnapshot = db.collection("fountains").get().await()
                val fountains = fountainsSnapshot.toObjects(Fountain::class.java)

                if (fountains.isEmpty()) {
                    _error.value = "No hay fuentes disponibles"
                    _gameState.value = GameState.Error
                } else {
                    // Preparamos la fuente pero vamos a la pantalla de Instrucciones
                    _currentFountain.value = fountains.random()
                    _gameState.value = GameState.Instructions
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _gameState.value = GameState.Error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onStartGameClicked() {
        _gameState.value = GameState.Playing
        _remainingTime.value = 60
        _score.value = 0
        _userGuessPos.value = null
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_remainingTime.value > 0 && _gameState.value == GameState.Playing) {
                delay(1000)
                _remainingTime.value -= 1
            }
            if (_remainingTime.value == 0 && _gameState.value == GameState.Playing) {
                finishGame()
            }
        }
    }

    fun setUserGuess(lat: Double, lng: Double) {
        _userGuessPos.value = Pair(lat, lng)
    }

    fun finishGame() {
        viewModelScope.launch {
            val fountain = _currentFountain.value ?: return@launch
            val guess = _userGuessPos.value ?: Pair(0.0, 0.0)
            val dist = calculateDistance(guess.first, guess.second, fountain.latitude, fountain.longitude)
            _distance.value = dist
            _score.value = calculateScore(dist)

            saveSession()
            _gameState.value = GameState.Finished
        }
    }

    private suspend fun saveSession() {
        val user = auth.currentUser ?: return
        val userName = user.displayName ?: "Jugador"
        val currentScore = _score.value
        val session = GameSession(
            userId = user.uid,
            userName = userName,
            score = currentScore,
            distance = _distance.value,
            date = Date(),
            fountainId = _currentFountain.value?.id ?: "",
            fountainName = _currentFountain.value?.name ?: ""
        )
        try {
            db.collection("game_sessions").add(session).await()

            // Actualiza el acumulado mensual
            updateMonthlyStats(user.uid, userName, currentScore)
        } catch (e: Exception) { }
    }

    private suspend fun updateMonthlyStats(userId: String, userName: String, score: Int) {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1 // Enero es 0
        val year = calendar.get(Calendar.YEAR)

        // El ID único asegura que cada usuario tenga solo UN doc por mes
        val docId = "${userId}_${month}_${year}"
        val monthlyRef = db.collection("monthlyRanking").document(docId)

        val data = mapOf(
            "userId" to userId,
            "userName" to userName,
            "totalScore" to com.google.firebase.firestore.FieldValue.increment(score.toLong()),
            "gamesCount" to com.google.firebase.firestore.FieldValue.increment(1),
            "totalDiscovered" to com.google.firebase.firestore.FieldValue.increment(1),
            "month" to month,
            "year" to year
        )

        try {
            //si no existe el doc lo crea, si existe solo suma los valores
            monthlyRef.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {}
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3
        val phi1 = lat1 * PI / 180
        val phi2 = lat2 * PI / 180
        val dPhi = (lat2 - lat1) * PI / 180
        val dLambda = (lon2 - lon1) * PI / 180
        val a = sin(dPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun calculateScore(dist: Double): Int {
        return when {
            dist < 50 -> 1000
            dist < 100 -> 800
            dist < 500 -> 500
            dist < 1000 -> 200
            else -> 50
        }
    }

    fun retryGame() {
        _gameState.value = GameState.Initial
        _error.value = null
    }

    sealed class GameState {
        object Initial : GameState()
        object Instructions : GameState()
        object Playing : GameState()
        object Finished : GameState()
        object DailyLimitReached : GameState()
        object Error : GameState()
    }
}