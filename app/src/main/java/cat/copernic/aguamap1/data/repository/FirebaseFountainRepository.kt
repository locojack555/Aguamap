package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
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
}