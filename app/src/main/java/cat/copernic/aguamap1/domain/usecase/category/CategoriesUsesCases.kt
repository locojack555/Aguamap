package cat.copernic.aguamap1.domain.usecase.category

import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Caso de Uso para obtener el flujo de todas las categorías disponibles.
 * Devuelve un [Flow] para que la UI se actualice automáticamente si una categoría
 * cambia en la base de datos (tiempo real).
 */
class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke() = repository.getCategories()
}

/**
 * Caso de Uso para registrar una nueva categoría en el sistema.
 * Solo debe ser accesible por usuarios con rol ADMIN.
 */
class CreateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    /**
     * @param category El objeto con los datos de la nueva categoría (nombre, imagen, etc.).
     */
    suspend operator fun invoke(category: Category) = repository.createCategory(category)
}

/**
 * Caso de Uso para modificar los datos de una categoría existente.
 * Permite actualizar el nombre, la descripción o la imagen de referencia.
 */
class UpdateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    /**
     * @param category El objeto categoría con los campos ya modificados.
     */
    suspend operator fun invoke(category: Category) = repository.updateCategory(category)
}

/**
 * Caso de Uso para eliminar una categoría del sistema de forma permanente.
 */
class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    /**
     * @param id El identificador único de la categoría que se desea borrar.
     */
    suspend operator fun invoke(id: String) = repository.deleteCategory(id)
}