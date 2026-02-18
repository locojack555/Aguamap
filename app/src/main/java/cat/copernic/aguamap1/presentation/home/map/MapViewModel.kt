package cat.copernic.aguamap1.presentation.home.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.usecase.fountain.CreateFountainUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.GetDistanceFountainsUseCaseUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.GetFountainsUseCase
import cat.copernic.aguamap1.ui.map.MapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getFountainsUseCase: GetFountainsUseCase,
    private val createFountainUseCase: CreateFountainUseCase,
    private val getDistanceUseCase: GetDistanceFountainsUseCaseUseCase
) : ViewModel() {

    var latitude by mutableDoubleStateOf(41.5632)
    var longitude by mutableDoubleStateOf(2.0089)
    var zoomLevel by mutableDoubleStateOf(15.0)
    var isFirstLocationUpdate by mutableStateOf(true)
    var uiState by mutableStateOf(MapUiState())
        private set
    private val _isMapView = MutableStateFlow(true)
    val isMapView: StateFlow<Boolean> = _isMapView.asStateFlow()
    private var allFountainsList = emptyList<Fountain>()
    private var userLat: Double? = null
    private var userLng: Double? = null
    var searchQuery by mutableStateOf("")
        private set

    init {
        loadFountains()
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery = newQuery
        applyFilterAndSort()
    }

    fun toggleView() {
        _isMapView.value = !_isMapView.value
    }

    fun onMapMoved(lat: Double, lng: Double, zoom: Double) {
        latitude = lat
        longitude = lng
        zoomLevel = zoom
    }

    fun addTestFountain(isAdmin: Boolean, lat: Double, lng: Double) {
        viewModelScope.launch {
            val testFountain = Fountain(
                name = if (isAdmin) "Fuente Admin" else "Fuente Pendiente",
                latitude = lat,
                longitude = lng,
                operational = true,
                category = "BEBIBLE",
                description = "Una fuente añadida desde el botón de prueba",
                dateCreated = java.util.Date()
            )
            val result = createFountainUseCase(testFountain, isAdmin)
            result.onSuccess {
                println("Fuente añadida con éxito")
                loadFountains()
            }.onFailure {
                println("Error al añadir: ${it.message}")
            }
        }
    }

    fun loadFountains() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            getFountainsUseCase().collect { result ->
                result.onSuccess { list ->
                    allFountainsList = list
                    if (userLat != null && userLng != null) {
                        updateDistances()
                    } else {
                        applyFilterAndSort()
                    }
                    uiState = uiState.copy(isLoading = false)
                }.onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = error.message
                            ?: "Error al conectar con el servidor" //Cambiar a multilenguaje
                    )
                }
            }
        }
    }

    fun onFirstLocationFound(lat: Double, lng: Double) {
        userLat = lat
        userLng = lng
        if (isFirstLocationUpdate) {
            latitude = lat
            longitude = lng
            zoomLevel = 17.0
            isFirstLocationUpdate = false
        }
        updateDistances()
    }

    private fun updateDistances() {
        val lat = userLat ?: return
        val lng = userLng ?: return
        allFountainsList = allFountainsList.map { fountain ->
            val distance = getDistanceUseCase(lat, lng, fountain.latitude, fountain.longitude)
            fountain.copy(distanceFromUser = distance)
        }
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        val filtered = if (searchQuery.isBlank()) {
            allFountainsList
        } else {
            allFountainsList.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
        val sorted = filtered.sortedBy { it.distanceFromUser ?: Double.MAX_VALUE }
        uiState = uiState.copy(fountains = sorted)
    }

}