package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.UserRole
import cat.copernic.aguamap1.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {


    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
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

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String): Result<Boolean> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.sendEmailVerification()?.await()
            auth.signOut()
            Result.success(true)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("ERROR_DUPLICATED"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkIfUserExists(uid: String): Boolean {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun getUserRole(uid: String): UserRole {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val roleString = document.getString("role") ?: "USER"
            UserRole.valueOf(roleString)
        } catch (e: Exception) {
            UserRole.USER
        }
    }

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

    override suspend fun getCurrentUserName(): String? {
        // Reutilizamos tu función existente para obtener el UID
        val uid = getCurrentUserUid() ?: return null

        return try {
            val document = firestore.collection("users").document(uid).get().await()
            // Extraemos el campo "nom" que guardaste en completeRegistration
            document.getString("nom")
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUserNameById(uid: String): Result<String> {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val name = document.getString("nom") ?: "Usuario desconocido"
            Result.success(name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserEmail(): String? {
        // Reutilizamos tu función existente para obtener el UID
        val uid = getCurrentUserUid() ?: return null

        return try {
            val document = firestore.collection("users").document(uid).get().await()
            // Extraemos el campo "nom" que guardaste en completeRegistration
            document.getString("email")
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCurrentUserEmailAuth(): String? {
        return auth.currentUser?.email
    }

    override suspend fun updateUserProfile(userId: String, nombre: String, email: String): Result<Unit> {
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

    override suspend fun updateUserEmail(newEmail: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
            user.verifyBeforeUpdateEmail(newEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    override suspend fun refreshUser() {
        auth.currentUser?.reload()?.await()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun isEmailVerified(): Boolean {
        auth.currentUser?.reload()?.await()
        return auth.currentUser?.isEmailVerified ?: false
    }

    override suspend fun updateUserProfilePicture(userId: String, imageUrl: String): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            userRef.update("profilePictureUrl", imageUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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