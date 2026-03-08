package cat.copernic.aguamap1.domain.usecase.game

import cat.copernic.aguamap1.domain.repository.game.GameRepository
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import javax.inject.Inject

/**
 * Caso de Uso encargado de obtener una fuente aleatoria para el mini-juego.
 * Actúa como el selector de misiones, proporcionando el objetivo que el usuario
 * deberá localizar en el mapa para ganar puntos.
 *
 * @property repository Repositorio de juego que gestiona la lógica de selección en la capa de datos.
 */
class GetRandomFountainUseCase @Inject constructor(
    private val repository: GameRepository
) {
    /**
     * Ejecuta la petición para recuperar una fuente al azar.
     * * @return [Result] que contiene una [Fountain] aleatoria o nulo si no hay fuentes disponibles,
     * encapsulado para gestionar posibles errores de red o base de datos.
     */
    suspend operator fun invoke(): Result<Fountain?> {
        return repository.getRandomFountain()
    }
}