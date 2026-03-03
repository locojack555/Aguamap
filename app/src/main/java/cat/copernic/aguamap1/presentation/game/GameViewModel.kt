package cat.copernic.aguamap1.presentation.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.error.ErrorResourceProvider
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.GameSession
import cat.copernic.aguamap1.domain.usecase.fountain.GetFountainsUseCase
import cat.copernic.aguamap1.domain.usecase.game.*
import cat.copernic.aguamap1.presentation.music.SoundManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val getFountainsUseCase: GetFountainsUseCase,
    private val hasPlayedTodayUseCase: HasPlayedTodayUseCase,
    private val calculateScoreUseCase: CalculateScoreUseCase,
    private val calculateDistanceUseCase: CalculateDistanceUseCase,
    private val saveGameSessionUseCase: SaveGameSessionUseCase,
    private val auth: FirebaseAuth,
    private val soundManager: SoundManager,
    private val errorResourceProvider: ErrorResourceProvider
) : ViewModel() {

    private val VALIDATION_DISTANCE_METERS = 5.0
    private var checkCanPlayJob: Job? = null
    private var lastCheckTime = 0L

    private val _gameState = MutableStateFlow<GameState>(GameState.Initial)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _currentFountain = MutableStateFlow<Fountain?>(null)
    val currentFountain: StateFlow<Fountain?> = _currentFountain.asStateFlow()

    private val _remainingTime = MutableStateFlow(30)
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

    private val _hasLost = MutableStateFlow(false)
    val hasLost: StateFlow<Boolean> = _hasLost.asStateFlow()

    private val _distanceToFountain = MutableStateFlow(0.0)
    val distanceToFountain: StateFlow<Double> = _distanceToFountain.asStateFlow()

    fun checkCanPlay(userLat: Double, userLng: Double) {
        if (userLat == 0.0) return
        if (_isLoading.value) return
        if (_gameState.value != GameState.Initial && _gameState.value != GameState.TooFar) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCheckTime < 2000) return
        lastCheckTime = currentTime

        checkCanPlayJob?.cancel()
        checkCanPlayJob = viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _error.value = errorResourceProvider.getString(R.string.game_error_login_required)
                _gameState.value = GameState.Error
                _isLoading.value = false
                return@launch
            }

            try {
                val hasPlayed = hasPlayedTodayUseCase(userId).getOrDefault(false)
                if (hasPlayed) {
                    _error.value = errorResourceProvider.getString(R.string.game_error_daily_limit)
                    _gameState.value = GameState.DailyLimitReached
                    _isLoading.value = false
                    return@launch
                }

                // Usamos executeOnce() que ahora es una llamada suspendida directa, no un Flow
                val allFountains = getFountainsUseCase.executeOnce().getOrDefault(emptyList())

                if (allFountains.isEmpty()) {
                    _error.value = "No hay fuentes registradas."
                    _gameState.value = GameState.Error
                    return@launch
                }

                var minD = Double.MAX_VALUE
                var isNear = false
                for (f in allFountains) {
                    val d = calculateDistanceUseCase(userLat, userLng, f.latitude, f.longitude)
                    if (d < minD) minD = d
                    if (d <= VALIDATION_DISTANCE_METERS) { isNear = true; break }
                }

                _distanceToFountain.value = minD

                if (isNear) {
                    _currentFountain.value = allFountains.random()
                    _gameState.value = GameState.Instructions
                } else {
                    _error.value = errorResourceProvider.getString(R.string.game_error_too_far)
                    _gameState.value = GameState.TooFar
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión."
                _gameState.value = GameState.Error
            } finally { _isLoading.value = false }
        }
    }

    fun onStartGameClicked() {
        soundManager.startBackgroundMusic()
        _gameState.value = GameState.Playing
        _remainingTime.value = 30
        _score.value = 0
        _userGuessPos.value = null
        _hasLost.value = false
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_remainingTime.value > 0 && _gameState.value == GameState.Playing) {
                delay(1000)
                _remainingTime.value -= 1
            }
            if (_remainingTime.value == 0 && _gameState.value == GameState.Playing) {
                if (_userGuessPos.value == null) handleLoss() else finishGame()
            }
        }
    }

    private fun handleLoss() {
        viewModelScope.launch {
            _hasLost.value = true
            _score.value = 0
            _distance.value = 0.0
            saveLossSession()
            soundManager.playLossSound()
            _gameState.value = GameState.Finished
        }
    }

    private suspend fun saveLossSession() {
        val user = auth.currentUser ?: return
        val session = GameSession(user.uid, user.displayName ?: "User", 0, 0.0, Date(), _currentFountain.value?.id ?: "", _currentFountain.value?.name ?: "")
        saveGameSessionUseCase(session)
    }

    fun setUserGuess(lat: Double, lng: Double) { _userGuessPos.value = Pair(lat, lng) }

    fun finishGame() {
        viewModelScope.launch {
            if (_userGuessPos.value == null) { handleLoss(); return@launch }
            val f = _currentFountain.value ?: return@launch
            val g = _userGuessPos.value ?: return@launch
            val d = calculateDistanceUseCase(g.first, g.second, f.latitude, f.longitude)
            _distance.value = d
            _score.value = calculateScoreUseCase(d)
            saveSession()
            _gameState.value = GameState.Finished
        }
    }

    private suspend fun saveSession() {
        val user = auth.currentUser ?: return
        val session = GameSession(user.uid, user.displayName ?: "User", _score.value, _distance.value, Date(), _currentFountain.value?.id ?: "", _currentFountain.value?.name ?: "")
        saveGameSessionUseCase(session)
        if (_score.value > 0) soundManager.playWinSound()
    }

    fun onBackToHomePressed() { viewModelScope.launch { soundManager.stopBackgroundMusic() } }
    override fun onCleared() { soundManager.stopBackgroundMusic(); super.onCleared() }

    fun retryGame(lat: Double, lng: Double) {
        _gameState.value = GameState.Initial
        _error.value = null
        checkCanPlay(lat, lng)
    }

    fun clearGameState() {
        viewModelScope.launch {
            checkCanPlayJob?.cancel()
            _gameState.value = GameState.Initial
            _currentFountain.value = null
            _error.value = null
            soundManager.stopBackgroundMusic()
        }
    }

    sealed class GameState {
        object Initial : GameState()
        object Instructions : GameState()
        object Playing : GameState()
        object Finished : GameState()
        object DailyLimitReached : GameState()
        object Error : GameState()
        object TooFar : GameState()
    }
}