package cat.copernic.aguamap1.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import cat.copernic.aguamap1.domain.model.UserRole
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.usecase.profile.GetCurrentUserHistoricStatsUseCase
import cat.copernic.aguamap1.domain.usecase.profile.GetUserCommentsCountUseCase
import cat.copernic.aguamap1.domain.usecase.profile.GetUserFountainsCountUseCase
import cat.copernic.aguamap1.domain.usecase.profile.ObserveUserHistoricRankingUseCase
import cat.copernic.aguamap1.domain.usecase.profile.ObserveUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getCurrentUserHistoricStatsUseCase: GetCurrentUserHistoricStatsUseCase,
    private val getUserCommentsCountUseCase: GetUserCommentsCountUseCase,
    private val getUserFountainsCountUseCase: GetUserFountainsCountUseCase,
    private val observeUserStatsUseCase: ObserveUserStatsUseCase,
    private val observeUserHistoricRankingUseCase: ObserveUserHistoricRankingUseCase
) : ViewModel() {

    // Estado del perfil
    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserData()
        observeUserStats()
        observeUserHistoricRanking()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = authRepository.getCurrentUserUid() ?: return@launch
                val userName: String? = authRepository.getCurrentUserName()
                val userEmail: String? = authRepository.getCurrentUserEmail()
                val userRole: UserRole = authRepository.getUserRole(userId)
                val historicStats = getCurrentUserHistoricStatsUseCase(userId)
                val fountainsCount = getUserFountainsCountUseCase(userId)
                val commentsCount = getUserCommentsCountUseCase(userId)

                _profileState.update { currentState ->
                    currentState.copy(
                        userName = userName ?: currentState.userName,
                        userEmail = userEmail ?: currentState.userEmail,
                        userRole = userRole.name,
                        points = historicStats?.points ?: currentState.points,
                        fountainsCount = fountainsCount,
                        ratingsCount = commentsCount
                    )
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeUserStats() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserUid() ?: return@launch

            observeUserStatsUseCase(userId).collect { stats ->
                Log.d("ProfileViewModel", "📡 Recibida actualización: $stats")

                if (stats != null) {
                    _profileState.update { currentState ->
                        currentState.copy(
                            fountainsCount = stats.fountainsCount,
                            ratingsCount = stats.commentsCount
                        )
                    }
                }
            }
        }
    }

    private fun observeUserHistoricRanking() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserUid() ?: return@launch

            observeUserHistoricRankingUseCase(userId).collect { ranking ->
                Log.d("ProfileViewModel", "📊 Ranking actualizado: $ranking")

                if (ranking != null) {
                    _profileState.update { currentState ->
                        currentState.copy(
                            points = ranking.points
                        )
                    }
                }
            }
        }
    }
}
