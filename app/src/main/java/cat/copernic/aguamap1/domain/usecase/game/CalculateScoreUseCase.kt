package cat.copernic.aguamap1.domain.usecase.game

import javax.inject.Inject
import kotlin.math.*

/**
 * Caso de Uso encargado de transformar la distancia entre el usuario y la fuente en una puntuación.
 * Implementa una lógica de niveles de recompensa para incentivar la precisión en el juego.
 */
class CalculateScoreUseCase @Inject constructor() {
    /**
     * Evalúa la distancia y asigna los puntos correspondientes.
     * * @param distance Distancia en metros calculada previamente.
     * @return Cantidad de puntos obtenidos (desde 50 hasta 1000).
     */
    operator fun invoke(distance: Double): Int {
        return when {
            distance < 50 -> 1000   // Excelente precisión
            distance < 100 -> 800   // Muy buena
            distance < 500 -> 500   // Buena
            distance < 1000 -> 200  // Aceptable
            else -> 50              // Fuera de rango óptimo
        }
    }
}

/**
 * Caso de Uso que implementa la fórmula de Haversine para calcular la distancia
 * entre dos puntos en una esfera (la Tierra).
 * * A diferencia del uso de APIs de Android (Location), esta implementación es
 * puramente matemática, lo que la hace ideal para pruebas unitarias (Unit Tests)
 * ya que no depende del framework de Android.
 */
class CalculateDistanceUseCase @Inject constructor() {
    /**
     * Calcula la distancia en metros entre dos coordenadas geográficas.
     * * @param lat1 Latitud origen.
     * @param lon1 Longitud origen.
     * @param lat2 Latitud destino.
     * @param lon2 Longitud destino.
     * @return Distancia en metros.
     */
    operator fun invoke(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371e3 // Radio medio de la Tierra en metros
        val phi1 = lat1 * PI / 180
        val phi2 = lat2 * PI / 180
        val dPhi = (lat2 - lat1) * PI / 180
        val dLambda = (lon2 - lon1) * PI / 180

        // Fórmula de Haversine
        val a = sin(dPhi / 2).pow(2) +
                cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }
}