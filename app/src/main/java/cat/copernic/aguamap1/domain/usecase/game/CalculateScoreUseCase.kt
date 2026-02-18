package cat.copernic.aguamap1.domain.usecase.game

import cat.copernic.aguamap1.domain.model.GameSession
import cat.copernic.aguamap1.domain.repository.GameRepository
import kotlin.math.*

import javax.inject.Inject

class CalculateScoreUseCase @Inject constructor() {
    operator fun invoke(distance: Double): Int {
        return when {
            distance < 50 -> 1000
            distance < 100 -> 800
            distance < 500 -> 500
            distance < 1000 -> 200
            else -> 50
        }
    }
}

class CalculateDistanceUseCase @Inject constructor() {
    operator fun invoke(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371e3
        val phi1 = lat1 * PI / 180
        val phi2 = lat2 * PI / 180
        val dPhi = (lat2 - lat1) * PI / 180
        val dLambda = (lon2 - lon1) * PI / 180
        val a = sin(dPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}