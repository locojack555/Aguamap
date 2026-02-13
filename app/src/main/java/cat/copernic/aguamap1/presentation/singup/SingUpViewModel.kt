package cat.copernic.aguamap1.presentation.singup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.data.repository.FirebaseAuthRepository
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.usecase.validation.ValidateEmailUseCase
import cat.copernic.aguamap1.domain.usecase.validation.ValidatePasswordUseCase
import kotlinx.coroutines.launch

class SingUpViewModel(
    private val repository: AuthRepository = FirebaseAuthRepository(),
    private val validateEmail: ValidateEmailUseCase = ValidateEmailUseCase(),
    private val validatePassword: ValidatePasswordUseCase = ValidatePasswordUseCase()
) : ViewModel() {

    var email by mutableStateOf("")
    var emailError by mutableStateOf<Int?>(null)
    var password by mutableStateOf("")
    var passwordError by mutableStateOf<Int?>(null)
    var isSuccess by mutableStateOf(false)


    fun onSingUpClick() {
        val emailResult = validateEmail(email)
        val passwordResult = validatePassword(password)
        if (!emailResult.success || !passwordResult.success) {
            emailError = emailResult.errorResId
            passwordError = passwordResult.errorResId
        }
        viewModelScope.launch {
            val result = repository.signUp(email, password)
            if (result.isSuccess) {
                isSuccess = true
            } else {
                emailError =
                    if (result.exceptionOrNull()?.message == "ERROR_DUPLICATED") R.string.error_email_duplicated else R.string.error_email_generic
            }
        }
    }
}