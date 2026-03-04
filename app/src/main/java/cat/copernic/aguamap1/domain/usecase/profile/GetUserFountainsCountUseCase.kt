package cat.copernic.aguamap1.domain.usecase.profile

import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de contabilizar el número total de fuentes creadas por un usuario.
 * Esta métrica refleja la contribución directa del usuario a la expansión del mapa
 * y suele utilizarse para otorgar insignias de "Explorador" o "Cartógrafo".
 *
 * @property repository Repositorio de fuentes que filtra los documentos por el campo 'createdBy'.
 */
class GetUserFountainsCountUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    /**
     * Ejecuta el recuento de fuentes cuya autoría pertenece al usuario especificado.
     *
     * @param userId El identificador único del usuario (UID) cuyas fuentes se quieren contar.
     * @return El número total de fuentes registradas por el usuario en el sistema.
     */
    suspend operator fun invoke(userId: String): Int {
        return repository.getUserFountainsCount(userId)
    }
}