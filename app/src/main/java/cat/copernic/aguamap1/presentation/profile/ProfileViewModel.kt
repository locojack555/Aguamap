package cat.copernic.aguamap1.presentation.profile

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

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    private val _hasPendingVerification = MutableStateFlow(false)
    val hasPendingVerification: StateFlow<Boolean> = _hasPendingVerification.asStateFlow()

    init {
        loadUserData()
        observeUserStats()
        observeUserHistoricRanking()
    }

    fun loadUserData(onAutoLogout: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Refrescamos para obtener último estado
                authRepository.refreshUser()

                // Obtenemos datos actualizados
                val userId = authRepository.getCurrentUserUid() ?: return@launch
                val newAuthEmail = authRepository.getCurrentUserEmailAuth()
                val newFirestoreEmail = authRepository.getCurrentUserEmail()
                val isVerifiedNow = authRepository.isEmailVerified()

                // Determinamos si hay pendiente AHORA
                val hasPendingNow = (newFirestoreEmail != newAuthEmail) &&
                        !newAuthEmail.isNullOrBlank()

                // Actualizamos la bandera de pendiente
                _hasPendingVerification.value = hasPendingNow

                // Cargamos el resto de datos
                val userName = authRepository.getCurrentUserName()
                val userRole = authRepository.getUserRole(userId)

                val fountainsCount = getUserFountainsCountUseCase(userId)
                val commentsCount = getUserCommentsCountUseCase(userId)
                val historicStats = getCurrentUserHistoricStatsUseCase(userId)

                // Actualizamos el estado
                _profileState.update { currentState ->
                    currentState.copy(
                        userName = userName ?: currentState.userName,
                        userEmail = newFirestoreEmail ?: currentState.userEmail,
                        authEmail = newAuthEmail ?: currentState.authEmail,
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

    // 🔥 NUEVA FUNCIÓN: Se llama cuando el usuario pulsa el botón
    fun checkVerificationAndLogout(onVerified: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Refrescamos para obtener el último estado
                authRepository.refreshUser()

                val isVerifiedNow = authRepository.isEmailVerified()
                val authEmail = authRepository.getCurrentUserEmailAuth()
                val firestoreEmail = authRepository.getCurrentUserEmail()

                // 🎯 REGLA: Si está verificado y los emails coinciden
                if (isVerifiedNow && authEmail == firestoreEmail) {
                    // Cerramos sesión y redirigimos
                    authRepository.signOut()
                    onVerified()
                } else {
                    // Si no está verificado, recargamos datos normal
                    loadUserData()
                }
            } catch (e: Exception) {
                _error.value = "Error al verificar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(nombre: String, email: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null


            try {
                val userId = authRepository.getCurrentUserUid()
                    ?: throw Exception("Usuario no autenticado")

                val currentUser = authRepository.getCurrentUserEmail()
                val currentName = authRepository.getCurrentUserName()

                // 1. Actualizar en Firestore (siempre)
                val firestoreResult = authRepository.updateUserProfile(userId, nombre, email)
                if (firestoreResult.isFailure) {
                    throw firestoreResult.exceptionOrNull() ?: Exception("Error en Firestore")
                }

                // 2. Actualizar en Authentication SOLO si cambiaron
                val authUpdates = mutableListOf<Result<Unit>>()

                // Si el nombre cambió
                if (nombre != currentName) {
                    authUpdates.add(authRepository.updateUserName(nombre))
                }

                // Si el email cambió
                if (email != currentUser) {
                    authUpdates.add(authRepository.updateUserEmail(email))
                }

                // Verificar si alguna actualización de Authentication falló
                val authErrors = authUpdates.filter { it.isFailure }
                if (authErrors.isNotEmpty()) {
                    // Al menos una actualización de Auth falló, pero Firestore ya se actualizó
                    val errorMessages = authErrors.mapNotNull { it.exceptionOrNull()?.message }
                    throw Exception("Perfil actualizado en Firestore pero no en Authentication: ${errorMessages.joinToString()}")
                }

                loadUserData()
                _isSuccess.value = true

            } catch (e: Exception) {
                val errorMessage = e.message ?: ""

                when {
                    // CASO 1: Error de email duplicado (No se guarda nada)
                    errorMessage.contains("email address is already in use") -> {
                        _error.value = "El correo electrónico ya está en uso por otra cuenta."
                    }

                    // CASO 2: Sesión antigua (No se guarda el email, pero quizás sí el nombre)
                    errorMessage.contains("requires recent authentication") -> {
                        _error.value = "Por seguridad, cierra sesión y vuelve a entrar para cambiar el email."
                    }

                    // CASO 3: EL ESTADO DE VERIFICACIÓN (Lo que querías para la Opción B)
                    // Si llegamos aquí con este mensaje, Firestore ya se actualizó.
                    errorMessage.contains("Perfil actualizado en Firestore pero no en Authentication") -> {
                        _error.value = "Nombre actualizado. Revisa tu bandeja de entrada para confirmar el nuevo email."
                        _isSuccess.value = true // Esto cierra la pantalla de edición y vuelve al perfil
                    }

                    // CASO 4: Otros errores (Internet, permisos, etc.)
                    else -> {
                        _error.value = "Error al actualizar: ${e.message}"
                    }
                }
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

    private fun observeUserStats() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserUid() ?: return@launch

            observeUserStatsUseCase(userId).collect { stats ->

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
