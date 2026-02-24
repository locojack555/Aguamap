package cat.copernic.aguamap1.presentation.fountain.addFountain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.usecase.category.GetCategoriesUseCase
import cat.copernic.aguamap1.domain.usecase.fountain.CreateFountainUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class AddFountainViewModel @Inject constructor(
    private val createFountainUseCase: CreateFountainUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    // --- ESTADO DE NAVEGACIÓN (pantalla completa, ya no modal) ---
    var isAdding by mutableStateOf(false)
        private set

    var selectedLocationForNewFountain by mutableStateOf<GeoPoint?>(null)
        private set

    // --- ESTADO DEL FORMULARIO (Hoisting) ---
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var selectedCategory by mutableStateOf<Category?>(null)
    var isOperational by mutableStateOf(true)

    // Estado para las categorías de la UI
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    // Validación reactiva
    val isFormValid: Boolean
        get() = name.isNotBlank() && description.isNotBlank() && selectedCategory != null

    init {
        viewModelScope.launch {
            getCategoriesUseCase().collect { lista ->
                categories = lista
            }
        }
    }

    fun openAddFountain(lat: Double, lng: Double) {
        selectedLocationForNewFountain = GeoPoint(lat, lng)
        isAdding = true
    }

    fun closeAddFountain() {
        resetForm()
        isAdding = false
        selectedLocationForNewFountain = null
    }

    private fun resetForm() {
        name = ""
        description = ""
        selectedCategory = null
        isOperational = true
    }

    fun addNewFountain(onSuccess: () -> Unit) {
        val location = selectedLocationForNewFountain ?: return
        val category = selectedCategory ?: return

        viewModelScope.launch {
            val newFountain = Fountain(
                name = name,
                latitude = location.latitude,
                longitude = location.longitude,
                operational = isOperational,
                category = category,
                description = description
            )

            val result = createFountainUseCase(newFountain, isUserAdmin = false)

            result.onSuccess {
                onSuccess()
                closeAddFountain()
            }
        }
    }
}