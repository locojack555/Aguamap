package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de recuperar la información detallada de una fuente específica.
 * Se utiliza principalmente para cargar la pantalla de detalles, la sección de comentarios
 * o para verificar el estado de una fuente antes de realizar una acción de moderación.
 *
 * @property fountainRepository Repositorio que gestiona el acceso a la colección de fuentes en Firestore.
 */
class GetFountainByIdUseCase @Inject constructor(
    private val fountainRepository: FountainRepository
) {
    /**
     * Ejecuta la consulta para obtener una fuente por su identificador único.
     *
     * @param fountainId El ID del documento de la fuente en la base de datos.
     * @return [Result] que contiene el objeto [Fountain] si se encuentra, o una excepción en caso de error.
     */
    suspend operator fun invoke(fountainId: String): Result<Fountain> {
        return fountainRepository.getFountainById(fountainId)
    }
}