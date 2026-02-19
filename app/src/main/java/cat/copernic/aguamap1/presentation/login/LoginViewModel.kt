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
    var name by mutableStateOf("")
    var needsName by mutableStateOf(false)
    private val _navigateToHome = MutableSharedFlow<Boolean>()
    val navigateToHome = _navigateToHome.asSharedFlow()

    fun resetState() {
        needsName = false
        name = ""
        isError = false
        isEmailVerifiedError = false
    }

    fun onEmailChanged(newValue: String) {
        email = newValue
        isError = false
        isEmailVerifiedError = false
    }

    fun onNameChanged(newValue: String) {
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
                checkUserAndNavigate()
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

    fun onCompleteRegistration() {
        if (name.isBlank()) return
        viewModelScope.launch {
            val result = repository.completeRegistration(name)
            if (result.isSuccess) {
                needsName = false
                _navigateToHome.emit(true)
            } else {
                isError = true
            }
        }
    }

    fun checkPendingRegistration() {
        val uid = repository.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch {
                val exists = repository.checkIfUserExists(uid)
                if (!exists) {
                    needsName = true
                } else {
                    _navigateToHome.emit(true)
                }
            }
        }
    }
}