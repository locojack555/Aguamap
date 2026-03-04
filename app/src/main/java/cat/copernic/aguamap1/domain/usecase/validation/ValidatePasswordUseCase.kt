package cat.copernic.aguamap1.domain.usecase.validation

import cat.copernic.aguamap1.R
import javax.inject.Inject

/**
 * Representa el resultado de una operación de validación de entrada de datos.
 * * @property success Indica si los datos cumplen con las reglas de negocio.
 * @property errorResId El ID del recurso de cadena (string) que contiene el mensaje de error
 * amigable para el usuario, en caso de que [success] sea false.
 */
data class ValidationResult(val success: Boolean, val errorResId: Int? = null)

/**
 * Caso de Uso encargado de aplicar las políticas de seguridad sobre las contraseñas.
 * * Una contraseña válida en AguaMap debe cumplir los siguientes requisitos:
 * 1. Mínimo 8 caracteres.
 * 2. Al menos una letra mayúscula.
 * 3. Al menos un dígito numérico.
 * 4. Al menos un carácter especial (símbolo).
 */
class ValidatePasswordUseCase @Inject constructor() {
    /**
     * Evalúa la complejidad de una contraseña proporcionada.
     * * @param password La cadena de texto a validar.
     * @return [ValidationResult] con el estado de éxito o el error específico detectado.
     */
    operator fun invoke(password: String): ValidationResult {
        return when {
            // 1. Validación de longitud mínima
            password.length < 8 ->
                ValidationResult(false, R.string.error_password_len)

            // 2. Requisito de mayúsculas
            !password.any { it.isUpperCase() } ->
                ValidationResult(false, R.string.error_password_uppercase)

            // 3. Requisito de números
            !password.any { it.isDigit() } ->
                ValidationResult(false, R.string.error_password_number)

            // 4. Requisito de caracteres especiales (no letra ni dígito)
            !password.any { !it.isLetterOrDigit() } ->
                ValidationResult(false, R.string.error_password_special)

            // Si cumple todas las condiciones
            else ->
                ValidationResult(true)
        }
    }
}