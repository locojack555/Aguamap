package cat.copernic.aguamap1.domain.repository

interface AuthRepository {
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserUid(): String?
    suspend fun login(email: String, password: String): Result<Boolean>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun signUp(email: String, password: String): Result<Boolean>
    suspend fun checkIfUserExists(uid: String): Boolean
    suspend fun completeRegistration(name: String): Result<Boolean>
}