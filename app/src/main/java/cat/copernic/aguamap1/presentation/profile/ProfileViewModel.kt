package cat.copernic.aguamap1.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.usecase.profile.GetCurrentUserHistoricStatsUseCase
import cat.copernic.aguamap1.domain.usecase.profile.GetUserCommentsCountUseCase
import cat.copernic.aguamap1.domain.usecase.profile.GetUserFountainsCountUseCase
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
    private val getUserFountainsCountUseCase: GetUserFountainsCountUseCase
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = authRepository.getCurrentUserUid() ?: return@launch
                val userName = authRepository.getCurrentUserName()
                val userEmail = authRepository.getCurrentUserEmail()
                val userRole = authRepository.getUserRole(userId)
                val fountainsCount = getUserFountainsCountUseCase(userId)
                val commentsCount = getUserCommentsCountUseCase(userId)
                val historicStats = getCurrentUserHistoricStatsUseCase(userId)

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

    fun updateProfile(nombre: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null

            try {
                val userId = authRepository.getCurrentUserUid()
                    ?: throw Exception("Usuario no autenticado")

                val currentName = authRepository.getCurrentUserName()
                val currentEmail = authRepository.getCurrentUserEmail() ?: ""

                // Actualizar en Firestore (nombre + email actual, sin cambiar email)
                val firestoreResult = authRepository.updateUserProfile(userId, nombre, currentEmail)
                if (firestoreResult.isFailure) {
                    throw firestoreResult.exceptionOrNull() ?: Exception("Error en Firestore")
                }

                // Actualizar displayName en Auth si cambió
                if (nombre != currentName) {
                    authRepository.updateUserName(nombre)
                }

                loadUserData()
                _isSuccess.value = true

            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun resetSuccess() {
        _isSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }
}