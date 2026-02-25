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

    fun checkCanPlay(userLat: Double, userLng: Double) {
        // Guarda la ubicación del usuario
        _userLocation.value = Pair(userLat, userLng)

        viewModelScope.launch {
            _isLoading.value = true
            val userId = auth.currentUser?.uid

            if (userId == null) {
                _error.value = errorResourceProvider.getString(R.string.game_error_login_required)
                _gameState.value = GameState.Error
                _isLoading.value = false
                return@launch
            }

            try {
                val hasPlayedResult = hasPlayedTodayUseCase(userId)
                if (hasPlayedResult.isFailure) {
                    _error.value = errorResourceProvider.getString(R.string.game_error_checking_session)
                    _gameState.value = GameState.Error
                    _isLoading.value = false
                    return@launch
                }

                /*if (hasPlayedResult.getOrNull() == true) {
                    _error.value = errorResourceProvider.getString(R.string.game_error_daily_limit)
                    _gameState.value = GameState.DailyLimitReached
                    _isLoading.value = false
                    return@launch
                }*/

                val fountainResult = getRandomFountainUseCase()
                if (fountainResult.isFailure) {
                    _error.value = errorResourceProvider.getString(R.string.game_error_loading_fountains)
                    _gameState.value = GameState.Error
                    _isLoading.value = false
                    return@launch
                }

                val fountain = fountainResult.getOrNull()
                if (fountain == null) {
                    _error.value = errorResourceProvider.getString(R.string.game_error_no_fountains)
                    _gameState.value = GameState.Error
                } else {
                    _currentFountain.value = fountain
                    _gameState.value = GameState.Instructions
                }
            } catch (e: Exception) {
                _error.value = errorResourceProvider.getString(R.string.game_error_generic, e.message ?: "")
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
                if (_userGuessPos.value == null) {
                    handleLoss()
                } else {
                    finishGame()
                }
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
            userName = user.displayName ?: "Jugador",
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

            val dist = calculateDistanceUseCase(
                guess.first,
                guess.second,
                fountain.latitude,
                fountain.longitude
            )
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
            userName = user.displayName ?: "Jugador",
            score = _score.value,
            distance = _distance.value,
            date = Date(),
            fountainId = _currentFountain.value?.id ?: "",
            fountainName = _currentFountain.value?.name ?: ""
        )
        saveGameSessionUseCase(session)

        if (_score.value > 0) {
            soundManager.playWinSound()
        }
    }

    fun onBackToHomePressed() {
        viewModelScope.launch {
            soundManager.stopBackgroundMusic()
            _error.value = errorResourceProvider.getString(R.string.game_error_daily_limit)
            _gameState.value = GameState.DailyLimitReached
        }
    }

    override fun onCleared() {
        soundManager.stopBackgroundMusic()
        super.onCleared()
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