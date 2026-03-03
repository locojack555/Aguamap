package cat.copernic.aguamap1.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.error.ErrorResourceProvider
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.GameSession
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
    private val getRandomFountainUseCase: GetRandomFountainUseCase,
    private val hasPlayedTodayUseCase: HasPlayedTodayUseCase,
    private val calculateScoreUseCase: CalculateScoreUseCase,
    private val calculateDistanceUseCase: CalculateDistanceUseCase,
    private val saveGameSessionUseCase: SaveGameSessionUseCase,
    private val auth: FirebaseAuth,
    private val soundManager: SoundManager,
    private val errorResourceProvider: ErrorResourceProvider
) : ViewModel() {

    private val MAX_PLAY_DISTANCE_METERS = 5.0

    private var checkCanPlayJob: Job? = null
    private var lastCheckTime = 0L

    private val _gameState = MutableStateFlow<GameState>(GameState.Initial)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _currentFountain = MutableStateFlow<Fountain?>(null)
    val currentFountain: StateFlow<Fountain?> = _currentFountain.asStateFlow()

    private val _remainingTime = MutableStateFlow(10)
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

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation.asStateFlow()

    private val _distanceToFountain = MutableStateFlow(0.0)
    val distanceToFountain: StateFlow<Double> = _distanceToFountain.asStateFlow()

    fun checkCanPlay(userLat: Double, userLng: Double) {
        if ((userLat == 41.5632 && userLng == 2.0089) || userLat == 0.0) return
        if (_isLoading.value) return
        if (_gameState.value != GameState.Initial && _gameState.value != GameState.TooFar) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCheckTime < 2000) return
        lastCheckTime = currentTime

        checkCanPlayJob?.cancel()

        checkCanPlayJob = viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            _userLocation.value = Pair(userLat, userLng)

            val userId = auth.currentUser?.uid
            if (userId == null) {
                _error.value = errorResourceProvider.getString(R.string.game_error_login_required)
                _gameState.value = GameState.Error
                _isLoading.value = false
                return@launch
            }

            try {
                delay(50)

                val hasPlayedResult = hasPlayedTodayUseCase(userId)
                if (hasPlayedResult.getOrDefault(false)) {
                    _error.value = errorResourceProvider.getString(R.string.game_error_daily_limit)
                    _gameState.value = GameState.DailyLimitReached
                    _isLoading.value = false
                    return@launch
                }

                val fountainResult = getRandomFountainUseCase()
                val fountain = fountainResult.getOrNull()

                if (fountain == null) {
                    _error.value = errorResourceProvider.getString(R.string.game_error_no_fountains)
                    _gameState.value = GameState.Error
                } else {
                    val dist = calculateDistanceUseCase(userLat, userLng, fountain.latitude, fountain.longitude)
                    _distanceToFountain.value = dist

                    if (dist > MAX_PLAY_DISTANCE_METERS) {
                        _currentFountain.value = null
                        // CAMBIO: Ahora usa stringResource a través del provider
                        _error.value = errorResourceProvider.getString(R.string.game_error_too_far)
                        _gameState.value = GameState.TooFar
                    } else {
                        _currentFountain.value = fountain
                        _gameState.value = GameState.Instructions
                    }
                }
            } catch (e: Exception) {
                // CAMBIO: No concatenamos e.message para mantener el idioma puro del recurso
                _error.value = errorResourceProvider.getString(R.string.game_error_generic)
                _gameState.value = GameState.Error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onStartGameClicked() {
        soundManager.startBackgroundMusic()
        _gameState.value = GameState.Playing
        _remainingTime.value = 10
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
        val session = GameSession(
            userId = user.uid,
            userName = user.displayName ?: errorResourceProvider.getString(R.string.game_default_user_name),
            score = 0,
            distance = 0.0,
            date = Date(),
            fountainId = _currentFountain.value?.id ?: "",
            fountainName = _currentFountain.value?.name ?: ""
        )
        saveGameSessionUseCase(session)
    }

    fun setUserGuess(lat: Double, lng: Double) {
        _userGuessPos.value = Pair(lat, lng)
    }

    fun finishGame() {
        viewModelScope.launch {
            if (_userGuessPos.value == null) {
                handleLoss()
                return@launch
            }

            val fountain = _currentFountain.value ?: return@launch
            val guess = _userGuessPos.value ?: return@launch

            val dist = calculateDistanceUseCase(guess.first, guess.second, fountain.latitude, fountain.longitude)
            _distance.value = dist
            _score.value = calculateScoreUseCase(dist)

            saveSession()
            _gameState.value = GameState.Finished
        }
    }

    private suspend fun saveSession() {
        val user = auth.currentUser ?: return
        val session = GameSession(
            userId = user.uid,
            userName = user.displayName ?: errorResourceProvider.getString(R.string.game_default_user_name),
            score = _score.value,
            distance = _distance.value,
            date = Date(),
            fountainId = _currentFountain.value?.id ?: "",
            fountainName = _currentFountain.value?.name ?: ""
        )
        saveGameSessionUseCase(session)
        if (_score.value > 0) soundManager.playWinSound()
    }

    fun onBackToHomePressed() {
        viewModelScope.launch {
            soundManager.stopBackgroundMusic()
            // No cambiamos a DailyLimitReached aquí si solo es navegación,
            // pero mantenemos tu lógica original de estados.
        }
    }

    override fun onCleared() {
        soundManager.stopBackgroundMusic()
        super.onCleared()
    }

    fun retryGame(userLat: Double, userLng: Double) {
        checkCanPlayJob?.cancel()
        _isLoading.value = false
        _gameState.value = GameState.Initial
        _error.value = null
        _currentFountain.value = null
        lastCheckTime = 0L
        checkCanPlay(userLat, userLng)
    }

    fun clearGameState() {
        viewModelScope.launch {
            checkCanPlayJob?.cancel()
            lastCheckTime = 0L
            _isLoading.value = false
            _gameState.value = GameState.Initial
            _currentFountain.value = null
            _remainingTime.value = 10
            _score.value = 0
            _distance.value = 0.0
            _error.value = null
            _userGuessPos.value = null
            _hasLost.value = false
            _userLocation.value = null
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