package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.domain.model.fountain.StateFountain
import cat.copernic.aguamap1.domain.repository.auth.AuthRepository
import cat.copernic.aguamap1.domain.repository.fountain.FountainRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de la creación y registro de nuevas fuentes en el sistema.
 * Gestiona la asignación de metadatos iniciales, el rastreo de autoría y el
 * estado de validación según el rango del usuario que realiza la acción.
 *
 * @property repository Repositorio para la persistencia de fuentes en Firestore.
 * @property authRepository Repositorio de autenticación para identificar al creador.
 */
class CreateFountainUseCase @Inject constructor(
    private val repository: FountainRepository,
    private val authRepository: AuthRepository
) {
    /**
     * Ejecuta la lógica de creación de una fuente con valores por defecto y control de roles.
     *
     * @param fountain El objeto [Fountain] con los datos básicos (nombre, ubicación, descripción).
     * @param isUserAdmin Indica si el creador tiene privilegios de administrador.
     * @return [Result] que confirma si la fuente ha sido creada correctamente en el servidor.
     */
    suspend operator fun invoke(fountain: Fountain, isUserAdmin: Boolean): Result<Unit> {
        val currentUser = authRepository.getCurrentUserUid()

        // Registramos al creador como el primer votante positivo para evitar votos duplicados iniciales.
        val initialVoterList = if (currentUser != null) listOf(currentUser) else emptyList()

        // 1. Inicialización de campos de auditoría y estadísticas a valores neutros.
        val baseFountain = fountain.copy(
            dateCreated = java.util.Date(),
            ratingAverage = 0.0,
            totalRatings = 0,
            negativeVotes = 0,
            id = "" // El ID será generado automáticamente por el repositorio/Firestore.
        )

        // 2. Aplicación de Lógica de Negocio basada en Roles.
        val fountainToSave = if (isUserAdmin) {
            // Fuentes creadas por ADMIN se aceptan de inmediato y nacen con mayor peso positivo.
            baseFountain.copy(
                status = StateFountain.ACCEPTED,
                positiveVotes = 3,
                votedByPositive = initialVoterList,
                createdBy = currentUser ?: "ADMIN"
            )
        } else {
            // Fuentes de usuarios estándar quedan en espera y requieren validación comunitaria/admin.
            baseFountain.copy(
                status = StateFountain.PENDING,
                positiveVotes = 1,
                votedByPositive = initialVoterList,
                createdBy = currentUser ?: "ANONYMOUS"
            )
        }

        return repository.createFountain(fountainToSave)
    }
}