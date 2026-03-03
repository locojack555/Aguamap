package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.repository.FountainRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseFountainRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val authRepository: AuthRepository
) : FountainRepository {

    // Método para el Mapa (Reactivo)
    override fun fetchSources(lat: Double?, lng: Double?): Flow<Result<List<Fountain>>> = flow {
        try {
            if (lat == null || lng == null) {
                val snapshot = db.collection("fountains").get().await()
                val fountains = snapshot.documents.mapNotNull { it.toObject(Fountain::class.java)?.copy(id = it.id) }
                emit(Result.success(fountains))
                return@flow
            }

            val center = com.firebase.geofire.GeoLocation(lat, lng)
            val radiusInM = 8000.0
            val bounds = com.firebase.geofire.GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
            val tasks = bounds.map { b ->
                db.collection("fountains")
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash)
                    .get()
            }

            val snapshots = com.google.android.gms.tasks.Tasks.whenAllSuccess<com.google.firebase.firestore.QuerySnapshot>(tasks).await()
            val filtered = snapshots.flatMap { snap ->
                snap.documents.mapNotNull { doc ->
                    val f = doc.toObject(Fountain::class.java)?.copy(id = doc.id)
                    if (f != null) {
                        val loc = com.firebase.geofire.GeoLocation(f.latitude, f.longitude)
                        if (com.firebase.geofire.GeoFireUtils.getDistanceBetween(loc, center) <= radiusInM) f else null
                    } else null
                }
            }
            emit(Result.success(filtered))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // NUEVO MÉTODO: Directo para el juego (Evita el error de Flow Aborted)
    override suspend fun getAllFountainsDirect(): Result<List<Fountain>> {
        return try {
            val snapshot = db.collection("fountains").get().await()
            val fountains = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Fountain::class.java)?.copy(id = doc.id)
            }
            Result.success(fountains)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Resto de métodos (getFountainById, create, update, etc.) se mantienen igual...
    override suspend fun getFountainById(fountainId: String): Result<Fountain> {
        return try {
            val doc = db.collection("fountains").document(fountainId).get().await()
            val f = doc.toObject(Fountain::class.java)?.copy(id = doc.id)
            if (f != null) Result.success(f) else Result.failure(Exception("Not found"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun createFountain(fountain: Fountain): Result<Unit> {
        return try {
            val hash = com.firebase.geofire.GeoFireUtils.getGeoHashForLocation(com.firebase.geofire.GeoLocation(fountain.latitude, fountain.longitude))
            val ref = db.collection("fountains").document()
            ref.set(fountain.copy(id = ref.id, geohash = hash)).await()
            updateUserFountainsCount(fountain.createdBy, authRepository.getCurrentUserName() ?: "User", 1)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateFountain(fountainId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("fountains").document(fountainId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteFountain(fountainId: String): Result<Unit> {
        return try {
            val doc = db.collection("fountains").document(fountainId).get().await()
            val uid = doc.getString("createdBy") ?: ""
            db.collection("fountains").document(fountainId).delete().await()
            if (uid.isNotEmpty()) updateUserFountainsCount(uid, "User", -1)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun addComment(fountainId: String, comment: Comment): Result<Unit> {
        return try {
            val fRef = db.collection("fountains").document(fountainId)
            val cRef = fRef.collection("comments").document()
            db.runTransaction { t ->
                t.set(cRef, comment.copy(id = cRef.id))
                t.update(fRef, "totalRatings", FieldValue.increment(1))
            }.await()
            updateUserCommentsCount(comment.userId, comment.userName, 1)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun fetchComments(fountainId: String): Flow<Result<List<Comment>>> = callbackFlow {
        val sub = db.collection("fountains").document(fountainId).collection("comments")
            .orderBy("timestamp").addSnapshotListener { sn, er ->
                if (er != null) trySend(Result.failure(er))
                else trySend(Result.success(sn?.documents?.mapNotNull { it.toObject(Comment::class.java)?.copy(id = it.id) } ?: emptyList()))
            }
        awaitClose { sub.remove() }
    }

    override suspend fun updateComment(fId: String, cId: String, up: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("fountains").document(fId).collection("comments").document(cId).update(up).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteComment(fId: String, cId: String): Result<Unit> {
        return try {
            val fRef = db.collection("fountains").document(fId)
            val cRef = fRef.collection("comments").document(cId)
            val doc = cRef.get().await()
            val uid = doc.getString("userId") ?: ""
            db.runTransaction { t ->
                t.delete(cRef)
                t.update(fRef, "totalRatings", FieldValue.increment(-1))
            }.await()
            if (uid.isNotEmpty()) updateUserCommentsCount(uid, "User", -1)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun reportComment(fId: String, cId: String, re: String): Result<Unit> {
        return try {
            val cRef = db.collection("fountains").document(fId).collection("comments").document(cId)
            db.runBatch { b ->
                b.update(cRef, "reported", true)
                b.set(db.collection("reportsComments").document(), mapOf("fountainId" to fId, "commentId" to cId, "reason" to re, "timestamp" to System.currentTimeMillis()))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateUserFountainsCount(id: String, name: String, inc: Int): Result<Unit> {
        val data = mapOf("userId" to id, "userName" to name, "fountainsCount" to FieldValue.increment(inc.toLong()))
        return try { db.collection("userStats").document(id).set(data, SetOptions.merge()).await(); Result.success(Unit) } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateUserCommentsCount(id: String, name: String, inc: Int): Result<Unit> {
        val data = mapOf("userId" to id, "userName" to name, "commentsCount" to FieldValue.increment(inc.toLong()))
        return try { db.collection("userStats").document(id).set(data, SetOptions.merge()).await(); Result.success(Unit) } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getUserFountainsCount(id: String): Int = try { db.collection("userStats").document(id).get().await().getLong("fountainsCount")?.toInt() ?: 0 } catch (e: Exception) { 0 }
    override suspend fun getUserCommentsCount(id: String): Int = try { db.collection("userStats").document(id).get().await().getLong("commentsCount")?.toInt() ?: 0 } catch (e: Exception) { 0 }
}