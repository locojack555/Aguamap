package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de la eliminación permanente de una fuente de la base de datos.
 * Esta acción es irreversible y suele utilizarse para descartar fuentes duplicadas,
 * inexistentes o que no cumplen con los estándares de calidad de la comunidad.
 *
 * @property repository Repositorio de fuentes que gestiona la comunicación con Firestore.
 */
class DeleteFountainUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    /**
     * Ejecuta el borrado de la fuente identificada por su ID único.
     * * @param fountainId El identificador de la fuente que se desea eliminar.
     * @return [Result] que indica si la operación de borrado se completó con éxito.
     */
    suspend operator fun invoke(fountainId: String): Result<Unit> {
        return repository.deleteFountain(fountainId)
    }
}