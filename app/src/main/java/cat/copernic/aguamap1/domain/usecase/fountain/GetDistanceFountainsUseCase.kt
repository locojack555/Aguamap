package cat.copernic.aguamap1.domain.usecase.fountain

import android.location.Location
import javax.inject.Inject

/**
 * Caso de Uso encargado de calcular la distancia geodésica entre dos puntos de coordenadas.
 * Se utiliza principalmente para mostrar al usuario a cuántos metros o kilómetros se encuentra
 * de una fuente específica o para validar la proximidad en el mini-juego.
 *
 * Al ser una operación puramente matemática y local, no requiere de repositorios externos.
 */
class GetDistanceFountainsUseCase @Inject constructor() {
    /**
     * Calcula la distancia en línea recta sobre la curvatura terrestre (WGS84).
     *
     * @param lat1 Latitud del punto de origen (ej. posición del usuario).
     * @param lon1 Longitud del punto de origen.
     * @param lat2 Latitud del punto de destino (ej. ubicación de la fuente).
     * @param lon2 Longitud del punto de destino.
     * @return La distancia calculada en metros (m).
     */
    operator fun invoke(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val results = FloatArray(1)
        // Utilizamos el método estático de Android que implementa la fórmula de Haversine/Vincenty
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toDouble()
    }
}