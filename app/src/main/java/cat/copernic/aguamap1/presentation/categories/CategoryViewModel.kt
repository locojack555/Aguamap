package cat.copernic.aguamap1.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.CategoryRepository
import cat.copernic.aguamap1.domain.usecase.fountain.GetFountainsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder { NAME, BEST_RATING, WORST_RATING }
enum class FountainStateFilter { ALL, OPERATIONAL, MAINTENANCE }

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val getFountainsUseCase: GetFountainsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortOrder = MutableStateFlow(SortOrder.NAME)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _stateFilter = MutableStateFlow(FountainStateFilter.ALL)
    val stateFilter: StateFlow<FountainStateFilter> = _stateFilter

    // 1. Buscador exclusivo de Categorías
    val categories: StateFlow<List<Category>> = combine(
        categoryRepository.getCategories(),
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list.sortedBy { it.name }
        } else {
            val regex = queryToRegex(query)
            list.filter { it.name.matches(regex) }.sortedBy { it.name }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Filtros aplicados a las fuentes (sin buscador de texto)
    val fountainsByCategory: StateFlow<Map<String, List<Fountain>>> = combine(
        getFountainsUseCase().map { it.getOrNull() ?: emptyList() },
        _sortOrder,
        _stateFilter
    ) { fountains, order, state ->

        // Filtrado por estado (Operativa / Mantenimiento)
        val filteredByState = when(state) {
            FountainStateFilter.OPERATIONAL -> fountains.filter { it.operational }
            FountainStateFilter.MAINTENANCE -> fountains.filter { !it.operational }
            FountainStateFilter.ALL -> fountains
        }

        // Ordenación por valoración global
        val processedList = when (order) {
            SortOrder.BEST_RATING -> filteredByState.sortedByDescending { it.ratingAverage }
            SortOrder.WORST_RATING -> filteredByState.sortedBy { it.ratingAverage }
            else -> filteredByState.sortedBy { it.name }
        }

        // Agrupamos las fuentes
        processedList.groupBy {
            try {
                it.category.id.lowercase().trim()
            } catch (e: Exception) {
                it.category.toString().lowercase().trim()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private fun queryToRegex(query: String): Regex {
        if (!query.contains("*") && !query.contains("?")) {
            return Regex("(?i).*${Regex.escape(query)}.*")
        }
        val escaped = Regex.escape(query).replace("\\*", ".*").replace("\\?", ".")
        return Regex("(?i)^$escaped$")
    }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateSortOrder(order: SortOrder) { _sortOrder.value = order }
    fun updateStateFilter(filter: FountainStateFilter) { _stateFilter.value = filter }

    // GESTIÓN DE CATEGORÍAS
    fun addCategory(category: Category) {
        viewModelScope.launch { categoryRepository.createCategory(category) }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch { categoryRepository.updateCategory(category) }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch { categoryRepository.deleteCategory(id) }
    }
}