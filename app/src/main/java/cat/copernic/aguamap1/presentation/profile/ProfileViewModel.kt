package cat.copernic.aguamap1.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
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
import cat.copernic.aguamap1.domain.model.ProfileState

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

    // Cambiado de String? a Int? para usar IDs de recursos (R.string)
    private val _errorResId = MutableStateFlow<Int?>(null)
    val errorResId: StateFlow<Int?> = _errorResId.asStateFlow()

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
            _errorResId.value = null

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
                _errorResId.value = R.string.error_loading_user_data
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(nombre: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorResId.value = null

            try {
                val userId = authRepository.getCurrentUserUid()
                    ?: throw Exception()

                val currentName = authRepository.getCurrentUserName()
                val currentEmail = authRepository.getCurrentUserEmail() ?: ""

                val firestoreResult = authRepository.updateUserProfile(userId, nombre, currentEmail)
                if (firestoreResult.isFailure) throw Exception()

                if (nombre != currentName) {
                    authRepository.updateUserName(nombre)
                }

                loadUserData()
                _isSuccess.value = true
                onComplete()

            } catch (e: Exception) {
                _errorResId.value = R.string.error_updating_profile
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteProfilePicture(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorResId.value = null

            try {
                val userId = authRepository.getCurrentUserUid()
                    ?: throw Exception()

                val result = authRepository.updateUserProfilePicture(userId, "")
                if (result.isFailure) throw Exception()

                loadUserData()
                _isSuccess.value = true
                onComplete()

            } catch (e: Exception) {
                _errorResId.value = R.string.error_deleting_picture
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun saveProfilePicture(onComplete: () -> Unit) {
        val imageUri = _selectedImageUri.value ?: return
        val userId = authRepository.getCurrentUserUid() ?: return

        viewModelScope.launch {
            _isUploadingPicture.value = true
            _errorResId.value = null
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
                                if (result.isFailure) throw Exception()

                                loadUserData()
                                _isUploadingPicture.value = false
                                _selectedImageUri.value = null
                                onComplete()
                            }
                            is UploadProgress.Error -> {
                                _errorResId.value = R.string.error_uploading_image
                                _isUploadingPicture.value = false
                            }
                            else -> {}
                        }
                    }
            } catch (e: Exception) {
                _errorResId.value = R.string.error_generic
                _isUploadingPicture.value = false
            }
        }
    }

    fun resetSuccess() { _isSuccess.value = false }
    fun clearError() { _errorResId.value = null }
    fun updateSelectedImage(uri: Uri?) { _selectedImageUri.value = uri }
    fun clearSelectedImage() { _selectedImageUri.value = null }
}