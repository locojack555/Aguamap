package cat.copernic.aguamap1.data.repository.category

import cat.copernic.aguamap1.domain.model.category.Category
import cat.copernic.aguamap1.domain.repository.category.CategoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementación de Firebase para el repositorio de categorías.
 * Maneja la persistencia y consulta de datos en la colección "categories" de Cloud Firestore.
 * Esta clase permite que la aplicación gestione las diferentes tipologías de fuentes disponibles.
 */
class FirebaseCategoryRepository @Inject constructor(
    // Instancia de Firestore inyectada para realizar operaciones en la base de datos
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    // Referencia a la colección específica de categorías en la base de datos
    private val collection = firestore.collection("categories")

    /**
     * Obtiene un flujo (Flow) de la lista de categorías.
     * Utiliza un SnapshotListener para que cualquier cambio en la base de datos (creación,
     * edición o borrado) se refleje automáticamente en la interfaz de usuario sin recargar.
     */
    override fun getCategories(): Flow<List<Category>> = callbackFlow {
        // El listener escucha cambios en tiempo real en toda la colección
        val listener = collection.addSnapshotListener { snapshot, error ->
            // Si ocurre un error de conexión o permisos, cerramos el flujo con la excepción
            if (error != null) {
                close(error) // Notifica el error al suscriptor del Flow
                return@addSnapshotListener
            }

            // Si el snapshot contiene datos, los mapeamos a objetos de la clase Category
            if (snapshot != null) {
                val categories = snapshot.toObjects(Category::class.java)
                trySend(categories) // Envía la lista actualizada de forma reactiva al suscriptor
            }
        }

        /**
         * Bloque de cierre: Se asegura de eliminar el listener de Firestore cuando el Flow
         * ya no se use (por ejemplo, al cambiar de pantalla) para evitar fugas de memoria.
         */
        awaitClose { listener.remove() }
    }

    /**
     * Busca una categoría específica por su identificador único.
     * @param id El ID del documento en Firestore.
     * @return El objeto Category si se encuentra, o null en caso de error o inexistencia.
     */
    override suspend fun getCategoryById(id: String): Category? {
        return try {
            // Realiza una petición única (get) y espera el resultado de forma suspendida
            collection.document(id).get().await().toObject(Category::class.java)
        } catch (e: Exception) {
            null // Devuelve null si el documento no existe o hay un fallo de red
        }
    }

    /**
     * Crea una nueva categoría en la base de datos.
     * Genera automáticamente un ID único de Firestore antes de guardar el documento.
     */
    override suspend fun createCategory(category: Category) {
        val document = collection.document() // Genera una referencia con ID automático
        // Copiamos la categoría asignándole el ID generado por Firestore
        val newCategory = category.copy(id = document.id)
        document.set(newCategory).await()
    }

    /**
     * Actualiza los datos de una categoría existente.
     * Solo procede si el objeto proporcionado ya cuenta con un ID válido.
     */
    override suspend fun updateCategory(category: Category) {
        if (category.id.isNotEmpty()) {
            // El método .set() sobrescribe el documento existente con la nueva información
            collection.document(category.id).set(category).await()
        }
    }

    /**
     * Elimina una categoría de la base de datos de forma permanente.
     * @param id El identificador del documento a borrar.
     */
    override suspend fun deleteCategory(id: String) {
        collection.document(id).delete().await()
    }
}