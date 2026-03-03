package cat.copernic.aguamap1.presentation.forgotpassword

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: AuthRepository,
    application: Application // Inyectamos Application para acceder a recursos
) : AndroidViewModel(application) {

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

    // Helper para obtener traducciones
    private fun getString(resId: Int): String = getApplication<Application>().getString(resId)

    fun onEmailChanged(newValue: String) {
        email = newValue
        isError = false
        errorMessage = null
        isSent = false
    }

    fun onResetPasswordClick() {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isError = true
            errorMessage = getString(R.string.incorrect_email)
            return
        }

        viewModelScope.launch {
            isLoading = true
            isError = false
            errorMessage = null

            val result = repository.sendPasswordResetEmail(email)

            if (result.isSuccess) {
                isSent = true
                isError = false
            } else {
                isError = true
                isSent = false
                // Aquí capturamos el error real de Firebase y lo traducimos
                errorMessage = when (result.exceptionOrNull()?.message) {
                    "com.google.firebase.auth.FirebaseAuthInvalidUserException" ->
                        getString(R.string.error_user_not_found) // Asegúrate de tener esta clave
                    else -> getString(R.string.error_email_generic)
                }
            }
            isLoading = false
        }
    }
}