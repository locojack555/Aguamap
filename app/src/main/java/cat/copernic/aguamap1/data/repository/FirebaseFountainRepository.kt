package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.UserStats
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.repository.FountainRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseFountainRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val authRepository: AuthRepository
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

            val userName = authRepository.getCurrentUserName() ?: "Usuario"
            updateUserFountainsCount(fountain.createdBy, userName, 1)

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
            // Primero obtenemos la fuente para saber quién la creó
            val fountainDoc = db.collection("fountains").document(fountainId).get().await()
            val createdBy = fountainDoc.getString("createdBy") ?: ""
            val userName = fountainDoc.getString("userName") ?: "Usuario"

            // Borramos la fuente
            db.collection("fountains").document(fountainId).delete().await()

            // 🔥 DECREMENTAR CONTADOR
            if (createdBy.isNotEmpty()) {
                updateUserFountainsCount(createdBy, userName, -1)
            }

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

            // 3. ACTUALIZAR CONTADOR AGREGADO DEL USUARIO
            updateUserCommentsCount(comment.userId, comment.userName, 1)

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
            // Primero obtenemos el comentario para saber el userId
            val commentDoc = db.collection("fountains")
                .document(fountainId)
                .collection("comments")
                .document(commentId)
                .get()
                .await()

            val userId = commentDoc.getString("userId") ?: ""
            val userName = commentDoc.getString("userName") ?: "Usuario"

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

            if (userId.isNotEmpty()) {
                updateUserCommentsCount(userId, userName, -1)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ACTUALIZAR CONTADOR DE FUENTES
    override suspend fun updateUserFountainsCount(
        userId: String,
        userName: String,
        increment: Int
    ): Result<Unit> {
        return try {
            val userStatsRef = db.collection("userStats").document(userId)

            val data = mapOf(
                "userId" to userId,
                "userName" to userName,
                "fountainsCount" to FieldValue.increment(increment.toLong()),
                "lastUpdated" to FieldValue.serverTimestamp()
            )

            userStatsRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ACTUALIZAR CONTADOR DE COMENTARIOS
    override suspend fun updateUserCommentsCount(
        userId: String,
        userName: String,
        increment: Int
    ): Result<Unit> {
        return try {
            val userStatsRef = db.collection("userStats").document(userId)

            val data = mapOf(
                "userId" to userId,
                "userName" to userName,
                "commentsCount" to FieldValue.increment(increment.toLong()),
                "lastUpdated" to FieldValue.serverTimestamp()
            )

            userStatsRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // OBTENER CONTADOR DE FUENTES
    override suspend fun getUserFountainsCount(userId: String): Int {
        return try {
            val doc = db.collection("userStats").document(userId).get().await()
            val count = doc.getLong("fountainsCount")?.toInt() ?: 0
            count
        } catch (e: Exception) {
            0
        }
    }

    // OBTENER CONTADOR DE COMENTARIOS
    override suspend fun getUserCommentsCount(userId: String): Int {
        return try {
            val doc = db.collection("userStats").document(userId).get().await()
            val count = doc.getLong("commentsCount")?.toInt() ?: 0
            count
        } catch (e: Exception) {
            0
        }
    }

    override fun observeUserStats(userId: String): Flow<UserStats?> = callbackFlow {
        val docRef = db.collection("userStats").document(userId)

        val subscription = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val stats = UserStats(
                    userId = snapshot.getString("userId") ?: "",
                    userName = snapshot.getString("userName") ?: "",
                    fountainsCount = snapshot.getLong("fountainsCount")?.toInt() ?: 0,
                    commentsCount = snapshot.getLong("commentsCount")?.toInt() ?: 0,
                    lastUpdated = snapshot.getTimestamp("lastUpdated")
                )
                trySend(stats)
            } else {
                trySend(null)
            }
        }

        awaitClose { subscription.remove() }
    }
}