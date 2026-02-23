package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.repository.CategoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseCategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    private val collection = firestore.collection("categories")

    override fun getCategories(): Flow<List<Category>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val categories = snapshot.toObjects(Category::class.java)
                trySend(categories)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getCategoryById(id: String): Category? {
        return collection.document(id).get().await().toObject(Category::class.java)
    }

    override suspend fun createCategory(category: Category) {
        val document = collection.document()
        val newCategory = category.copy(id = document.id)
        document.set(newCategory).await()
    }

    override suspend fun updateCategory(category: Category) {
        if (category.id.isNotEmpty()) {
            collection.document(category.id).set(category).await()
        }
    }

    override suspend fun deleteCategory(id: String) {
        collection.document(id).delete().await()
    }
}