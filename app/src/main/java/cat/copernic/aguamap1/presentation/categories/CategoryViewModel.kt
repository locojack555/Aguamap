package cat.copernic.aguamap1.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.CategoryRepository
import cat.copernic.aguamap1.domain.repository.FountainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val fountainRepository: FountainRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val categories: StateFlow<List<Category>> = combine(
        categoryRepository.getCategories(),
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) list else list.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fountainsByCategory: StateFlow<Map<String, List<Fountain>>> = fountainRepository.fetchSources()
        .map { result: Result<List<Fountain>> ->
            val listaFuentes = result.getOrNull() ?: emptyList()
            // Normalizamos el texto (minúsculas y sin espacios) para que coincida siempre
            listaFuentes.groupBy { fuente: Fountain -> fuente.category.lowercase().trim() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

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