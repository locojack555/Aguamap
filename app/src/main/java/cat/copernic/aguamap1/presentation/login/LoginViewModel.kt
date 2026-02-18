package cat.copernic.aguamap1.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // Estados de la UI
    var email by mutableStateOf("")
        private set // Solo se puede modificar desde dentro del ViewModel
    var password by mutableStateOf("")
        private set
    var isError by mutableStateOf(false)
        private set
    var isEmailVerifiedError by mutableStateOf(false)
        private set

    private val _navigateToHome = MutableSharedFlow<Boolean>()
    val navigateToHome = _navigateToHome.asSharedFlow()

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

    fun onLoginClick() {
        if (email.isEmpty() || password.isEmpty()) {
            isError = true
            return
        }
        //view model scope para lanzar un hilo de ejecución
        viewModelScope.launch {
            val result = repository.login(email, password)
            if (result.isSuccess) {
                _navigateToHome.emit(true)
            } else {
                val exception = result.exceptionOrNull()
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
}