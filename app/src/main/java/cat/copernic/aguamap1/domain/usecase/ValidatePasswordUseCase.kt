package cat.copernic.aguamap1.domain.usecase

class ValidatePasswordUseCase {
    operator fun invoke(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult(false, "Mínimo 8 caracteres")
        }
        val hasNumber = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        if (!hasNumber) {
            return ValidationResult(false, "Falta al menos un número")
        }
        if (!hasSpecial) {
            return ValidationResult(false, "Falta un carácter especial (@, #, etc.)")
        }
        return ValidationResult(true)
    }
}

data class ValidationResult(val success: Boolean, val errorMessage: String? = null)