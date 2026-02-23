package cat.copernic.aguamap1.domain.usecase.category

import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(): Flow<List<Category>> = repository.getCategories()
}