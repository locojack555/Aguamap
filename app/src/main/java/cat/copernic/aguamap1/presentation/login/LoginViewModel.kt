package cat.copernic.aguamap1.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.error.ErrorResourceProvider
import cat.copernic.aguamap1.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val errorResourceProvider: ErrorResourceProvider // Añadido para consistencia
) : ViewModel() {

    // Estados de la UI
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isError by mutableStateOf(false)
        private set
    var isEmailVerifiedError by mutableStateOf(false)
        private set
    var name by mutableStateOf("")
        private set // Cambiado a private set para control total
    var needsName by mutableStateOf(false)
        private set

    private val _navigateToHome = MutableSharedFlow<Boolean>()
    val navigateToHome = _navigateToHome.asSharedFlow()

    fun resetState() {
        needsName = false
        name = ""
        isError = false
        isEmailVerifiedError = false
        email = ""
        password = ""
    }

    fun onEmailChanged(newValue: String) {
        email = newValue
        isError = false
        isEmailVerifiedError = false
    }

    fun onNameChanged(newValue: String) {
        // Validación multiidioma: permite letras de cualquier alfabeto y espacios
        if (newValue.all { it.isLetter() || it.isWhitespace() }) {
            name = newValue
        }
    }

    fun onPasswordChanged(newValue: String) {
        password = newValue
        isError = false
        isEmailVerifiedError = false
    }

    fun onLoginClick() {
        if (email.isEmpty() || password.isEmpty()) {
            isError = true
            return
        }
        viewModelScope.launch {
            val result = repository.login(email, password)
            if (result.isSuccess) {
                // Comprobamos si el perfil de usuario (Firestore) existe
                checkUserAndNavigate()
            } else {
                val exception = result.exceptionOrNull()
                // La clave "EMAIL_NOT_VERIFIED" debe coincidir con la lógica de tu AuthRepository
                if (exception?.message == "EMAIL_NOT_VERIFIED") {
                    isEmailVerifiedError = true
                    isError = false
                } else {
                    isError = true
                    isEmailVerifiedError = false
                }
            }
        }
    }

    private suspend fun checkUserAndNavigate() {
        val uid = repository.getCurrentUserUid()
        if (uid != null) {
            val exists = repository.checkIfUserExists(uid)
            if (exists) {
                _navigateToHome.emit(true)
            } else {
                // Si no existe en Firestore (ej: login con Google por primera vez), pedimos nombre
                needsName = true
            }
        }
    }

    fun onCompleteRegistration() {
        // Validación: Nombre y al menos un apellido (2 palabras mínimo)
        val nameParts = name.trim().split(" ").filter { it.isNotEmpty() }
        if (nameParts.size < 2) return

        viewModelScope.launch {
            val result = repository.completeRegistration(name.trim())
            if (result.isSuccess) {
                needsName = false
                _navigateToHome.emit(true)
            } else {
                // Si falla el guardado en Firestore
                isError = true
            }
        }
    }
}