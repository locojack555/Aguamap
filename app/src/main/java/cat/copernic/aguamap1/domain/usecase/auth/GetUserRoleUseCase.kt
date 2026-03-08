package cat.copernic.aguamap1.domain.usecase.auth

import cat.copernic.aguamap1.domain.model.user.UserRole
import cat.copernic.aguamap1.domain.repository.auth.AuthRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de obtener el nivel de privilegios (rol) de un usuario específico.
 * Se utiliza principalmente tras el inicio de sesión para decidir si se debe
 * mostrar el panel de administración o restringir ciertas funcionalidades.
 *
 * @property repository El repositorio de autenticación que gestiona la comunicación con la base de datos.
 */
class GetUserRoleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Ejecuta la lógica para recuperar el rol del usuario.
     * * @param uid El identificador único del usuario del cual queremos conocer el rol.
     * @return El [UserRole] asignado al usuario (USER o ADMIN).
     */
    suspend operator fun invoke(uid: String): UserRole = repository.getUserRole(uid)
}