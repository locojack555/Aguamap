package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.repository.RealtimeStatusRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class RealtimeStatusRepositoryImpl @Inject constructor( // Le añadimos "Impl" al nombre
    private val db: FirebaseDatabase
) : RealtimeStatusRepository { // Implementamos la interfaz

    override fun getFountainStatus(fountainId: String): Flow<Boolean> = callbackFlow {
        val ref = db.getReference("fountain_status/$fountainId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) ?: true)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun updateFountainStatus(fountainId: String, isOperational: Boolean) {
        db.getReference("fountain_status/$fountainId").setValue(isOperational)
    }
}