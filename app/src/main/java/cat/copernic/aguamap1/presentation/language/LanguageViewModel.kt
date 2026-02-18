package cat.copernic.aguamap1.presentation.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class LanguageViewModel @Inject constructor() : ViewModel() {
    fun onChangeLanguage(languageCode: String) {
        val appLocales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }
}