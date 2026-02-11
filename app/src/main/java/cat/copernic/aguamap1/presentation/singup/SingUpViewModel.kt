package cat.copernic.aguamap1.presentation.singup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.data.repository.FirebaseAuthRepository
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.usecase.ValidatePasswordUseCase
import kotlinx.coroutines.launch

class SingUpViewModel(
    private val repository: AuthRepository = FirebaseAuthRepository(),
    private val validatePassword: ValidatePasswordUseCase = ValidatePasswordUseCase()
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordError by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)

    fun onSingUpClick() {
        val validation = validatePassword(password)
        if (!validation.success) {
            passwordError = validation.errorMessage
            return
        }
        viewModelScope.launch {
            val result = repository.signUp(email, password)
            if (result.isSuccess) {
                isSuccess = true
            } else {
                passwordError = "Error en el registro: ${result.exceptionOrNull()?.message}"
            }
        }
    }
}