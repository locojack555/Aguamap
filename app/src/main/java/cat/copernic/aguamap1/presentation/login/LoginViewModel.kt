package cat.copernic.aguamap1.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.data.repository.FirebaseAuthRepository
import cat.copernic.aguamap1.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = FirebaseAuthRepository() // Se inyecta el repositorio
) : ViewModel() {

    // Estados de la UI
    var email by mutableStateOf("")
        private set // Solo se puede modificar desde dentro del ViewModel
    var password by mutableStateOf("")
        private set
    var isError by mutableStateOf(false)
        private set

    private val _navigateToHome = MutableSharedFlow<Boolean>()
    val navigateToHome = _navigateToHome.asSharedFlow()

    fun onEmailChanged(newValue: String) {
        email = newValue
        isError = false
    }

    fun onPasswordChanged(newValue: String) {
        password = newValue
        isError = false
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
                isError = true
            }
        }
    }
}