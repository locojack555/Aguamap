package cat.copernic.aguamap1.presentation.maps.mapView

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.data.location.DefaultLocationProvider
import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.usecase.category.GetCategoriesUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.GetDistanceFountainsUseCaseUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.GetFountainsUseCase
import cat.copernic.aguamap1.domain.usecase.validation.generateSearchRegex
import cat.copernic.aguamap1.presentation.util.FilterState
import cat.copernic.aguamap1.presentation.util.SortOption
import cat.copernic.aguamap1.ui.map.MapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.* // Importante para el cálculo de movimiento

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getFountainsUseCase: GetFountainsUseCase,
    private val getDistanceUseCase: GetDistanceFountainsUseCaseUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val locationProvider: DefaultLocationProvider
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

    var userLat by mutableStateOf<Double?>(null)
        private set
    var userLng by mutableStateOf<Double?>(null)
        private set

    val isLocationAvailable: Boolean get() = userLat != null && userLng != null

    var searchQuery by mutableStateOf("")
        private set
    var showFilterMenu by mutableStateOf(false)
        private set
    var filterState by mutableStateOf(FilterState())
        private set
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    var selectedFountainForDetail by mutableStateOf<Fountain?>(null)
        private set

    init {
        observeLocationUpdates()
        loadCategories()
    }

    private fun observeLocationUpdates() {
        viewModelScope.launch {
            locationProvider.getLocationUpdates().collect { location ->
                onFirstLocationFound(location.latitude, location.longitude)
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { categories = it }
        }
    }

    fun loadFountains() {
        if (userLat == null || userLng == null) return

        viewModelScope.launch {
            getFountainsUseCase(userLat, userLng).collect { result ->
                result.onSuccess { list ->
                    allFountainsList = list
                    updateDistances()
                }
            }
        }
    }

    // --- CORRECCIÓN PARA EVITAR DOBLE CARGA Y COORDENADAS FALSAS ---
    fun onFirstLocationFound(lat: Double, lng: Double) {
        // ESCUDO 1: Ignoramos la ubicación si es exactamente la de Terrassa por defecto
        // y no es la primera vez (o si es 0.0), para evitar que el juego falle al arrancar.
        if (lat == 41.5632 && lng == 2.0089 && !isFirstLocationUpdate) return
        if (lat == 0.0) return

        // Si no es la primera vez, comprobamos si el movimiento es significativo (2m)
        if (!isFirstLocationUpdate) {
            val lastLat = userLat ?: 0.0
            val lastLng = userLng ?: 0.0
            val distanceMoved = getDistanceUseCase(lastLat, lastLng, lat, lng)

            if (distanceMoved < 2.0) return
        }

        userLat = lat
        userLng = lng

        if (isFirstLocationUpdate) {
            latitude = lat
            longitude = lng
            zoomLevel = 17.0
            isFirstLocationUpdate = false
            loadFountains()
        } else {
            updateDistances()
        }
    }

    private fun updateDistances() {
        val lat = userLat ?: run { applyFilterAndSort(); return }
        val lng = userLng ?: run { applyFilterAndSort(); return }

        allFountainsList = allFountainsList.map { fountain ->
            fountain.copy(
                distanceFromUser = getDistanceUseCase(
                    lat,
                    lng,
                    fountain.latitude,
                    fountain.longitude
                )
            )
        }
        applyFilterAndSort()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        applyFilterAndSort()
    }

    fun toggleFilterMenu() {
        showFilterMenu = !showFilterMenu
    }

    fun updateFilters(newState: FilterState) {
        filterState = newState
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        var filtered = allFountainsList

        if (searchQuery.isNotBlank()) {
            val regex = generateSearchRegex(searchQuery)
            filtered = if (regex != null) {
                filtered.filter { regex.containsMatchIn(it.name) }
            } else {
                filtered.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
        }

        filterState.selectedCategory?.let { cat ->
            filtered = filtered.filter { it.category.id == cat.id }
        }

        if (filterState.onlyOperational) {
            filtered = filtered.filter { it.operational }
        }

        filtered = filtered.filter { fountain ->
            val ratingMatch = fountain.ratingAverage >= filterState.minRating
            val distanceMatch =
                (fountain.distanceFromUser ?: 0.0) / 1000.0 <= filterState.maxDistanceKm
            ratingMatch && distanceMatch
        }

        val sorted = when (filterState.sortBy) {
            SortOption.DISTANCE_ASC -> filtered.sortedBy { it.distanceFromUser ?: Double.MAX_VALUE }
            SortOption.DISTANCE_DESC -> filtered.sortedByDescending { it.distanceFromUser ?: 0.0 }
            SortOption.RATING_DESC -> filtered.sortedByDescending { it.ratingAverage }
            SortOption.RATING_ASC -> filtered.sortedBy { it.ratingAverage }
            SortOption.DATE_DESC -> filtered.sortedByDescending { it.dateCreated }
            SortOption.DATE_ASC -> filtered.sortedBy { it.dateCreated }
        }

        uiState = uiState.copy(fountains = sorted)
    }

    fun toggleView() {
        _isMapView.value = !_isMapView.value
    }

    fun onMapMoved(lat: Double, lng: Double, zoom: Double) {
        latitude = lat
        longitude = lng
        zoomLevel = zoom
    }

    fun selectFountain(fountain: Fountain) {
        selectedFountainForDetail = fountain
    }

    fun clearSelectedFountain() {
        selectedFountainForDetail = null
    }

    fun updateSingleFountainInList(updatedFountain: Fountain) {
        allFountainsList = allFountainsList.map {
            if (it.id == updatedFountain.id) updatedFountain else it
        }
        if (selectedFountainForDetail?.id == updatedFountain.id) {
            selectedFountainForDetail = updatedFountain
        }
        applyFilterAndSort()
    }
}