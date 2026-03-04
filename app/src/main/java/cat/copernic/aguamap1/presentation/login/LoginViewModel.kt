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

/**
 * ViewModel que gestiona la lógica de autenticación y validación del Login.
 * Implementa un sistema de estados reactivos para la UI y maneja el flujo de
 * verificación de perfil tras un inicio de sesión exitoso en Firebase Auth.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val errorResourceProvider: ErrorResourceProvider
) : ViewModel() {

    // --- ESTADOS DE LA UI (PROTEGIDOS) ---
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isError by mutableStateOf(false)
        private set
    var isEmailVerifiedError by mutableStateOf(false)
        private set
    var name by mutableStateOf("")
        private set
    var needsName by mutableStateOf(false)
        private set

    /**
     * SharedFlow para eventos de navegación única.
     * Evita que al rotar la pantalla se vuelva a disparar la navegación si el estado persiste.
     */
    private val _navigateToHome = MutableSharedFlow<Boolean>()
    val navigateToHome = _navigateToHome.asSharedFlow()

    /**
     * Limpia todos los estados del formulario al entrar o salir de la pantalla.
     */
    fun resetState() {
        needsName = false
        name = ""
        isError = false
        isEmailVerifiedError = false
        email = ""
        password = ""
    }

    // --- MANEJADORES DE ENTRADA ---

    fun onEmailChanged(newValue: String) {
        email = newValue
        isError = false
        isEmailVerifiedError = false
    }

    fun onPasswordChanged(newValue: String) {
        password = newValue
        isError = false
        isEmailVerifiedError = false
    }

    /**
     * Valida que el nombre solo contenga caracteres alfabéticos o espacios.
     */
    fun onNameChanged(newValue: String) {
        if (newValue.all { it.isLetter() || it.isWhitespace() }) {
            name = newValue
        }
    }

    // --- LÓGICA DE NEGOCIO ---

    /**
     * Proceso de Login:
     * 1. Valida campos vacíos.
     * 2. Llama al repositorio para autenticar con Firebase.
     * 3. Maneja errores específicos como la falta de verificación de email.
     */
    fun onLoginClick() {
        if (email.isEmpty() || password.isEmpty()) {
            isError = true
            return
        }
        viewModelScope.launch {
            val result = repository.login(email, password)
            if (result.isSuccess) {
                // Si el login es correcto, comprobamos la integridad del perfil en Firestore
                checkUserAndNavigate()
            } else {
                val exception = result.exceptionOrNull()
                // Verificamos si el error es por falta de activación de cuenta
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

    /**
     * Verifica si el usuario autenticado tiene un documento de perfil creado.
     * Si no existe (caso típico de primer login social), activa el estado 'needsName'.
     */
    private suspend fun checkUserAndNavigate() {
        val uid = repository.getCurrentUserUid()
        if (uid != null) {
            val exists = repository.checkIfUserExists(uid)
            if (exists) {
                _navigateToHome.emit(true)
            } else {
                needsName = true
            }
        }
    }

    /**
     * Finaliza el registro guardando el nombre y apellidos en la base de datos.
     * Requiere un mínimo de dos palabras para asegurar Nombre + Apellido.
     */
    fun onCompleteRegistration() {
        val nameParts = name.trim().split(" ").filter { it.isNotEmpty() }
        if (nameParts.size < 2) return

        viewModelScope.launch {
            val result = repository.completeRegistration(name.trim())
            if (result.isSuccess) {
                needsName = false
                _navigateToHome.emit(true)
            } else {
                isError = true
            }
        }
    }
}