package cat.copernic.aguamap1.presentation.categories

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
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
import cat.copernic.aguamap1.domain.usecase.fountain.GetDistanceFountainsUseCaseUseCase
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
    private val getDistanceUseCase: GetDistanceFountainsUseCaseUseCase,
    private val cloudinaryService: CloudinaryService,
    private val authRepository: AuthRepository,
    private val getUserRoleUseCase: GetUserRoleUseCase,
    application: Application // Cambiamos a AndroidViewModel para acceder a recursos
) : AndroidViewModel(application) {

    // Helper para obtener traducciones
    private fun getString(resId: Int): String = getApplication<Application>().getString(resId)

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

    // --- OPTIMIZACIÓN DE UBICACIÓN ---
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    private var lastFetchedLocation: Pair<Double, Double>? = null
    private val MIN_DISTANCE_TO_REFETCH = 2000.0

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
            if (location == null) {
                kotlinx.coroutines.flow.flowOf(Result.success(emptyList<Fountain>()))
            } else {
                getFountainsUseCase(location.first, location.second)
            }
        }
        .combine(_operationalFilter) { result, isOp ->
            val fountains = result.getOrDefault(emptyList())
            val location = _userLocation.value

            val fountainsWithDistance = if (location != null) {
                fountains.map { fountain ->
                    fountain.copy(
                        distanceFromUser = getDistanceUseCase(
                            location.first, location.second,
                            fountain.latitude, fountain.longitude
                        )
                    )
                }
            } else fountains

            val filtered = if (isOp == null) fountainsWithDistance else fountainsWithDistance.filter { it.operational == isOp }
            filtered.groupBy { it.category.id.trim() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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

    fun updateSearchQuery(query: String) { _searchQuery.value = query }

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

    fun clearError() { errorMessage = null }

    fun updateSelectedImage(uri: Uri?) { selectedImageUri = uri }

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

                if (categoryToEdit != null) updateCategoryUseCase(categoryData)
                else createCategoryUseCase(categoryData)

                onSuccess()
                resetForm()
            } catch (e: Exception) {
                errorMessage = getString(R.string.error_email_generic)
            } finally {
                isUploading = false
            }
        }
    }

    fun deleteCategory(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val fountainsInThisCategory = fountainsByCategory.value[id]
                if (!fountainsInThisCategory.isNullOrEmpty()) {
                    errorMessage = getString(R.string.error_category_not_empty)
                    return@launch
                }
                deleteCategoryUseCase(id)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = getString(R.string.error_title)
            }
        }
    }
}