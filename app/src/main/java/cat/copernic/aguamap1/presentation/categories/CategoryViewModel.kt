package cat.copernic.aguamap1.presentation.categories

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.data.cloudinary.CloudinaryService
import cat.copernic.aguamap1.data.cloudinary.UploadProgress
import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.UserRole
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.usecase.auth.GetUserRoleUseCase
import cat.copernic.aguamap1.domain.usecase.category.CreateCategoryUseCase
import cat.copernic.aguamap1.domain.usecase.category.DeleteCategoryUseCase
import cat.copernic.aguamap1.domain.usecase.category.GetCategoriesUseCase
import cat.copernic.aguamap1.domain.usecase.category.UpdateCategoryUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.GetFountainsUseCase
import cat.copernic.aguamap1.domain.usecase.validation.generateSearchRegex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val getFountainsUseCase: GetFountainsUseCase,
    private val cloudinaryService: CloudinaryService,
    private val authRepository: AuthRepository,
    private val getUserRoleUseCase: GetUserRoleUseCase
) : ViewModel() {

    // --- ESTADO DE USUARIO ---
    var isAdmin by mutableStateOf(false)
        private set
    var currentUserId by mutableStateOf<String?>(null)
        private set

    // --- FILTROS Y BÚSQUEDA ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _operationalFilter = MutableStateFlow<Boolean?>(null)
    val operationalFilter: StateFlow<Boolean?> = _operationalFilter

    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())

    // --- OPTIMIZACIÓN DE UBICACIÓN Y FIREBASE ---
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    private var lastFetchedLocation: Pair<Double, Double>? = null
    private val MIN_DISTANCE_TO_REFETCH = 3000.0 // 2 Kilómetros

    /**
     * Actualiza la ubicación pero solo dispara una nueva consulta a Firebase
     * si el usuario se ha movido más de 2km para ahorrar lecturas.
     */
    fun setLocation(lat: Double, lng: Double) {
        val lastLoc = lastFetchedLocation
        if (lastLoc == null) {
            _userLocation.value = lat to lng
            lastFetchedLocation = lat to lng
        } else {
            val distance = FloatArray(1)
            android.location.Location.distanceBetween(
                lastLoc.first, lastLoc.second, lat, lng, distance
            )
            if (distance[0] > MIN_DISTANCE_TO_REFETCH) {
                _userLocation.value = lat to lng
                lastFetchedLocation = lat to lng
            }
        }
    }

    // --- FLUJOS DE DATOS ---
    val categories: StateFlow<List<Category>> =
        combine(_allCategories, _searchQuery) { list, query ->
            if (query.isBlank()) {
                list
            } else {
                val regex = generateSearchRegex(query)
                if (regex != null) {
                    list.filter { it.name.contains(regex) }
                } else {
                    list.filter { it.name.contains(query, ignoreCase = true) }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val fountainsByCategory: StateFlow<Map<String, List<Fountain>>> = _userLocation
        .flatMapLatest { location ->
            // Solo descarga fuentes en radio de 15km (definido en repositorio)
            getFountainsUseCase(location?.first, location?.second)
        }
        .combine(_operationalFilter) { result, isOp ->
            val fountains = result.getOrDefault(emptyList())
            val filtered = if (isOp == null) {
                fountains
            } else {
                fountains.filter { it.operational == isOp }
            }
            filtered.groupBy { it.category.id }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // --- ESTADO DEL FORMULARIO ---
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var currentImageUrl by mutableStateOf("")
    var isUploading by mutableStateOf(false)
    var uploadProgress by mutableIntStateOf(0)
    var errorMessage by mutableStateOf<String?>(null)
    var categoryToEdit by mutableStateOf<Category?>(null)

    init {
        loadUserData()
        viewModelScope.launch {
            getCategoriesUseCase().collect { _allCategories.value = it }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserUid()
            currentUserId = uid
            if (uid != null) {
                val role = getUserRoleUseCase(uid)
                isAdmin = (role == UserRole.ADMIN)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleOperationalFilter(isFilterActive: Boolean) {
        _operationalFilter.value = if (isFilterActive) null else false
    }

    fun onEditCategory(category: Category) {
        categoryToEdit = category
        name = category.name
        description = category.description
        currentImageUrl = category.imageUrl
        selectedImageUri = null
    }

    fun resetForm() {
        name = ""; description = ""; selectedImageUri = null
        currentImageUrl = ""; categoryToEdit = null
        uploadProgress = 0; errorMessage = null
    }

    fun clearError() {
        errorMessage = null
    }

    fun updateSelectedImage(uri: Uri?) {
        selectedImageUri = uri
    }

    fun clearSelectedImage() {
        selectedImageUri = null
        currentImageUrl = ""
    }

    fun saveCategory(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isUploading = true
            errorMessage = null
            try {
                var finalUrl = currentImageUrl
                selectedImageUri?.let { uri ->
                    cloudinaryService.uploadImageWithProgress(uri).collect { progress ->
                        if (progress is UploadProgress.InProgress) {
                            uploadProgress = progress.percentage
                        }
                    }
                    val uploadResult = cloudinaryService.uploadImage(uri)
                    finalUrl = uploadResult.getOrThrow()
                }

                val categoryData = Category(
                    id = categoryToEdit?.id ?: "",
                    name = name,
                    description = description,
                    imageUrl = finalUrl
                )

                if (categoryToEdit != null) {
                    updateCategoryUseCase(categoryData)
                } else {
                    createCategoryUseCase(categoryData)
                }
                onSuccess()
                resetForm()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido al guardar"
            } finally {
                isUploading = false
            }
        }
    }

    fun canDeleteCategory(categoryId: String): Boolean {
        return fountainsByCategory.value[categoryId].isNullOrEmpty()
    }

    fun deleteCategory(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                if (!canDeleteCategory(id)) {
                    errorMessage = "No se puede eliminar la categoría porque todavía tiene fuentes asociadas en tu zona."
                    return@launch
                }
                deleteCategoryUseCase(id)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Error al eliminar: ${e.message}"
            }
        }
    }
}