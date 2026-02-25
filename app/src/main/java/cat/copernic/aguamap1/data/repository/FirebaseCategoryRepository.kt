package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.repository.CategoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementación de Firebase para el repositorio de categorías.
 * Maneja la persistencia de datos en la colección "categories" de Firestore.
 */
class FirebaseCategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    private val collection = firestore.collection("categories")

    override fun getCategories(): Flow<List<Category>> = callbackFlow {
        // El listener escucha cambios en tiempo real
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Notifica el error al suscriptor
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val categories = snapshot.toObjects(Category::class.java)
                trySend(categories) // Envía la lista actualizada al Flow
            }
        }
        // Se asegura de eliminar el listener cuando el Flow ya no se use para evitar fugas de memoria
        awaitClose { listener.remove() }
    }

    override suspend fun getCategoryById(id: String): Category? {
        return try {
            collection.document(id).get().await().toObject(Category::class.java)
        } catch (e: Exception) {
            null // Devuelve null si el documento no existe o hay error de red
        }
    }

    override suspend fun createCategory(category: Category) {
        val document = collection.document() // Genera un ID automático en Firestore
        val newCategory = category.copy(id = document.id)
        document.set(newCategory).await()
    }

    override suspend fun updateCategory(category: Category) {
        if (category.id.isNotEmpty()) {
            // El método .set() sobrescribe el documento con los nuevos datos
            collection.document(category.id).set(category).await()
        }
    }

    override suspend fun deleteCategory(id: String) {
        collection.document(id).delete().await()
    }
}