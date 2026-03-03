package cat.copernic.aguamap1.presentation.fountain.addFountain

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.data.cloudinary.CloudinaryService
import cat.copernic.aguamap1.data.cloudinary.UploadProgress
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
    private val cloudinaryService: CloudinaryService,
    application: Application // Inyección para multiidioma
) : AndroidViewModel(application) {

    private fun getString(resId: Int): String = getApplication<Application>().getString(resId)

    // --- ESTADOS ---
    var isAdding by mutableStateOf(false) ; private set
    var selectedLocationForNewFountain by mutableStateOf<GeoPoint?>(null) ; private set
    var fountainToEdit by mutableStateOf<Fountain?>(null) ; private set

    // --- FORMULARIO ---
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var selectedCategory by mutableStateOf<Category?>(null)
    var isOperational by mutableStateOf(true)

    // --- COORDENADAS ---
    var useGpsLocation by mutableStateOf(true)
    var manualLatitude by mutableStateOf("")
    var manualLongitude by mutableStateOf("")
    var latitudeError by mutableStateOf<String?>(null)
    var longitudeError by mutableStateOf<String?>(null)

    // --- IMAGEN ---
    var selectedImageUri by mutableStateOf<Uri?>(null) ; private set
    var currentImageUrl by mutableStateOf("") ; private set
    var isUploading by mutableStateOf(false) ; private set
    var uploadProgress by mutableStateOf(0) ; private set

    var errorMessage by mutableStateOf<String?>(null) ; private set
    var categories by mutableStateOf<List<Category>>(emptyList()) ; private set

    val isFormValid: Boolean
        get() = name.isNotBlank() &&
                description.isNotBlank() &&
                selectedCategory != null &&
                (!isUploading) &&
                (useGpsLocation || validateManualCoordinates())

    init {
        viewModelScope.launch {
            getCategoriesUseCase().collect { categories = it }
        }
    }

    private fun validateManualCoordinates(): Boolean {
        val lat = manualLatitude.toDoubleOrNull()
        val lng = manualLongitude.toDoubleOrNull()

        // LOCALIZACIÓN DE ERRORES DE COORDENADAS
        latitudeError = when {
            manualLatitude.isBlank() -> getString(R.string.error_lat_required)
            lat == null -> getString(R.string.error_invalid_format)
            lat < -90 || lat > 90 -> getString(R.string.error_lat_range)
            else -> null
        }

        longitudeError = when {
            manualLongitude.isBlank() -> getString(R.string.error_lng_required)
            lng == null -> getString(R.string.error_invalid_format)
            lng < -180 || lng > 180 -> getString(R.string.error_lng_range)
            else -> null
        }

        return latitudeError == null && longitudeError == null
    }

    fun openAddFountain(lat: Double, lng: Double, fountain: Fountain? = null) {
        if (fountain != null) {
            fountainToEdit = fountain
            name = fountain.name
            description = fountain.description
            selectedCategory = fountain.category
            isOperational = fountain.operational
            currentImageUrl = fountain.imageUrl
            useGpsLocation = false
            manualLatitude = fountain.latitude.toString()
            manualLongitude = fountain.longitude.toString()
            selectedLocationForNewFountain = GeoPoint(fountain.latitude, fountain.longitude)
        } else {
            resetForm()
            selectedLocationForNewFountain = GeoPoint(lat, lng)
        }
        isAdding = true
    }

    fun saveFountain(onSuccess: () -> Unit) {
        val location = if (useGpsLocation) {
            selectedLocationForNewFountain
        } else {
            val lat = manualLatitude.toDoubleOrNull()
            val lng = manualLongitude.toDoubleOrNull()
            if (lat != null && lng != null) GeoPoint(lat, lng) else null
        }

        if (location == null) {
            errorMessage = getString(R.string.error_location_invalid)
            return
        }

        val category = selectedCategory ?: return

        viewModelScope.launch {
            isUploading = true
            errorMessage = null

            try {
                var imageUrl = currentImageUrl

                selectedImageUri?.let { uri ->
                    cloudinaryService.uploadImageWithProgress(uri).collect { progress ->
                        if (progress is UploadProgress.InProgress) {
                            uploadProgress = progress.percentage
                        }
                    }
                    val uploadResult = cloudinaryService.uploadImage(uri)
                    imageUrl = uploadResult.getOrThrow()
                }

                if (fountainToEdit != null) {
                    val updatedFields = mapOf(
                        "name" to name,
                        "description" to description,
                        "category" to category,
                        "operational" to isOperational,
                        "imageUrl" to imageUrl,
                        "latitude" to location.latitude,
                        "longitude" to location.longitude
                    )
                    updateFountainUseCase(fountainToEdit!!.id, updatedFields).getOrThrow()
                } else {
                    val newFountain = Fountain(
                        name = name,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        operational = isOperational,
                        category = category,
                        description = description,
                        imageUrl = imageUrl
                    )
                    createFountainUseCase(newFountain, isUserAdmin = false).getOrThrow()
                }
                onSuccess()
                closeAddFountain()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: getString(R.string.error_email_generic)
            } finally {
                isUploading = false
            }
        }
    }

    fun toggleLocationSource() {
        useGpsLocation = !useGpsLocation
        if (!useGpsLocation && selectedLocationForNewFountain != null) {
            manualLatitude = selectedLocationForNewFountain!!.latitude.toString()
            manualLongitude = selectedLocationForNewFountain!!.longitude.toString()
        }
    }

    fun updateManualLatitude(v: String) { manualLatitude = v; latitudeError = null }
    fun updateManualLongitude(v: String) { manualLongitude = v; longitudeError = null }
    fun updateSelectedImage(uri: Uri?) { selectedImageUri = uri }
    fun clearSelectedImage() { selectedImageUri = null; currentImageUrl = "" }
    fun closeAddFountain() { resetForm(); isAdding = false }

    private fun resetForm() {
        name = ""; description = ""; selectedCategory = null; isOperational = true
        useGpsLocation = true; manualLatitude = ""; manualLongitude = ""
        latitudeError = null; longitudeError = null; selectedImageUri = null
        currentImageUrl = ""; isUploading = false; uploadProgress = 0
        errorMessage = null; fountainToEdit = null
    }
}