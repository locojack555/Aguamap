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
import java.text.NumberFormat
import java.util.Locale
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

    // --- NUEVOS CAMPOS PARA COORDENADAS MANUALES ---
    var useGpsLocation by mutableStateOf(true) // true = usar GPS, false = manual
    var manualLatitude by mutableStateOf("")
    var manualLongitude by mutableStateOf("")
    var latitudeError by mutableStateOf<String?>(null)
    var longitudeError by mutableStateOf<String?>(null)

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
        get() = name.isNotBlank() &&
                description.isNotBlank() &&
                selectedCategory != null &&
                (useGpsLocation || validateManualCoordinates())

    init {
        viewModelScope.launch {
            getCategoriesUseCase().collect { lista ->
                categories = lista
            }
        }
    }

    private fun validateManualCoordinates(): Boolean {
        return try {
            val lat = manualLatitude.toDoubleOrNull()
            val lng = manualLongitude.toDoubleOrNull()

            latitudeError = when {
                manualLatitude.isBlank() -> "La latitud es obligatoria"
                lat == null -> "Formato de latitud inválido"
                lat < -90 || lat > 90 -> "La latitud debe estar entre -90 y 90"
                else -> null
            }

            longitudeError = when {
                manualLongitude.isBlank() -> "La longitud es obligatoria"
                lng == null -> "Formato de longitud inválido"
                lng < -180 || lng > 180 -> "La longitud debe estar entre -180 y 180"
                else -> null
            }

            latitudeError == null && longitudeError == null
        } catch (e: Exception) {
            false
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
            useGpsLocation = false // Al editar, usar coordenadas manuales
            manualLatitude = fountain.latitude.toString()
            manualLongitude = fountain.longitude.toString()
            selectedLocationForNewFountain = GeoPoint(fountain.latitude, fountain.longitude)
        } else {
            resetForm()
            useGpsLocation = true
            manualLatitude = ""
            manualLongitude = ""
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
        useGpsLocation = true
        manualLatitude = ""
        manualLongitude = ""
        latitudeError = null
        longitudeError = null
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

    fun toggleLocationSource() {
        useGpsLocation = !useGpsLocation
        if (!useGpsLocation && selectedLocationForNewFountain != null) {
            // Precargar las coordenadas actuales del GPS como punto de partida
            manualLatitude = selectedLocationForNewFountain!!.latitude.toString()
            manualLongitude = selectedLocationForNewFountain!!.longitude.toString()
        }
        latitudeError = null
        longitudeError = null
    }

    fun updateManualLatitude(value: String) {
        manualLatitude = value
        latitudeError = null
    }

    fun updateManualLongitude(value: String) {
        manualLongitude = value
        longitudeError = null
    }

    /**
     * Guarda la fuente: Crea una nueva o actualiza la existente si fountainToEdit != null.
     */
    fun saveFountain(onSuccess: () -> Unit) {
        val location = if (useGpsLocation) {
            selectedLocationForNewFountain
        } else {
            try {
                val lat = manualLatitude.toDoubleOrNull()
                val lng = manualLongitude.toDoubleOrNull()
                if (lat != null && lng != null) {
                    GeoPoint(lat, lng)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

        if (location == null) {
            errorMessage = if (useGpsLocation)
                "No se ha podido obtener la ubicación GPS"
            else
                "Coordenadas manuales inválidas"
            return
        }

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
                    val updatedFields = mutableMapOf<String, Any>(
                        "name" to name,
                        "description" to description,
                        "category" to category,
                        "operational" to isOperational,
                        "imageUrl" to imageUrl,
                        "latitude" to location.latitude,
                        "longitude" to location.longitude,
                        "status" to fountainToEdit!!.status
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