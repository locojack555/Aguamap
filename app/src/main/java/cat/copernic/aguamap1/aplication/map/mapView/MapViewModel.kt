package cat.copernic.aguamap1.aplication.map.mapView

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.data.location.DefaultLocationProvider
import cat.copernic.aguamap1.domain.model.category.Category
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.domain.usecase.category.GetCategoriesUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.GetDistanceFountainsUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.GetFountainsUseCase
import cat.copernic.aguamap1.domain.usecase.validation.generateSearchRegex
import cat.copernic.aguamap1.aplication.utils.FilterState
import cat.copernic.aguamap1.aplication.utils.SortOption
import cat.copernic.aguamap1.ui.map.MapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel central para la gestión del mapa y la lista de fuentes.
 * Se encarga de la lógica de filtrado reactivo, cálculo de distancias en tiempo real
 * y sincronización con el proveedor de ubicación del dispositivo.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val getFountainsUseCase: GetFountainsUseCase,
    private val getDistanceUseCase: GetDistanceFountainsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val locationProvider: DefaultLocationProvider
) : ViewModel() {

    // --- ESTADO DE LA CÁMARA Y MAPA ---
    var latitude by mutableDoubleStateOf(41.5632) // Coordenadas por defecto (Terrassa)
    var longitude by mutableDoubleStateOf(2.0089)
    var zoomLevel by mutableDoubleStateOf(15.0)
    var isFirstLocationUpdate by mutableStateOf(true)

    /**
     * Estado inmutable de la UI que contiene la lista de fuentes procesada (filtrada y ordenada).
     */
    var uiState by mutableStateOf(MapUiState())
        private set

    private val _isMapView = MutableStateFlow(true)
    val isMapView: StateFlow<Boolean> = _isMapView.asStateFlow()

    // Fuente de datos original (sin filtros) para realizar operaciones en memoria
    private var allFountainsList = emptyList<Fountain>()

    // --- DATOS DE USUARIO Y LOCALIZACIÓN ---
    var userLat by mutableStateOf<Double?>(null)
        private set
    var userLng by mutableStateOf<Double?>(null)
        private set

    val isLocationAvailable: Boolean get() = userLat != null && userLng != null

    // --- ESTADOS DE FILTRADO Y BÚSQUEDA ---
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

    /**
     * Se subscribe al flujo de ubicación del GPS. Cada vez que la posición cambia,
     * actualizamos las distancias relativas de todas las fuentes.
     */
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

    /**
     * Carga las fuentes desde el repositorio (Firebase) basándose en la ubicación actual.
     */
    fun loadFountains() {
        val lat = userLat ?: return
        val lng = userLng ?: return

        viewModelScope.launch {
            getFountainsUseCase(lat, lng).collect { result ->
                result.onSuccess { list ->
                    allFountainsList = list
                    updateDistances()
                }
            }
        }
    }

    /**
     * Gestiona la llegada de la primera ubicación válida.
     * Implementa un umbral de 2 metros para evitar recalcular la UI con micro-movimientos.
     */
    fun onFirstLocationFound(lat: Double, lng: Double) {
        if (lat == 0.0) return
        if (lat == 41.5632 && lng == 2.0089 && !isFirstLocationUpdate) return

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

    /**
     * Recalcula la distancia de cada fuente respecto a la posición actual del usuario.
     */
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

    // --- ACCIONES DE USUARIO ---

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


    /**
     * MOTOR DE FILTRADO Y ORDENACIÓN PROFESIONAL:
     * Aplica de forma secuencial: Búsqueda -> Categoría -> Estado -> Valoración/Distancia -> Orden.
     */
    private fun applyFilterAndSort() {
        var filtered = allFountainsList

        // 1. Filtro de búsqueda (Soporte para Regex generado dinámicamente)
        if (searchQuery.isNotBlank()) {
            val regex = generateSearchRegex(searchQuery)
            filtered = if (regex != null) {
                filtered.filter { regex.containsMatchIn(it.name) }
            } else {
                filtered.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
        }

        // 2. Filtro por categoría
        filterState.selectedCategory?.let { cat ->
            filtered = filtered.filter { it.category.id == cat.id }
        }

        // 3. Filtro de operatividad
        if (filterState.onlyOperational) {
            filtered = filtered.filter { it.operational }
        }

        // 4. Filtro combinado: Rating mínimo y Distancia máxima (Conversión Km a Metros)
        filtered = filtered.filter { fountain ->
            val ratingMatch = fountain.ratingAverage >= filterState.minRating
            val distanceInMeters = fountain.distanceFromUser ?: 0.0
            val maxAllowedMeters = filterState.maxDistanceKm * 1000.0

            ratingMatch && (distanceInMeters <= maxAllowedMeters)
        }

        // 5. Lógica de Ordenación
        val sorted = when (filterState.sortBy) {
            SortOption.DISTANCE_ASC -> filtered.sortedBy { it.distanceFromUser ?: Double.MAX_VALUE }
            SortOption.DISTANCE_DESC -> filtered.sortedByDescending { it.distanceFromUser ?: 0.0 }
            SortOption.RATING_DESC -> filtered.sortedByDescending { it.ratingAverage }
            SortOption.RATING_ASC -> filtered.sortedBy { it.ratingAverage }
            SortOption.DATE_DESC -> filtered.sortedByDescending { it.dateCreated }
            SortOption.DATE_ASC -> filtered.sortedBy { it.dateCreated }
        }

        // Actualizamos el estado de la UI para disparar la recomposición en Compose
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

    /**
     * Actualiza una fuente específica en la lista sin necesidad de recargar desde el servidor.
     * Útil tras calificar una fuente o reportar un error.
     */
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