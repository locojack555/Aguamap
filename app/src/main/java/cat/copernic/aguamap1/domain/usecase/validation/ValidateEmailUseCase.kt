package cat.copernic.aguamap1.domain.usecase.validation

import android.util.Patterns
import cat.copernic.aguamap1.R

class ValidateEmailUseCase {
    operator fun invoke(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, R.string.error_email_empty)
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ValidationResult(false, R.string.error_email_pattern)
        }
        return ValidationResult(true)
    }
}