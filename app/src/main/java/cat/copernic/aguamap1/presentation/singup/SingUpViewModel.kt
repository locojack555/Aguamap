package cat.copernic.aguamap1.presentation.singup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.usecase.validation.ValidateEmailUseCase
import cat.copernic.aguamap1.domain.usecase.validation.ValidatePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SingUpViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val validateEmail: ValidateEmailUseCase = ValidateEmailUseCase(),
    private val validatePassword: ValidatePasswordUseCase = ValidatePasswordUseCase()
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var emailError by mutableStateOf<Int?>(null)
    var passwordError by mutableStateOf<Int?>(null)
    var isWaitingVerification by mutableStateOf(false)

    fun onSingUpClick() {
        // 1. Validaciones locales
        val emailResult = validateEmail(email)
        val passwordResult = validatePassword(password)

        emailError = null
        passwordError = null

        if (!emailResult.success || !passwordResult.success) {
            emailError = emailResult.errorResId
            passwordError = passwordResult.errorResId
            return
        }

        // 2. Registro en Firebase
        viewModelScope.launch {
            val result = repository.signUp(email, password)
            if (result.isSuccess) {
                // Al tener el signOut() en el Repositorio, el usuario
                // queda bloqueado hasta que vuelva a loguearse tras verificar.
                isWaitingVerification = true
                email = ""
                password = ""
            } else {
                val exception = result.exceptionOrNull()
                emailError = if (exception?.message == "ERROR_DUPLICATED") {
                    R.string.error_email_duplicated
                } else {
                    R.string.error_email_generic
                }
            }
        }
    }

    fun resetState() {
        isWaitingVerification = false
        emailError = null
        passwordError = null
    }
}