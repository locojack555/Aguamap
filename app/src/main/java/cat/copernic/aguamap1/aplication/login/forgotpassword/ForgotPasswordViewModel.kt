package cat.copernic.aguamap1.aplication.login.forgotpassword

import android.app.Application
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.repository.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Gestiona el estado y la lógica de negocio para la recuperación de contraseñas.
 * * Hereda de [AndroidViewModel] para permitir la traducción de mensajes de error
 * directamente desde el ViewModel utilizando el contexto de la aplicación.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: AuthRepository,
    application: Application
) : AndroidViewModel(application) {

    // --- ESTADO DE LA INTERFAZ (UI STATE) ---
    var email by mutableStateOf("")
        private set

    var isError by mutableStateOf(false)
        private set

    var isSent by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    /**
     * Helper privado para acceder de forma sencilla a los recursos de strings localizados.
     */
    private fun getString(resId: Int): String = getApplication<Application>().getString(resId)

    /**
     * Actualiza el valor del correo y reinicia los estados de error y éxito para
     * permitir una nueva validación limpia.
     */
    fun onEmailChanged(newValue: String) {
        email = newValue
        isError = false
        errorMessage = null
        isSent = false
    }

    /**
     * Procesa la solicitud de restablecimiento de contraseña.
     * * Realiza una validación previa del formato mediante [android.util.Patterns].
     * * Gestiona la llamada al repositorio dentro de una corrutina.
     * * Mapea las excepciones de Firebase (como usuario no encontrado) a mensajes amigables.
     */
    fun onResetPasswordClick() {
        // Validación de formato antes de tocar la red
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isError = true
            errorMessage = getString(R.string.incorrect_email)
            return
        }

        viewModelScope.launch {
            isLoading = true
            isError = false
            errorMessage = null

            // Petición al repositorio de autenticación
            val result = repository.sendPasswordResetEmail(email)

            if (result.isSuccess) {
                isSent = true
                isError = false
            } else {
                isError = true
                isSent = false

                // Lógica de traducción de errores técnicos de Firebase
                errorMessage = when (result.exceptionOrNull()?.message) {
                    "com.google.firebase.auth.FirebaseAuthInvalidUserException" ->
                        getString(R.string.error_user_not_found)
                    else -> getString(R.string.error_email_generic)
                }
            }
            isLoading = false
        }
    }
}