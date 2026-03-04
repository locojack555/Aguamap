package cat.copernic.aguamap1.data.error

import android.content.Context
import cat.copernic.aguamap1.domain.error.ErrorResourceProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación concreta de la interfaz ErrorResourceProvider.
 * Esta clase actúa como un puente entre la capa de Dominio (que no debe conocer Android)
 * y el sistema de recursos de Android, permitiendo obtener cadenas de texto (strings)
 * desde cualquier parte de la aplicación, incluidos los ViewModels o Casos de Uso.
 */
@Singleton
class ErrorResourceProviderImpl @Inject constructor(
    // Contexto de la aplicación inyectado para acceder a los recursos del sistema
    private val context: Context
) : ErrorResourceProvider {

    /**
     * Obtiene una cadena de texto simple desde el archivo strings.xml.
     * @param resId El identificador del recurso (ej. R.string.error_network).
     * @return La cadena de texto traducida según el idioma del dispositivo.
     */
    override fun getString(resId: Int): String = context.getString(resId)

    /**
     * Obtiene una cadena de texto que contiene marcadores de posición (placeholders).
     * @param resId El identificador del recurso (ej. R.string.error_with_code).
     * @param args Argumentos variables (vararg) para rellenar los marcadores (ej. %s o %d).
     * @return La cadena de texto formateada con los argumentos proporcionados.
     */
    override fun getString(resId: Int, vararg args: Any): String =
        context.getString(resId, *args)
}