package cat.copernic.aguamap1.domain.repository

import cat.copernic.aguamap1.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: String): Category?
    suspend fun createCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(id: String)
}