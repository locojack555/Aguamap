package cat.copernic.aguamap1.domain.usecase.validation

/**
 * Genera una expresión regular optimizada para búsquedas flexibles a partir de un texto.
 * * Esta función permite al usuario utilizar comodines tradicionales:
 * - El carácter '*' se traduce a '.*' (cualquier número de caracteres).
 * - El carácter '?' se traduce a '.' (exactamente un carácter).
 * - El resto de caracteres especiales se escapan para evitar errores de sintaxis en el motor de Regex.
 *
 * @param query La cadena de texto introducida por el usuario.
 * @return Un objeto [Regex] listo para filtrar listas, o null si la consulta está vacía o es inválida.
 */
fun generateSearchRegex(query: String): Regex? {
    if (query.isBlank()) return null

    return try {
        val pattern = buildString {
            // Permitimos que la coincidencia ocurra en cualquier parte del texto inicial
            append(".*")

            query.forEach { char ->
                when (char) {
                    '*' -> append(".*")     // Comodín de secuencia
                    '?' -> append(".")      // Comodín de carácter único
                    // Escapamos caracteres reservados de Regex (como +, [, {, etc.)
                    // para que se traten como texto literal.
                    else -> append(Regex.escape(char.toString()))
                }
            }

            // Permitimos que la coincidencia continúe hasta el final del texto
            append(".*")
        }

        // La búsqueda no distingue entre mayúsculas y minúsculas para facilitar el uso móvil
        Regex(pattern, RegexOption.IGNORE_CASE)
    } catch (e: Exception) {
        // En caso de un patrón mal formado que el motor de Regex no pueda procesar
        null
    }
}