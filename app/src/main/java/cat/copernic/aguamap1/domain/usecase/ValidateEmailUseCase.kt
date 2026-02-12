package cat.copernic.aguamap1.domain.usecase

import android.util.Patterns

class ValidateEmailUseCase {
    operator fun invoke(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, "El correo no puede estar vacío")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ValidationResult(false, "El formato del correo no es válido")
        }
        return ValidationResult(true)
    }
}