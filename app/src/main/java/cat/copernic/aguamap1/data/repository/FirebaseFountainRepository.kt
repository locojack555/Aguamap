package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseFountainRepository @Inject constructor(
    private val db: FirebaseFirestore
) : FountainRepository {

    override fun fetchSources(): Flow<Result<List<Fountain>>> = callbackFlow {
        val subscription = db.collection("fountains")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val fountains = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Fountain::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(fountains))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun createFountain(fountain: Fountain): Result<Unit> {
        return try {
            val newDocRef = db.collection("fountains").document()
            val fountainWithId = fountain.copy(id = newDocRef.id)
            newDocRef.set(fountainWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFountain(
        fountainId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            val finalUpdates = updates.toMutableMap()

            // Soporte para incrementar contadores (votos o ratings) de forma atómica
            // Si el mapa contiene "totalRatings", "positiveVotes" o "negativeVotes", usamos increment
            val incrementableFields = listOf("totalRatings", "positiveVotes", "negativeVotes")

            incrementableFields.forEach { field ->
                if (updates.containsKey(field)) {
                    val value = (updates[field] as? Number)?.toLong() ?: 0L
                    finalUpdates[field] = FieldValue.increment(value)
                }
            }

            db.collection("fountains")
                .document(fountainId)
                .update(finalUpdates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFountain(fountainId: String): Result<Unit> {
        return try {
            db.collection("fountains")
                .document(fountainId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- GESTIÓN DE COMENTARIOS (SUBCOLECCIÓN) ---

    override suspend fun addComment(fountainId: String, comment: Comment): Result<Unit> {
        return try {
            val ref = db.collection("fountains")
                .document(fountainId)
                .collection("comments")
                .document()

            // 1. Guardamos el comentario
            ref.set(comment.copy(id = ref.id)).await()

            // 2. Incrementamos el contador en el documento padre
            db.collection("fountains")
                .document(fountainId)
                .update("totalRatings", FieldValue.increment(1))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun fetchComments(fountainId: String): Flow<Result<List<Comment>>> = callbackFlow {
        val subscription = db.collection("fountains")
            .document(fountainId)
            .collection("comments")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(comments))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateComment(
        fountainId: String,
        commentId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            db.collection("fountains")
                .document(fountainId)
                .collection("comments")
                .document(commentId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(fountainId: String, commentId: String): Result<Unit> {
        return try {
            // 1. Borramos el comentario de la subcolección
            db.collection("fountains")
                .document(fountainId)
                .collection("comments")
                .document(commentId)
                .delete()
                .await()

            // 2. Decrementamos el contador totalRatings en el documento padre
            db.collection("fountains")
                .document(fountainId)
                .update("totalRatings", FieldValue.increment(-1))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}