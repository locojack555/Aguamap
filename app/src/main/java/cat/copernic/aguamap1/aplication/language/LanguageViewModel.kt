package cat.copernic.aguamap1.aplication.language

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.repository.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
     * Cambia el idioma manualmente (Menú desplegable) y lo guarda en Firestore.
     */
    fun onChangeLanguage(languageCode: String) {
        applyLanguage(languageCode)

        viewModelScope.launch {
            authRepository.getCurrentUserUid()?.let { uid ->
                authRepository.updateLanguagePreference(uid, languageCode)
            }
        }
    }

    /**
     * SINCRONIZACIÓN FORZADA: Recupera el idioma del perfil y lo aplica.
     * Esto es lo que hará que el idioma de Firestore mande sobre el del Login.
     */
    fun syncUserLanguage() {
        val uid = authRepository.getCurrentUserUid() ?: return

        viewModelScope.launch {
            authRepository.getUserData(uid).onSuccess { user ->
                val userLanguage = user.language
                val currentAppLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()

                if (userLanguage.isNotEmpty() && userLanguage != currentAppLocale) {
                    Log.d("LanguageVM", "Forzando idioma de Firestore: $userLanguage")
                    applyLanguage(userLanguage)
                }
            }.onFailure {
                Log.e("LanguageVM", "Error al sincronizar: ${it.message}")
            }
        }
    }

    private fun applyLanguage(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}