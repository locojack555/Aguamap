package cat.copernic.aguamap1.domain.repository

import cat.copernic.aguamap1.domain.model.UserRole

interface AuthRepository {
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserUid(): String?
    suspend fun login(email: String, password: String): Result<Boolean>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun signUp(email: String, password: String): Result<Boolean>
    suspend fun checkIfUserExists(uid: String): Boolean
    suspend fun completeRegistration(name: String): Result<Boolean>
    suspend fun getUserRole(uid: String): UserRole
    suspend fun getCurrentUserName(): String?
    suspend fun getCurrentUserEmail(): String?
    suspend fun getCurrentUserEmailAuth(): String?
    suspend fun updateUserProfile(userId: String, nombre: String, email: String): Result<Unit>
    suspend fun updateUserEmail(newEmail: String): Result<Unit>
    suspend fun updateUserName(newName: String): Result<Unit>
    suspend fun refreshUser()
    suspend fun signOut()
    suspend fun isEmailVerified(): Boolean
    suspend fun getUserNameById(uid: String): Result<String>
}