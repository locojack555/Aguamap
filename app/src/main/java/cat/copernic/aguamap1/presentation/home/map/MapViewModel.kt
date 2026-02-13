package cat.copernic.aguamap1.presentation.home.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.usecase.fountain.CreateFountainUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.GetFountainsUseCase
import cat.copernic.aguamap1.ui.map.MapUiState
import kotlinx.coroutines.launch

class MapViewModel(
    private val getFountainsUseCase: GetFountainsUseCase,
    private val createFountainUseCase: CreateFountainUseCase
) : ViewModel() {
    var latitude by mutableDoubleStateOf(41.5632)
    var longitude by mutableDoubleStateOf(2.0089)
    var zoomLevel by mutableDoubleStateOf(15.0)
    var isFirstLocationUpdate by mutableStateOf(true)
    var uiState by mutableStateOf(MapUiState())
        private set
    var fountains by mutableStateOf<List<Fountain>>(emptyList())
        private set

    init {
        loadFountains()
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
                isOperational = true,
                category = "Bebible",
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
            // Llamamos al caso de uso (gracias a 'invoke')
            getFountainsUseCase().onSuccess { list ->
                uiState = uiState.copy(fountains = list, isLoading = false)
            }.onFailure {
                uiState = uiState.copy(isLoading = false, errorMessage = it.message)
            }
        }
    }

    fun onFirstLocationFound(lat: Double, lng: Double) {
        if (isFirstLocationUpdate) {
            latitude = lat
            longitude = lng
            zoomLevel = 17.0
            isFirstLocationUpdate = false
        }
    }
}