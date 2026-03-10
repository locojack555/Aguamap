package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.comment.Comment
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.domain.repository.fountain.FountainRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de Uso central para la recuperación de datos de fuentes y sus interacciones.
 * Proporciona flujos de datos en tiempo real para la interfaz de usuario y
 * métodos de acceso directo para la lógica interna del sistema.
 *
 * @property repository Repositorio de fuentes que actúa como fuente de verdad (Firestore).
 */
class GetFountainsUseCase @Inject constructor(private val repository: FountainRepository) {

    /**
     * Obtiene un flujo constante de fuentes, opcionalmente filtradas por proximidad.
     * Al devolver un [Flow], la UI se actualizará automáticamente ante cualquier
     * cambio en la base de datos (nuevas fuentes, cambios de estado, etc.).
     *
     * @param lat Latitud opcional del usuario para el centrado/filtrado.
     * @param lng Longitud opcional del usuario para el centrado/filtrado.
     * @return [Flow] que emite una lista de [Fountain] envuelta en [Result].
     */
    operator fun invoke(lat: Double?, lng: Double?): Flow<Result<List<Fountain>>> {
        return repository.fetchSources(lat, lng)
    }

    /**
     * Recupera todas las fuentes de forma inmediata mediante una petición única (One-shot).
     * Ideal para procesos que no requieren actualizaciones en tiempo real, como
     * la selección de la "fuente del día" en el mini-juego.
     *
     * @return [Result] con la lista completa de fuentes disponibles.
     */
    suspend fun executeOnce(): Result<List<Fountain>> {
        return repository.getAllFountainsDirect()
    }

    /**
     * Proporciona un flujo de comentarios asociados a una fuente específica.
     * Permite que la sección de reseñas de una fuente sea dinámica y refleje
     * nuevas aportaciones o ediciones de forma instantánea.
     *
     * @param fountainId El identificador único de la fuente.
     * @return [Flow] que emite la lista de [Comment] actualizada en tiempo real.
     */
    fun fetchComments(fountainId: String): Flow<Result<List<Comment>>> {
        return repository.fetchComments(fountainId)
    }
}