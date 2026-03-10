package cat.copernic.aguamap1.domain.repository.status

import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Dominio para la gestión del estado operativo de las fuentes en tiempo real.
 * Se separa del repositorio principal de fuentes para permitir el uso de tecnologías
 * de baja latencia (como Firebase Realtime Database) específicas para estados binarios.
 */
interface RealtimeStatusRepository {

    /**
     * Escucha de forma reactiva si una fuente específica está operativa o no.
     * @param fountainId El identificador único de la fuente a monitorizar.
     * @return Un [Flow] que emite 'true' si la fuente funciona correctamente
     * o 'false' si está fuera de servicio por mantenimiento o avería.
     */
    fun getFountainStatus(fountainId: String): Flow<Boolean>

    /**
     * Actualiza el estado de disponibilidad de una fuente.
     * Permite a los usuarios o administradores reportar si una fuente ha dejado de
     * funcionar o si ya ha sido reparada.
     * @param fountainId El identificador único de la fuente.
     * @param isOperational El nuevo estado de la fuente (true = operativa, false = averiada).
     */
    fun updateFountainStatus(fountainId: String, isOperational: Boolean)
}