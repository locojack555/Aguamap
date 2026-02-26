package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.Report
import cat.copernic.aguamap1.domain.model.UserStats
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.repository.FountainRepository
import com.google.firebase.database.core.Repo
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
            db.collection("fountains")
                .document(fountainId)
                .update(updates)
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

    // --- GESTIÓN DE COMENTARIOS ---

    override suspend fun addComment(fountainId: String, comment: Comment): Result<Unit> {
        return try {
            val fountainRef = db.collection("fountains").document(fountainId)
            val commentRef = fountainRef.collection("comments").document()

            // Usamos una transacción para asegurar que el contador y el comentario van a la par
            db.runTransaction { transaction ->
                // 1. Guardar comentario
                transaction.set(commentRef, comment.copy(id = commentRef.id))
                // 2. Incrementar totalRatings
                transaction.update(fountainRef, "totalRatings", FieldValue.increment(1))
            }.await()

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

            android.util.Log.d("FIREBASE_OK", "Comentario $commentId actualizado con éxito")
            Result.success(Unit)
        } catch (e: Exception) {
            // ESTO TE DIRÁ EL ERROR REAL EN EL LOGCAT
            android.util.Log.e("FIREBASE_ERROR", "Error actualizando comentario: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(fountainId: String, commentId: String): Result<Unit> {
        return try {
            val fountainRef = db.collection("fountains").document(fountainId)
            val commentRef = fountainRef.collection("comments").document(commentId)
            val commentDoc = db.collection("fountains")
                .document(fountainId)
                .collection("comments")
                .document(commentId)
                .get()
                .await()

            val userId = commentDoc.getString("userId") ?: ""
            val userName = commentDoc.getString("userName") ?: "Usuario"


            db.runTransaction { transaction ->
                transaction.delete(commentRef)
                transaction.update(fountainRef, "totalRatings", FieldValue.increment(-1))
            }.await()

            //Decretar total comentarios
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

    // Método para el futuro Panel de Admin
    // --- GESTIÓN DE REPORTES ---

    override suspend fun reportComment(
        fountainId: String,
        commentId: String,
        reason: String // Cambiado de userId a reason para coincidir con el uso del ViewModel
    ): Result<Unit> {
        return try {
            // 1. Marcamos el flag en el comentario (para que el icono cambie a naranja en la UI)
            val commentRef = db.collection("fountains")
                .document(fountainId)
                .collection("comments")
                .document(commentId)

            // 2. Creamos un documento en una colección global 'reports_comments' para el Admin
            val reportData = hashMapOf(
                "fountainId" to fountainId,
                "commentId" to commentId,
                "reason" to reason,
                "timestamp" to System.currentTimeMillis(),
                "type" to "COMMENT_REPORT"
            )

            db.runBatch { batch ->
                batch.update(commentRef, "isReported", true)
                // Añadimos el reporte a una colección independiente que el admin pueda vigilar
                val newReportRef = db.collection("reports_comments").document()
                batch.set(newReportRef, reportData)
            }.await()

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
}