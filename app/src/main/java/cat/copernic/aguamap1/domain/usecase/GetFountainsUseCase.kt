package cat.copernic.aguamap1.domain.usecase

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository

class GetFountainsUseCase(private val repository: FountainRepository) {

    /**
     * El operador 'invoke' permite llamar a la clase como si fuera una función:
     * val fuentes = getFountainsUseCase()
     */
    suspend operator fun invoke(): List<Fountain> {
        // Aquí podrías añadir lógica de negocio extra en el futuro,
        // como filtrar fuentes que no estén operativas o por distancia.
        return repository.fetchSources()
    }
}