package cat.copernic.aguamap1.aplication.singup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.repository.auth.AuthRepository
import cat.copernic.aguamap1.domain.usecase.validation.ValidateEmailUseCase
import cat.copernic.aguamap1.domain.usecase.validation.ValidatePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que gestiona la lógica de registro de usuarios.
 * Utiliza casos de uso para validar los inputs antes de realizar peticiones a Firebase.
 */
@HiltViewModel
class SingUpViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val validateEmail: ValidateEmailUseCase = ValidateEmailUseCase(),
    private val validatePassword: ValidatePasswordUseCase = ValidatePasswordUseCase()
) : ViewModel() {

    // Estados de los campos vinculados bidireccionalmente con la UI
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    // Estados de error que almacenan IDs de recursos (R.string)
    var emailError by mutableStateOf<Int?>(null)
    var passwordError by mutableStateOf<Int?>(null)

    // Estado para controlar el mensaje de "Link de verificación enviado"
    var isWaitingVerification by mutableStateOf(false)


    /**
     * Ejecuta el proceso de registro: valida campos locales y luego llama al repositorio.
     */
    fun onSingUpClick() {
        // 1. Validaciones locales de formato y seguridad
        val emailResult = validateEmail(email)
        val passwordResult = validatePassword(password)

        // Limpieza de errores previos antes de revalidar
        emailError = null
        passwordError = null

        if (!emailResult.success || !passwordResult.success) {
            emailError = emailResult.errorResId
            passwordError = passwordResult.errorResId
            return
        }

        // 2. Intento de registro en Firebase a través del repositorio
        viewModelScope.launch {
            val result = repository.signUp(email, password)
            if (result.isSuccess) {
                // Registro exitoso: Activamos el feedback visual y limpiamos campos.
                // Nota: El repositorio suele hacer signOut() tras el registro para forzar
                // la verificación de email en el siguiente login.
                isWaitingVerification = true
                email = ""
                password = ""
            } else {
                // Gestión de errores específicos del backend
                val exception = result.exceptionOrNull()
                emailError = if (exception?.message == "ERROR_DUPLICATED") {
                    R.string.error_email_duplicated
                } else {
                    R.string.error_email_generic
                }
            }
        }
    }

    /**
     * Limpia el estado del ViewModel (útil al navegar fuera de la pantalla).
     */
    fun resetState() {
        isWaitingVerification = false
        emailError = null
        passwordError = null
        email = ""
        password = ""
    }
}