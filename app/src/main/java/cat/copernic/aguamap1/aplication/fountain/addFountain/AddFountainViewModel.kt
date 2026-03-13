package cat.copernic.aguamap1.aplication.fountain.addFountain

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
import cat.copernic.aguamap1.domain.model.category.Category
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.domain.usecase.category.GetCategoriesUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.CreateFountainUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.UpdateFountainUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

/**
 * ViewModel encargado de la lógica de creación y edición de fuentes.
 * Extiende de AndroidViewModel para acceder al contexto y gestionar strings multiidioma.
 */
@HiltViewModel
class AddFountainViewModel @Inject constructor(
    private val createFountainUseCase: CreateFountainUseCase,
    private val updateFountainUseCase: UpdateFountainUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val cloudinaryService: CloudinaryService,
    application: Application // Permite usar getString() para errores localizados
) : AndroidViewModel(application) {

    // Función auxiliar para obtener recursos de texto de forma sencilla
    private fun getString(resId: Int): String = getApplication<Application>().getString(resId)

    // --- ESTADOS DE CONTROL DE UI ---
    // Controlan la visibilidad del formulario y el objeto que se está editando
    var isAdding by mutableStateOf(false); private set
    var selectedLocationForNewFountain by mutableStateOf<GeoPoint?>(null); private set
    var fountainToEdit by mutableStateOf<Fountain?>(null); private set

    // --- ESTADOS DEL FORMULARIO ---
    // Observables por Compose para actualizar la UI en tiempo real
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var selectedCategory by mutableStateOf<Category?>(null)
    var isOperational by mutableStateOf(true)

    // --- LÓGICA DE COORDENADAS ---
    var useGpsLocation by mutableStateOf(true)
    var manualLatitude by mutableStateOf("")
    var manualLongitude by mutableStateOf("")
    var latitudeError by mutableStateOf<String?>(null)
    var longitudeError by mutableStateOf<String?>(null)

    // --- ESTADOS DE IMAGEN Y CLOUDINARY ---
    var selectedImageUri by mutableStateOf<Uri?>(null); private set
    var currentImageUrl by mutableStateOf(""); private set // URL existente si es edición
    var isUploading by mutableStateOf(false); private set
    var uploadProgress by mutableStateOf(0); private set

    //var moreInformation by mutableStateOf("")

    // Mensajes de error generales y lista de categorías cargada desde BD
    var errorMessage by mutableStateOf<String?>(null); private set
    var categories by mutableStateOf<List<Category>>(emptyList()); private set

    /**
     * Propiedad computada que valida si el formulario puede enviarse.
     * Verifica campos vacíos, carga de imágenes y validez de coordenadas.
     */
    val isFormValid: Boolean
        get() = name.isNotBlank() &&
                description.isNotBlank() &&
                selectedCategory != null &&
                (!isUploading) &&
                (useGpsLocation || validateManualCoordinates())

    init {
        // Carga inicial de las categorías disponibles (Potable, No potable, etc.)
        viewModelScope.launch {
            getCategoriesUseCase().collect { categories = it }
        }
    }

    /**
     * Valida que las coordenadas manuales sean números válidos y estén en rangos geográficos.
     */
    private fun validateManualCoordinates(): Boolean {
        val lat = manualLatitude.toDoubleOrNull()
        val lng = manualLongitude.toDoubleOrNull()

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

    /**
     * Prepara el ViewModel para mostrar el formulario.
     * Si recibe una fuente, rellena los campos para editar. Si no, resetea para crear nueva.
     */
    fun openAddFountain(lat: Double, lng: Double, fountain: Fountain? = null) {
        if (fountain != null) {
            fountainToEdit = fountain
            name = fountain.name
            description = fountain.description
            //moreInformation = fountain.moreInformation
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

    /**
     * Lógica principal de guardado.
     * 1. Determina la ubicación final.
     * 2. Sube la imagen a Cloudinary (si hay una nueva seleccionada).
     * 3. Crea o actualiza en Firestore según corresponda.
     */
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

                // Subida de imagen a la nube
                selectedImageUri?.let { uri ->
                    // Monitoreo del progreso de subida
                    cloudinaryService.uploadImageWithProgress(uri).collect { progress ->
                        if (progress is UploadProgress.InProgress) {
                            uploadProgress = progress.percentage
                        }
                    }
                    // Obtención de la URL final tras la subida
                    val uploadResult = cloudinaryService.uploadImage(uri)
                    imageUrl = uploadResult.getOrThrow()
                }

                if (fountainToEdit != null) {
                    // Modo Edición: Se mandan solo los campos actualizados
                    val updatedFields = mapOf(
                        "name" to name,
                        "description" to description,
                        //"moreInformation" to moreInformation,
                        "category" to category,
                        "operational" to isOperational,
                        "imageUrl" to imageUrl,
                        "latitude" to location.latitude,
                        "longitude" to location.longitude
                    )
                    updateFountainUseCase(fountainToEdit!!.id, updatedFields).getOrThrow()
                } else {
                    // Modo Creación: Se instancia un nuevo objeto Fountain
                    val newFountain = Fountain(
                        name = name,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        operational = isOperational,
                        category = category,
                        description = description,
                        //moreInformation = moreInformation,
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

    /**
     * Alterna entre el GPS y la entrada manual, sincronizando valores iniciales.
     */
    fun toggleLocationSource() {
        useGpsLocation = !useGpsLocation
        if (!useGpsLocation && selectedLocationForNewFountain != null) {
            manualLatitude = selectedLocationForNewFountain!!.latitude.toString()
            manualLongitude = selectedLocationForNewFountain!!.longitude.toString()
        }
    }

    // Funciones de actualización de estado sencillas
    fun updateManualLatitude(v: String) {
        manualLatitude = v; latitudeError = null
    }

    fun updateManualLongitude(v: String) {
        manualLongitude = v; longitudeError = null
    }

    fun updateSelectedImage(uri: Uri?) {
        selectedImageUri = uri
    }

    fun clearSelectedImage() {
        selectedImageUri = null; currentImageUrl = ""
    }

    fun closeAddFountain() {
        resetForm(); isAdding = false
    }

    /**
     * Limpia todos los estados para dejar el formulario vacío.
     */
    private fun resetForm() {
        name = ""; description = ""; name = ""; description = ""; //moreInformation = "";
        useGpsLocation = true; manualLatitude = ""; manualLongitude = ""
        latitudeError = null; longitudeError = null; selectedImageUri = null
        currentImageUrl = ""; isUploading = false; uploadProgress = 0
        errorMessage = null; fountainToEdit = null
    }
}