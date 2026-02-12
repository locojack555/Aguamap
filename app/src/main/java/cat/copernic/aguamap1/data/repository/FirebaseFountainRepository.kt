package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseFountainRepository : FountainRepository {
    private val db: FirebaseFirestore = Firebase.firestore

    override suspend fun fetchSources(): Result<List<Fountain>> {
        return try {
            val snapshot = db.collection("fountains").get().await()
            val list = snapshot.toObjects(Fountain::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
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