package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.repository.fountain.FountainRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de actualizar campos específicos de una fuente en la base de datos.
 * * A diferencia de una sustitución completa, este caso de uso permite realizar
 * "Patch updates", modificando únicamente las claves proporcionadas en el mapa
 * de cambios, lo que optimiza el tráfico de red y la integridad de los datos.
 *
 * @property repository Repositorio de fuentes que gestiona la comunicación con la capa de datos.
 */
class UpdateFountainUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    /**
     * Ejecuta la actualización parcial de un documento de fuente.
     * * @param fountainId El identificador único del documento a modificar.
     * @param updates Un mapa que contiene los pares clave-valor de los campos a actualizar
     * (ej: "nom" to "Nueva Fuente", "descripcio" to "Nueva descripción").
     * @return [Result] que indica el éxito o fallo de la operación en el servidor.
     */
    suspend operator fun invoke(fountainId: String, updates: Map<String, Any>): Result<Unit> {
        return repository.updateFountain(fountainId, updates)
    }
}