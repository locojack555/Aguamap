package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.domain.model.StateFountain
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.repository.FountainRepository
import javax.inject.Inject

class CreateFountainUseCase @Inject constructor(
    private val repository: FountainRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(fountain: Fountain, isUserAdmin: Boolean): Result<Unit> {
        val currentUser = authRepository.getCurrentUserUid()

        // 1. Aseguramos campos que SIEMPRE deben generarse al crear (por seguridad)
        val baseFountain = fountain.copy(
            dateCreated = java.util.Date(), // Fecha de creación real en este momento
            ratingAverage = 0.0,            // Nueva fuente empieza sin estrellas
            totalRatings = 0,               // Nueva fuente empieza sin votos de estrellas
            negativeVotes = 0,              // Nueva fuente empieza sin votos negativos
            id = ""                         // El ID lo suele generar Firebase al añadir
        )

        // 2. Aplicamos la lógica de roles (Status y Votos iniciales)
        val fountainToSave = if (isUserAdmin) {
            baseFountain.copy(
                status = StateFountain.ACCEPTED,
                positiveVotes = 3, // Los admins dan confianza inmediata
                createdBy = currentUser ?: "ADMIN"
            )
        } else {
            baseFountain.copy(
                status = StateFountain.PENDING,
                positiveVotes = 1, // El usuario que la propone es el primer voto positivo
                createdBy = currentUser ?: "ANONYMOUS"
            )
        }

        return repository.createFountain(fountainToSave)
    }
}