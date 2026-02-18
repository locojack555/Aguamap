package cat.copernic.aguamap1.domain.usecase.validation

import cat.copernic.aguamap1.R
import javax.inject.Inject

data class ValidationResult(val success: Boolean, val errorResId: Int? = null)

class ValidatePasswordUseCase @Inject constructor() {
    operator fun invoke(password: String): ValidationResult {
        return when {
            password.length < 8 ->
                ValidationResult(false, R.string.error_password_len)

            !password.any { it.isUpperCase() } ->
                ValidationResult(false, R.string.error_password_uppercase)

            !password.any { it.isDigit() } ->
                ValidationResult(false, R.string.error_password_number)

            !password.any { !it.isLetterOrDigit() } ->
                ValidationResult(false, R.string.error_password_special)

            else ->
                ValidationResult(true)
        }
    }
}