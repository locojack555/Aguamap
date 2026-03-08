package cat.copernic.aguamap1.data.repository.status

import cat.copernic.aguamap1.domain.repository.status.RealtimeStatusRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Implementación del repositorio de estado en tiempo real utilizando Firebase Realtime Database.
 * A diferencia de Firestore, esta base de datos es óptima para cambios de alta frecuencia
 * y baja latencia, como saber si una fuente está operativa o fuera de servicio en el momento.
 */
class RealtimeStatusRepositoryImpl @Inject constructor( // Le añadimos "Impl" al nombre para seguir la convención de Clean Architecture
    // Instancia de la base de datos en tiempo real inyectada
    private val db: FirebaseDatabase
) : RealtimeStatusRepository { // Implementamos la interfaz definida en la capa de dominio

    /**
     * Escucha los cambios de estado de una fuente específica de forma reactiva.
     * @param fountainId El identificador único de la fuente.
     * @return Un Flow que emite true si está operativa o false si no lo está.
     */
    override fun getFountainStatus(fountainId: String): Flow<Boolean> = callbackFlow {
        // Obtenemos la referencia al nodo específico dentro de la estructura de la base de datos
        val ref = db.getReference("fountain_status/$fountainId")

        // Creamos un listener para detectar cambios en el valor del nodo
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Intentamos enviar el nuevo estado al flujo; si es nulo, asumimos que está operativa (true)
                trySend(snapshot.getValue(Boolean::class.java) ?: true)
            }

            override fun onCancelled(error: DatabaseError) {
                // Si la conexión se cancela o falla, cerramos el flujo con la excepción correspondiente
                close(error.toException())
            }
        }

        // Registramos el listener en la referencia de la base de datos
        ref.addValueEventListener(listener)

        /**
         * Bloque de cierre: Se asegura de remover el listener de la base de datos
         * cuando el suscriptor deja de escuchar el Flow, evitando fugas de memoria y consumo de red.
         */
        awaitClose { ref.removeEventListener(listener) }
    }

    /**
     * Actualiza el estado operativo de una fuente de forma asíncrona.
     * @param fountainId El identificador único de la fuente.
     * @param isOperational El nuevo estado (true = operativa, false = fuera de servicio).
     */
    override fun updateFountainStatus(fountainId: String, isOperational: Boolean) {
        // Accede a la ruta del nodo y establece el nuevo valor booleano
        db.getReference("fountain_status/$fountainId").setValue(isOperational)
    }
}