package cat.copernic.aguamap1.domain.repository

interface AuthRepository {
    fun isUserLoggedIn(): Boolean
    suspend fun login(email: String, password: String): Result<Boolean>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun signUp(email: String, password: String): Result<Boolean>
}