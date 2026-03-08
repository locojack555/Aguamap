package cat.copernic.aguamap1.domain.repository.category

import cat.copernic.aguamap1.domain.model.category.Category
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define el contrato para la gestión de categorías de fuentes.
 * Las categorías permiten clasificar las fuentes según su tipo o utilidad.
 */
interface CategoryRepository {

    /**
     * Obtiene un flujo continuo de la lista de todas las categorías disponibles.
     * Al devolver un [kotlinx.coroutines.flow.Flow], la UI se actualizará automáticamente si una categoría
     * es añadida, editada o eliminada en la base de datos.
     * @return Flow con la lista actualizada de objetos [cat.copernic.aguamap1.domain.model.category.Category].
     */
    fun getCategories(): Flow<List<Category>>

    /**
     * Busca una categoría específica mediante su identificador único.
     * @param id El identificador único de la categoría.
     * @return El objeto [Category] si existe, o null si no se encuentra.
     */
    suspend fun getCategoryById(id: String): Category?

    /**
     * Registra una nueva categoría en el sistema.
     * @param category Objeto categoría que contiene la información a guardar.
     */
    suspend fun createCategory(category: Category)

    /**
     * Actualiza la información de una categoría ya existente.
     * @param category Objeto categoría con los datos actualizados (debe contener un ID válido).
     */
    suspend fun updateCategory(category: Category)

    /**
     * Elimina de forma permanente una categoría del sistema.
     * @param id El identificador de la categoría que se desea borrar.
     */
    suspend fun deleteCategory(id: String)
}