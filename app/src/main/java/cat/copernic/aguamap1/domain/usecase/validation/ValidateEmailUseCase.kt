package cat.copernic.aguamap1.domain.usecase.validation

import android.util.Patterns
import cat.copernic.aguamap1.R
import javax.inject.Inject

/**
 * Caso de Uso encargado de validar formalmente una dirección de correo electrónico.
 * * Verifica que el campo no esté vacío y que cumpla con el estándar internacional
 * de formato de email (RFC), proporcionando recursos de strings específicos para errores.
 */
class ValidateEmailUseCase @Inject constructor() {
    /**
     * Ejecuta la lógica de validación del email.
     * * @param email La cadena de texto introducida por el usuario en el campo de correo.
     * @return Un objeto [ValidationResult] que indica si es válido o el ID del recurso
     * de error correspondiente para mostrar en la UI.
     */
    operator fun invoke(email: String): ValidationResult {
        // 1. Verificación de campo obligatorio
        if (email.isBlank()) {
            return ValidationResult(false, R.string.error_email_empty)
        }

        // 2. Verificación de patrón sintáctico (Android Patterns)
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ValidationResult(false, R.string.error_email_pattern)
        }

        // Validación exitosa
        return ValidationResult(true)
    }
}