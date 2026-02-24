package cat.copernic.aguamap1.presentation.fountain.addFountain

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.data.cloudinary.CloudinaryService
import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.usecase.category.GetCategoriesUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.CreateFountainUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class AddFountainViewModel @Inject constructor(
    private val createFountainUseCase: CreateFountainUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val cloudinaryService: CloudinaryService // AÑADIDO
) : ViewModel() {

    // --- ESTADO DE NAVEGACIÓN ---
    var isAdding by mutableStateOf(false)
        private set

    var selectedLocationForNewFountain by mutableStateOf<GeoPoint?>(null)
        private set

    // --- ESTADO DEL FORMULARIO ---
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var selectedCategory by mutableStateOf<Category?>(null)
    var isOperational by mutableStateOf(true)

    // NUEVO: Estado para la imagen
    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    var isUploading by mutableStateOf(false)
        private set

    var uploadProgress by mutableStateOf(0)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    val isFormValid: Boolean
        get() = name.isNotBlank() && description.isNotBlank() && selectedCategory != null

    init {
        viewModelScope.launch {
            getCategoriesUseCase().collect { lista ->
                categories = lista
            }
        }
    }

    fun openAddFountain(lat: Double, lng: Double) {
        selectedLocationForNewFountain = GeoPoint(lat, lng)
        isAdding = true
    }

    fun closeAddFountain() {
        resetForm()
        isAdding = false
        selectedLocationForNewFountain = null
    }

    private fun resetForm() {
        name = ""
        description = ""
        selectedCategory = null
        isOperational = true
        selectedImageUri = null
        isUploading = false
        uploadProgress = 0
        errorMessage = null
    }

    // NUEVO: Actualizar URI de imagen
    fun updateSelectedImage(uri: Uri?) {
        selectedImageUri = uri
    }

    // NUEVO: Limpiar imagen seleccionada
    fun clearSelectedImage() {
        selectedImageUri = null
    }

    fun addNewFountain(onSuccess: () -> Unit) {
        val location = selectedLocationForNewFountain ?: return
        val category = selectedCategory ?: return

        viewModelScope.launch {
            isUploading = true
            errorMessage = null

            try {
                // 1. Subir imagen si hay una seleccionada
                val imageUrl = if (selectedImageUri != null) {
                    // Suscribirse al progreso
                    cloudinaryService.uploadImageWithProgress(selectedImageUri!!)
                        .collect { progress ->
                            when (progress) {
                                is cat.copernic.aguamap1.data.cloudinary.UploadProgress.InProgress -> {
                                    uploadProgress = progress.percentage
                                }
                                is cat.copernic.aguamap1.data.cloudinary.UploadProgress.Success -> {
                                    uploadProgress = 100
                                    // Continuar con la creación
                                }
                                is cat.copernic.aguamap1.data.cloudinary.UploadProgress.Error -> {
                                    errorMessage = "Error al subir imagen: ${progress.message}"
                                    isUploading = false
                                    return@collect
                                }
                                else -> {}
                            }
                        }

                    // En un caso real, aquí esperarías la URL. Simplificamos:
                    // Alternativa: usar uploadImage suspendido
                    cloudinaryService.uploadImage(selectedImageUri!!).getOrNull()
                } else {
                    null
                }

                // 2. Crear la fuente con la URL de la imagen
                val newFountain = Fountain(
                    name = name,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    operational = isOperational,
                    category = category,
                    description = description,
                    imageUrl = imageUrl ?: "" // Guardamos la URL
                )

                val result = createFountainUseCase(newFountain, isUserAdmin = false)

                result.onSuccess {
                    onSuccess()
                    closeAddFountain()
                }.onFailure {
                    errorMessage = it.message
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isUploading = false
            }
        }
    }
}