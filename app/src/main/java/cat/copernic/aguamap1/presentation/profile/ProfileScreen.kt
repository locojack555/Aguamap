package cat.copernic.aguamap1.presentation.profile

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.core.os.LocaleListCompat

@Composable
fun ProfileScreen() {
    Button(
        onClick = {
            // Obtenemos el idioma actual
            val currentLanguage = AppCompatDelegate.getApplicationLocales()[0]?.language

            // Alternamos entre español (es) e inglés (en)
            val newLanguage = if (currentLanguage == "en") "es" else "en"

            // Aplicamos el cambio
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(newLanguage)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    ) {
        Text("Cambiar Idioma")
    }
}