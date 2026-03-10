package cat.copernic.aguamap1.data.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Proveedor de ubicación predeterminado que utiliza los Google Play Services (Fused Location Provider).
 * Esta clase se encarga de obtener la posición GPS del usuario de manera eficiente.
 */
class DefaultLocationProvider @Inject constructor(
    // Cliente de Google para acceder a los servicios de ubicación
    private val fusedLocationClient: FusedLocationProviderClient
) {
    /**
     * Crea un flujo (Flow) de actualizaciones de ubicación en tiempo real.
     * Utiliza callbackFlow para convertir los callbacks de Google en una secuencia reactiva de Kotlin.
     */
    @SuppressLint("MissingPermission") // Se asume que los permisos se gestionan en la capa de UI
    fun getLocationUpdates(): Flow<Location> = callbackFlow {

        // 1. Intentamos obtener la última ubicación conocida inmediatamente.
        // Esto permite que la app muestre la posición actual del usuario al instante,
        // evitando el salto visual desde coordenadas por defecto al arrancar.
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let { trySend(it) }
        }

        /**
         * Configuración de la petición de ubicación:
         * - Prioridad Alta Precisión: Usa GPS, Wi-Fi y redes móviles.
         * - Intervalo: Cada 5 segundos (5000ms).
         * - Intervalo mínimo: 3 segundos (3000ms).
         * - Distancia mínima: Se activa solo si el usuario se mueve más de 2 metros.
         */
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(3000)
            .setMinUpdateDistanceMeters(2f)
            .build()

        /**
         * Callback que recibe los resultados de ubicación de Google.
         * Cada vez que hay una nueva posición, se envía (trySend) al flujo de la aplicación.
         */
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }

        // Inicia la escucha de actualizaciones en el hilo principal (Looper)
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        /**
         * Bloque de cierre: Se ejecuta cuando el Flow se cancela o la pantalla se destruye.
         * Es fundamental para detener el GPS y ahorrar batería en el dispositivo.
         */
        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}