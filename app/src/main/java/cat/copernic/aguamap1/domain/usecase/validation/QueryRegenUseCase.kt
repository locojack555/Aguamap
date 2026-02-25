package cat.copernic.aguamap1.domain.usecase.validation

fun generateSearchRegex(query: String): Regex? {
    if (query.isBlank()) return null
    return try {
        val pattern = buildString {
            append(".*") // para buscar en cualquier parte

            query.forEach { char ->
                when (char) {
                    '*' -> append(".*")     // * = cualquier cantidad de caracteres
                    '?' -> append(".")      // ? = un solo carácter
                    else -> append(Regex.escape(char.toString()))
                }
            }

            append(".*")
        }
        Regex(pattern, RegexOption.IGNORE_CASE)
    } catch (e: Exception) {
        null
    }
}
