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

    // --- ESTADO DE USUARIO Y ROL ---
    var isAdmin by mutableStateOf(false)
        private set

    var currentUserId by mutableStateOf<String?>(null)
        private set

    // --- ESTADOS DE UI (LISTA Y FILTROS) ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _operationalFilter = MutableStateFlow<Boolean?>(null)
    val operationalFilter: StateFlow<Boolean?> = _operationalFilter

    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())

    // MODIFICADO: Ahora utiliza generateSearchRegex para filtrar las categorías
    val categories: StateFlow<List<Category>> =
        combine(_allCategories, _searchQuery) { list, query ->
            if (query.isBlank()) {
                list
            } else {
                val regex = generateSearchRegex(query)
                if (regex != null) {
                    list.filter { it.name.contains(regex) }
                } else {
                    // Fallback por si la expresión regular no es válida
                    list.filter { it.name.contains(query, ignoreCase = true) }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fountainsByCategory: StateFlow<Map<String, List<Fountain>>> = combine(
        getFountainsUseCase(),
        _operationalFilter
    ) { result, isOp ->
        val fountains = result.getOrDefault(emptyList())
        val filtered = if (isOp == null) {
            fountains
        } else {
            fountains.filter { it.operational == isOp }
        }
        // Agrupamos por ID exacto (evitamos lowercase/trim para coincidir con Firestore)
        filtered.groupBy { it.category.id }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // --- ESTADO DEL FORMULARIO (PARA CREAR/EDITAR) ---
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

    // --- ACCIONES ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleOperationalFilter(currentIsOnlyAveriadas: Boolean) {
        // Si ya estaba seleccionado (true), lo ponemos a null (quitar filtro)
        // Si no estaba seleccionado, lo ponemos a false (solo averiadas)
        _operationalFilter.value = if (currentIsOnlyAveriadas) null else false
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

    fun saveCategory(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isUploading = true
            errorMessage = null
            try {
                var finalUrl = currentImageUrl

                // 1. Subir a Cloudinary si hay URI nueva
                selectedImageUri?.let { uri ->
                    cloudinaryService.uploadImageWithProgress(uri).collect { progress ->
                        if (progress is UploadProgress.InProgress) {
                            uploadProgress = progress.percentage
                        }
                    }
                    val uploadResult = cloudinaryService.uploadImage(uri)
                    finalUrl = uploadResult.getOrThrow()
                }

                // 2. Operación en Firestore
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

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            try {
                deleteCategoryUseCase(id)
            } catch (e: Exception) {
                errorMessage = "Error al eliminar: ${e.message}"
            }
        }
    }
}