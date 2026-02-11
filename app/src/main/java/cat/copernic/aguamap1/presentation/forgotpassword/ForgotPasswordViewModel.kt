package cat.copernic.aguamap1.presentation.forgotpassword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.data.repository.FirebaseAuthRepository
import cat.copernic.aguamap1.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val repository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {
    var email by mutableStateOf("")
        private set
    var isError by mutableStateOf(false)
        private set
    var isSent by mutableStateOf(false)
        private set

    fun onEmailChanged(newValue: String) {
        email = newValue
        isError = false
    }

    fun onResetPasswordClick() {
        if (email.isEmpty()) {
            isError = true
            return
        }
        viewModelScope.launch {
            val result = repository.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                isSent = true
            } else {
                isError = true
            }
        }
    }
}