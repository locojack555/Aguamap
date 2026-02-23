package cat.copernic.aguamap1.presentation.home.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.UserRole
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.usecase.auth.GetUserRoleUseCase
import cat.copernic.aguamap1.domain.usecase.category.GetCategoriesUseCase
import cat.copernic.aguamap1.domain.usecase.comments.AddCommentUseCase
import cat.copernic.aguamap1.domain.usecase.comments.CensorCommentUseCase
import cat.copernic.aguamap1.domain.usecase.comments.DeleteCommentUseCase
import cat.copernic.aguamap1.domain.usecase.comments.UpdateCommentUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.DeleteFountainUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.GetDistanceFountainsUseCaseUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.GetFountainsUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.ProcessFountainVoteUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.UpdateFountainUseCase
import cat.copernic.aguamap1.presentation.util.FilterState
import cat.copernic.aguamap1.presentation.util.SortOption
import cat.copernic.aguamap1.ui.map.MapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getFountainsUseCase: GetFountainsUseCase,
    private val getDistanceUseCase: GetDistanceFountainsUseCaseUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val authRepository: AuthRepository,
    private val getUserRoleUseCase: GetUserRoleUseCase,
    private val deleteFountainUseCase: DeleteFountainUseCase,
    private val updateFountainUseCase: UpdateFountainUseCase,
    private val processFountainVoteUseCase: ProcessFountainVoteUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val censorCommentUseCase: CensorCommentUseCase,
    private val updateCommentUseCase: UpdateCommentUseCase
) : ViewModel() {

    // --- ESTADOS DE USUARIO ---
    var isAdmin by mutableStateOf(false)
        private set
    var currentUserId by mutableStateOf<String?>(null)
        private set
    var currentUserName by mutableStateOf<String?>(null)
        private set

    // --- ESTADOS DEL MAPA ---
    var latitude by mutableDoubleStateOf(41.5632)
    var longitude by mutableDoubleStateOf(2.0089)
    var zoomLevel by mutableDoubleStateOf(15.0)
    var isFirstLocationUpdate by mutableStateOf(true)
    var uiState by mutableStateOf(MapUiState())
        private set

    var selectedFountain by mutableStateOf<Fountain?>(null)
        private set

    private val _isMapView = MutableStateFlow(true)
    val isMapView: StateFlow<Boolean> = _isMapView.asStateFlow()

    private var allFountainsList = emptyList<Fountain>()
    var userLat: Double? = null
        private set
    var userLng: Double? = null
        private set

    // --- ESTADOS DE FILTRO ---
    var searchQuery by mutableStateOf("")
        private set
    var showFilterMenu by mutableStateOf(false)
        private set
    var filterState by mutableStateOf(FilterState())
        private set

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    private var commentsJob: Job? = null

    init {
        loadFountains()
        loadCategories()
        loadUserData()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { lista ->
                categories = lista
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserUid()
            currentUserId = uid
            if (uid != null) {
                val role = getUserRoleUseCase(uid)
                isAdmin = (role == UserRole.ADMIN)
                currentUserName = authRepository.getCurrentUserName()
            } else {
                isAdmin = false
                currentUserName = null
            }
        }
    }

    // --- GESTIÓN DE COMENTARIOS ---

    private fun observeComments(fountainId: String) {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            getFountainsUseCase.fetchComments(fountainId).collect { result ->
                result.onSuccess { list ->
                    selectedFountain = selectedFountain?.copy(comments = list)
                }
            }
        }
    }

    fun addCommentToSelectedFountain(rating: Int, text: String) {
        val fountain = selectedFountain ?: return
        val userId = currentUserId ?: return
        val userName = currentUserName ?: "Usuario"
        viewModelScope.launch {
            val newComment = Comment(
                userId = userId,
                userName = userName,
                rating = rating,
                comment = text,
                timestamp = System.currentTimeMillis()
            )
            addCommentUseCase(fountain, newComment).onFailure { error ->
                uiState = uiState.copy(errorMessage = "Error: ${error.message}")
            }
        }
    }

    fun editMyComment(commentId: String, newRating: Int, newText: String) {
        val fountainId = selectedFountain?.id ?: return
        viewModelScope.launch {
            updateCommentUseCase(fountainId, commentId, newRating, newText).onFailure { error ->
                uiState = uiState.copy(errorMessage = "Error al editar: ${error.message}")
            }
        }
    }

    fun censorComment(commentId: String) {
        if (!isAdmin) return
        val fountainId = selectedFountain?.id ?: return
        viewModelScope.launch {
            censorCommentUseCase(fountainId, commentId).onFailure { error ->
                uiState = uiState.copy(errorMessage = "Error al censurar")
            }
        }
    }

    fun deleteComment(commentId: String) {
        val fountainId = selectedFountain?.id ?: return
        viewModelScope.launch {
            deleteCommentUseCase(fountainId, commentId).onFailure { error ->
                uiState = uiState.copy(errorMessage = "Error al borrar")
            }
        }
    }

    // --- SELECCIÓN ---

    fun selectFountain(fountain: Fountain) {
        selectedFountain = fountain
        observeComments(fountain.id)
    }

    fun clearSelection() {
        selectedFountain = null
        commentsJob?.cancel()
    }

    // --- ACCIONES DE FUENTE ---

    fun deleteSelectedFountain() {
        val fountainId = selectedFountain?.id ?: return
        if (!isAdmin) return

        viewModelScope.launch {
            deleteFountainUseCase(fountainId).onSuccess {
                clearSelection()
            }.onFailure { error ->
                uiState = uiState.copy(errorMessage = "Error al eliminar: ${error.message}")
            }
        }
    }

    fun updateFountainData(updates: Map<String, Any>) {
        val fountainId = selectedFountain?.id ?: return
        if (!isAdmin) return

        viewModelScope.launch {
            updateFountainUseCase(fountainId, updates).onFailure { error ->
                uiState = uiState.copy(errorMessage = "Error al actualizar: ${error.message}")
            }
        }
    }

    // Voto positivo único
    fun confirmFountain() {
        val userId = currentUserId ?: return
        selectedFountain?.let { fountain ->
            viewModelScope.launch {
                processFountainVoteUseCase.addPositiveVote(fountain, userId)
                    .onSuccess { loadFountains() }
                    .onFailure { error ->
                        uiState = uiState.copy(errorMessage = error.message ?: "Ya has votado")
                    }
            }
        }
    }

    // Voto negativo único (No existe)
    fun reportNonExistent() {
        val fountain = selectedFountain ?: return
        val userId = currentUserId ?: return

        viewModelScope.launch {
            processFountainVoteUseCase.addNegativeVote(fountain, userId)
                .onSuccess {
                    if (fountain.negativeVotes + 1 >= 3) {
                        clearSelection()
                    } else {
                        loadFountains()
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(errorMessage = error.message ?: "Ya has votado")
                }
        }
    }

    // Unificado: Cambia entre Averiada y Funcionando
    fun reportBroken() {
        val fountain = selectedFountain ?: return
        viewModelScope.launch {
            val newStatus = !fountain.operational
            selectedFountain = fountain.copy(operational = newStatus)
            updateFountainUseCase(fountain.id, mapOf("operational" to newStatus))
                .onSuccess {
                    loadFountains()
                }
                .onFailure {
                    selectedFountain = fountain
                }
        }
    }

    // --- FILTRADO Y MAPA ---

    fun updateFilters(newFilters: FilterState) {
        filterState = newFilters
        applyFilterAndSort()
    }

    fun toggleFilterMenu() {
        showFilterMenu = !showFilterMenu
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

    fun loadFountains() {
        viewModelScope.launch {
            getFountainsUseCase().collect { result ->
                result.onSuccess { list ->
                    allFountainsList = list
                    selectedFountain?.let { current ->
                        val updated = list.find { it.id == current.id }
                        if (updated != null) {
                            selectedFountain = updated.copy(comments = current.comments)
                        }
                    }
                    if (userLat != null && userLng != null) {
                        updateDistances()
                    } else {
                        applyFilterAndSort()
                    }
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
        var filtered = allFountainsList
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
        filterState.selectedCategory?.let { selected ->
            filtered = filtered.filter { it.category.id == selected.id }
        }
        if (filterState.onlyOperational) {
            filtered = filtered.filter { it.operational }
        }
        filtered = filtered.filter { it.ratingAverage >= filterState.minRating }
        filtered = filtered.filter {
            val dist = it.distanceFromUser ?: Double.MAX_VALUE
            (dist / 1000.0) <= filterState.maxDistanceKm
        }
        val sorted = when (filterState.sortBy) {
            SortOption.DISTANCE -> filtered.sortedBy { it.distanceFromUser ?: Double.MAX_VALUE }
            SortOption.RATING -> filtered.sortedByDescending { it.ratingAverage }
            SortOption.DATE -> filtered.sortedByDescending { it.dateCreated }
        }
        uiState = uiState.copy(fountains = sorted)
    }
}