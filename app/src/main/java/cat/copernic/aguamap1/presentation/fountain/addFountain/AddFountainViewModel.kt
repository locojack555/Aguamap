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
import cat.copernic.aguamap1.domain.usecase.fountain.UpdateFountainUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class AddFountainViewModel @Inject constructor(
    private val createFountainUseCase: CreateFountainUseCase,
    private val updateFountainUseCase: UpdateFountainUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val cloudinaryService: CloudinaryService
) : ViewModel() {

    // --- ESTADO DE NAVEGACIÓN Y EDICIÓN ---
    var isAdding by mutableStateOf(false)
        private set

    var selectedLocationForNewFountain by mutableStateOf<GeoPoint?>(null)
        private set

    var fountainToEdit by mutableStateOf<Fountain?>(null)
        private set

    // --- ESTADO DEL FORMULARIO ---
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var selectedCategory by mutableStateOf<Category?>(null)
    var isOperational by mutableStateOf(true)

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    var currentImageUrl by mutableStateOf("")
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

    fun openAddFountain(lat: Double, lng: Double, fountain: Fountain? = null) {
        if (fountain != null) {
            fountainToEdit = fountain
            name = fountain.name
            description = fountain.description
            selectedCategory = fountain.category
            isOperational = fountain.operational
            currentImageUrl = fountain.imageUrl
            selectedLocationForNewFountain = GeoPoint(fountain.latitude, fountain.longitude)
        } else {
            resetForm()
            selectedLocationForNewFountain = GeoPoint(lat, lng)
        }
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
        currentImageUrl = ""
        isUploading = false
        uploadProgress = 0
        errorMessage = null
        fountainToEdit = null
    }

    fun updateSelectedImage(uri: Uri?) {
        selectedImageUri = uri
    }

    fun clearSelectedImage() {
        selectedImageUri = null
        currentImageUrl = ""
    }

    /**
     * Guarda la fuente: Crea una nueva o actualiza la existente si fountainToEdit != null.
     */
    fun saveFountain(onSuccess: () -> Unit) {
        val location = selectedLocationForNewFountain ?: return
        val category = selectedCategory ?: return

        viewModelScope.launch {
            isUploading = true
            errorMessage = null

            try {
                var imageUrl = currentImageUrl

                if (selectedImageUri != null) {
                    cloudinaryService.uploadImageWithProgress(selectedImageUri!!)
                        .collect { progress ->
                            if (progress is cat.copernic.aguamap1.data.cloudinary.UploadProgress.InProgress) {
                                uploadProgress = progress.percentage
                            }
                        }

                    val uploadResult = cloudinaryService.uploadImage(selectedImageUri!!)
                    uploadResult.onSuccess { imageUrl = it }
                }

                if (fountainToEdit != null) {
                    // --- MODO ACTUALIZAR ---
                    // IMPORTANTE: Mantenemos el status original de la fuente (ACCEPTED o PENDING)
                    val updatedFields = mutableMapOf<String, Any>(
                        "name" to name,
                        "description" to description,
                        "category" to category,
                        "operational" to isOperational,
                        "imageUrl" to imageUrl,
                        "status" to fountainToEdit!!.status // <--- CORRECCIÓN: Mantiene el estado confirmado
                    )

                    updateFountainUseCase(fountainToEdit!!.id, updatedFields)
                        .onSuccess {
                            onSuccess()
                            closeAddFountain()
                        }
                        .onFailure { errorMessage = it.message }
                } else {
                    // --- MODO CREAR ---
                    val newFountain = Fountain(
                        name = name,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        operational = isOperational,
                        category = category,
                        description = description,
                        imageUrl = imageUrl
                        // Aquí no hace falta poner status porque el modelo/DB suele
                        // asignar PENDING por defecto al crear.
                    )

                    createFountainUseCase(newFountain, isUserAdmin = false)
                        .onSuccess {
                            onSuccess()
                            closeAddFountain()
                        }
                        .onFailure { errorMessage = it.message }
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isUploading = false
            }
        }
    }
}