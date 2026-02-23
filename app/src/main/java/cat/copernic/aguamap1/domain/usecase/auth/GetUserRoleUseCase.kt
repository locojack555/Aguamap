package cat.copernic.aguamap1.domain.usecase.auth

import cat.copernic.aguamap1.domain.model.UserRole
import cat.copernic.aguamap1.domain.repository.AuthRepository
import javax.inject.Inject

class GetUserRoleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(uid: String): UserRole = repository.getUserRole(uid)
}