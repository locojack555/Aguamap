package cat.copernic.aguamap1.presentation.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel encargado de la gestión de la internacionalización (i18n) de la aplicación.
 * Utiliza la API moderna de Android (AppCompatDelegate) para permitir al usuario
 * cambiar el idioma de forma dinámica sin necesidad de reiniciar manualmente la actividad
 * o gestionar la persistencia en SharedPreferences de forma rudimentaria.
 */
@HiltViewModel
class LanguageViewModel @Inject constructor() : ViewModel() {

    /**
     * Cambia el idioma de la aplicación a nivel global.
     * Android 13+ y las librerías de compatibilidad gestionan automáticamente
     * el guardado de esta preferencia y la recreación de las vistas necesarias.
     * * @param languageCode Código ISO del lenguaje (ej: "es", "ca", "en").
     */
    fun onChangeLanguage(languageCode: String) {
        // Creamos una lista de locales compatible con la API de Android
        val appLocales = LocaleListCompat.forLanguageTags(languageCode)

        // Aplicamos el cambio de idioma globalmente en la aplicación
        AppCompatDelegate.setApplicationLocales(appLocales)
    }
}