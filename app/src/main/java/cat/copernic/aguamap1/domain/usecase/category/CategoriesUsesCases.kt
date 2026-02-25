package cat.copernic.aguamap1.domain.usecase.category

import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(private val repository: CategoryRepository) {
    operator fun invoke() = repository.getCategories()
}

class CreateCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category) = repository.createCategory(category)
}

class UpdateCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category) = repository.updateCategory(category)
}

class DeleteCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: String) = repository.deleteCategory(id)
}