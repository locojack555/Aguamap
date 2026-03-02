package cat.copernic.aguamap1.presentation.profile

import android.net.Uri
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
import cat.copernic.aguamap1.data.cloudinary.CloudinaryService
import cat.copernic.aguamap1.data.cloudinary.UploadProgress

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getCurrentUserHistoricStatsUseCase: GetCurrentUserHistoricStatsUseCase,
    private val getUserCommentsCountUseCase: GetUserCommentsCountUseCase,
    private val getUserFountainsCountUseCase: GetUserFountainsCountUseCase,
    private val cloudinaryService: CloudinaryService
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

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _isUploadingPicture = MutableStateFlow(false)
    val isUploadingPicture: StateFlow<Boolean> = _isUploadingPicture.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress.asStateFlow()

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
                val profilePicture = authRepository.getCurrentUserProfilePicture()
                val fountainsCount = getUserFountainsCountUseCase(userId)
                val commentsCount = getUserCommentsCountUseCase(userId)
                val historicStats = getCurrentUserHistoricStatsUseCase(userId)

                _profileState.update { currentState ->
                    currentState.copy(
                        userName = userName ?: currentState.userName,
                        userEmail = userEmail ?: currentState.userEmail,
                        userRole = userRole.name,
                        profilePictureUrl = profilePicture,
                        points = historicStats?.points ?: currentState.points,
                        fountainsCount = fountainsCount,
                        ratingsCount = commentsCount
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(nombre: String) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null

            try {
                val userId = authRepository.getCurrentUserUid()
                    ?: throw Exception("Usuario no autenticado")

                val currentName = authRepository.getCurrentUserName()
                val currentEmail = authRepository.getCurrentUserEmail() ?: ""

                val firestoreResult = authRepository.updateUserProfile(userId, nombre, currentEmail)
                if (firestoreResult.isFailure) {
                    throw firestoreResult.exceptionOrNull() ?: Exception("Error en Firestore")
                }

                if (nombre != currentName) {
                    val authResult = authRepository.updateUserName(nombre)
                    if (authResult.isFailure) {
                        throw authResult.exceptionOrNull() ?: Exception("Error al actualizar nombre en Firestore")
                    }
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

    // NUEVA FUNCIÓN PARA ELIMINAR FOTO DE PERFIL
    fun deleteProfilePicture(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null

            try {
                val userId = authRepository.getCurrentUserUid()
                    ?: throw Exception("Usuario no autenticado")

                // Actualizar en Firestore con URL vacía
                val result = authRepository.updateUserProfilePicture(userId, "")
                if (result.isFailure) {
                    throw result.exceptionOrNull() ?: Exception("Error al eliminar la foto")
                }

                // Recargar datos
                loadUserData()
                _isSuccess.value = true
                onComplete()

            } catch (e: Exception) {
                _error.value = "Error al eliminar foto: ${e.message}"
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

    fun updateSelectedImage(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun clearSelectedImage() {
        _selectedImageUri.value = null
    }

    fun saveProfilePicture(onComplete: () -> Unit) {
        val imageUri = _selectedImageUri.value ?: return
        val userId = authRepository.getCurrentUserUid() ?: return

        viewModelScope.launch {
            _isUploadingPicture.value = true
            _error.value = null
            _uploadProgress.value = 0

            try {
                cloudinaryService.uploadImageWithProgress(imageUri)
                    .collect { progress ->
                        when (progress) {
                            is UploadProgress.InProgress -> {
                                _uploadProgress.value = progress.percentage
                            }

                            is UploadProgress.Success -> {
                                val result = authRepository.updateUserProfilePicture(
                                    userId,
                                    progress.imageUrl
                                )
                                if (result.isFailure) {
                                    throw result.exceptionOrNull()
                                        ?: Exception("Error al guardar la foto")
                                }

                                loadUserData()
                                _isUploadingPicture.value = false
                                _selectedImageUri.value = null
                                onComplete()
                            }

                            is UploadProgress.Error -> {
                                _error.value = "Error al subir imagen: ${progress.message}"
                                _isUploadingPicture.value = false
                            }

                            else -> {}
                        }
                    }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _isUploadingPicture.value = false
            }
        }
    }
}