package cat.copernic.aguamap1.domain.usecase.profile

import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de contabilizar el número total de aportaciones (comentarios)
 * que un usuario específico ha realizado en toda la plataforma.
 * * Es un indicador clave de la actividad social y el compromiso del usuario
 * con la calidad de la información sobre las fuentes.
 *
 * @property repository Repositorio de fuentes que gestiona las subcolecciones de comentarios.
 */
class GetUserCommentsCountUseCase @Inject constructor(
    private val repository: FountainRepository
) {
    /**
     * Ejecuta el recuento de comentarios asociados a un ID de usuario.
     * * @param userId El identificador único del usuario (UID) cuyas aportaciones se quieren contar.
     * @return El número total de comentarios activos (no borrados) realizados por el usuario.
     */
    suspend operator fun invoke(userId: String): Int {
        return repository.getUserCommentsCount(userId)
    }
}