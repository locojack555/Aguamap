package cat.copernic.aguamap1.presentation.home.map

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

    var showAddFountainSheet by mutableStateOf(false)
        private set

    var selectedLocationForNewFountain by mutableStateOf<GeoPoint?>(null)
        private set

    // Estado para las categorías de la UI
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    init {
        // Cargamos las categorías al iniciar
        viewModelScope.launch {
            getCategoriesUseCase().collect { lista ->
                categories = lista
            }
        }
    }

    fun openAddFountain(lat: Double, lng: Double) {
        selectedLocationForNewFountain = GeoPoint(lat, lng)
        showAddFountainSheet = true
    }

    fun closeAddFountain() {
        showAddFountainSheet = false
        selectedLocationForNewFountain = null
    }

    fun addNewFountain(
        name: String,
        description: String,
        category: Category,
        isOperational: Boolean,
        onSuccess: () -> Unit
    ) {
        val location = selectedLocationForNewFountain ?: return

        // Validación de campos obligatorios (Clean Architecture: la lógica de validación previa)
        if (name.isBlank() || description.isBlank() || category.id.isEmpty()) return

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