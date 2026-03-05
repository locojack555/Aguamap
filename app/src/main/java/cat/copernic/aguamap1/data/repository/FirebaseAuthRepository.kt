package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.User
import cat.copernic.aguamap1.domain.model.UserRole
import cat.copernic.aguamap1.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementación del repositorio de autenticación utilizando Firebase.
 * Gestiona tanto la autenticación de usuarios (Firebase Auth) como el almacenamiento
 * de sus datos de perfil adicionales (Cloud Firestore).
 */
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    /**
     * Comprueba de forma síncrona si hay una sesión de usuario activa.
     */
    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Actualiza la preferencia de idioma del usuario en Firestore  .
     */
    override suspend fun updateLanguagePreference(uid: String, languageCode: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .update("language", languageCode).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el usuario completo para conocer su idioma preferido al hacer login.
     */
    override suspend fun getUserData(uid: String): Result<User> = try {
        val document = firestore.collection("users").document(uid).get().await()
        val user = document.toObject(User::class.java)
        if (user != null) Result.success(user)
        else Result.failure(Exception("Usuario no encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Realiza el inicio de sesión con email y contraseña.
     * Incluye una validación de seguridad: si el email no ha sido verificado,
     * cierra la sesión y devuelve un error específico.
     */
    override suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Forzamos la recarga del usuario para obtener el estado actualizado de isEmailVerified
                user.reload().await()
                if (user.isEmailVerified) {
                    Result.success(true)
                } else {
                    auth.signOut()
                    Result.failure(Exception("EMAIL_NOT_VERIFIED"))
                }
            } else {
                Result.failure(Exception("USER_NOT_FOUND"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Envía un correo electrónico de recuperación de contraseña al email proporcionado.
     */
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registra un nuevo usuario en Firebase Auth.
     * Tras el registro, envía el correo de verificación y cierra la sesión para obligar
     * al usuario a validar su cuenta antes de entrar.
     */
    override suspend fun signUp(email: String, password: String): Result<Boolean> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.sendEmailVerification()?.await()
            auth.signOut()
            Result.success(true)
        } catch (e: FirebaseAuthUserCollisionException) {
            // Error específico si el email ya está registrado
            Result.failure(Exception("ERROR_DUPLICATED"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Comprueba si el documento del usuario ya existe en la colección 'users' de Firestore.
     */
    override suspend fun checkIfUserExists(uid: String): Boolean {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Obtiene el identificador único (UID) del usuario autenticado actualmente.
     */
    override fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Recupera el rol del usuario (ADMIN, USER) desde Firestore.
     * Si no se encuentra o hay error, se asigna el rol 'USER' por defecto.
     */
    override suspend fun getUserRole(uid: String): UserRole {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val roleString = document.getString("role") ?: "USER"
            UserRole.valueOf(roleString)
        } catch (e: Exception) {
            UserRole.USER
        }
    }

    /**
     * Finaliza el proceso de registro creando el perfil del usuario en Firestore.
     * Solo se ejecuta si el email ha sido verificado satisfactoriamente.
     */
    override suspend fun completeRegistration(name: String): Result<Boolean> {
        return try {
            val user = auth.currentUser
            user?.reload()?.await()
            if (user != null && user.isEmailVerified) {
                val userMap = hashMapOf(
                    "uid" to user.uid,
                    "nom" to name,
                    "email" to user.email,
                    "role" to UserRole.USER.name
                )
                firestore.collection("users")
                    .document(user.uid)
                    .set(userMap)
                    .await()
                Result.success(true)
            } else {
                Result.failure(Exception("EMAIL_NOT_VERIFIED"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el nombre del usuario actual consultando su documento en Firestore.
     */
    override suspend fun getCurrentUserName(): String? {
        val uid = getCurrentUserUid() ?: return null
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.getString("nom")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene el nombre de cualquier usuario a partir de su UID (útil para comentarios/listas).
     */
    override suspend fun getUserNameById(uid: String): Result<String> {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val name = document.getString("nom") ?: "Usuario desconocido"
            Result.success(name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el email guardado en el perfil de Firestore del usuario actual.
     */
    override suspend fun getCurrentUserEmail(): String? {
        val uid = getCurrentUserUid() ?: return null
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.getString("email")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene el email directamente desde el servicio de autenticación de Firebase.
     */
    override suspend fun getCurrentUserEmailAuth(): String? {
        return auth.currentUser?.email
    }

    /**
     * Actualiza los datos de perfil (nombre y email) en la base de datos Firestore.
     */
    override suspend fun updateUserProfile(
        userId: String,
        nombre: String,
        email: String
    ): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            val updates = mapOf(
                "nom" to nombre,
                "email" to email
            )
            userRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Envía una solicitud de actualización de email a Firebase Auth.
     * Requiere que el usuario verifique el nuevo email antes de que el cambio sea efectivo.
     */
    override suspend fun updateUserEmail(newEmail: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
            user.verifyBeforeUpdateEmail(newEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza el nombre del perfil directamente en el objeto de usuario de Firebase Auth.
     */
    override suspend fun updateUserName(newName: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()
            user.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Recarga los datos del usuario actual desde el servidor para obtener cambios externos.
     */
    override suspend fun refreshUser() {
        auth.currentUser?.reload()?.await()
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    override suspend fun signOut() {
        auth.signOut()
    }

    /**
     * Comprueba si el usuario tiene su dirección de correo electrónico verificada.
     */
    override suspend fun isEmailVerified(): Boolean {
        auth.currentUser?.reload()?.await()
        return auth.currentUser?.isEmailVerified ?: false
    }

    /**
     * Actualiza la URL de la foto de perfil en el documento de Firestore del usuario.
     */
    override suspend fun updateUserProfilePicture(userId: String, imageUrl: String): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            userRef.update("profilePictureUrl", imageUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Recupera la URL de la foto de perfil guardada en Firestore para el usuario actual.
     */
    override suspend fun getCurrentUserProfilePicture(): String? {
        val uid = getCurrentUserUid() ?: return null
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.getString("profilePictureUrl")
        } catch (e: Exception) {
            null
        }
    }
}