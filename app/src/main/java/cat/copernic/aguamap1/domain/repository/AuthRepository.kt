package cat.copernic.aguamap1.domain.repository

import cat.copernic.aguamap1.domain.model.User
import cat.copernic.aguamap1.domain.model.UserRole

/**
 * Interfaz que define las operaciones de autenticación y gestión de usuarios.
 * Esta interfaz reside en la capa de Dominio para mantener la independencia tecnológica.
 */
interface AuthRepository {

    /** Verifica si existe una sesión de usuario activa. */
    fun isUserLoggedIn(): Boolean

    /** Obtiene el identificador único (UID) del usuario actual. */
    fun getCurrentUserUid(): String?

    /** * Inicia sesión con credenciales.
     * @return Result con true si tiene éxito, o excepción si falla.
     */
    suspend fun login(email: String, password: String): Result<Boolean>

    /** Envía un correo de recuperación de contraseña. */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /** Actualiza la preferencia de idioma del usuario en Firestore. */
    suspend fun updateLanguagePreference(uid: String, languageCode: String): Result<Unit>

    /** Obtiene los datos del usuario desde Firestore. */
    suspend fun getUserData(uid: String): Result<User>

    /** * Registra un nuevo usuario en el sistema de autenticación.
     * Normalmente requiere una posterior verificación de email.
     */
    suspend fun signUp(email: String, password: String): Result<Boolean>

    /** Comprueba si el perfil del usuario ya está creado en la base de datos. */
    suspend fun checkIfUserExists(uid: String): Boolean

    /** * Crea el perfil de usuario en la base de datos tras verificar el email.
     * @param name Nombre que el usuario desea mostrar.
     */
    suspend fun completeRegistration(name: String): Result<Boolean>

    /** Obtiene el rol asignado al usuario (ej. ADMIN, USER). */
    suspend fun getUserRole(uid: String): UserRole

    /** Obtiene el nombre del usuario almacenado en su perfil de base de datos. */
    suspend fun getCurrentUserName(): String?

    /** Obtiene el email almacenado en el perfil de la base de datos. */
    suspend fun getCurrentUserEmail(): String?

    /** Obtiene el email directamente del proveedor de autenticación. */
    suspend fun getCurrentUserEmailAuth(): String?

    /** * Actualiza los datos básicos del perfil en la base de datos.
     */
    suspend fun updateUserProfile(userId: String, nombre: String, email: String): Result<Unit>

    /** Solicita el cambio de email en el sistema de autenticación. */
    suspend fun updateUserEmail(newEmail: String): Result<Unit>

    /** Actualiza el nombre de pantalla en el perfil de autenticación. */
    suspend fun updateUserName(newName: String): Result<Unit>

    /** Sincroniza los datos del usuario local con el servidor de autenticación. */
    suspend fun refreshUser()

    /** Cierra la sesión activa del usuario. */
    suspend fun signOut()

    /** Verifica si el usuario ha pulsado el link de confirmación en su email. */
    suspend fun isEmailVerified(): Boolean

    /** * Busca el nombre de un usuario específico mediante su UID.
     * Útil para mostrar quién publicó un comentario.
     */
    suspend fun getUserNameById(uid: String): Result<String>

    /** Actualiza la URL de la imagen de perfil en la base de datos. */
    suspend fun updateUserProfilePicture(userId: String, imageUrl: String): Result<Unit>

    /** Recupera la URL de la imagen de perfil del usuario actual. */
    suspend fun getCurrentUserProfilePicture(): String?
}