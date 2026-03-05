package cat.copernic.aguamap1.presentation.initial

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.presentation.navigationInitial.RootScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsable de la lógica de arranque de la aplicación.
 * Determina el flujo de navegación inicial y restaura las preferencias del usuario (Idioma).
 */
@HiltViewModel
class InitialViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<String?>(null)
    val destination: StateFlow<String?> = _destination.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            try {
                if (repository.isUserLoggedIn()) {
                    val uid = repository.getCurrentUserUid()
                    val userResult = if (uid != null) repository.getUserData(uid) else null

                    if (userResult != null && userResult.isSuccess) {
                        val user = userResult.getOrNull()

                        // Comprobar idioma antes de aplicar para evitar recreaciones infinitas
                        user?.language?.let { savedLanguage ->
                            val currentAppLocale =
                                AppCompatDelegate.getApplicationLocales().toLanguageTags()

                            // Si el idioma guardado en Firebase es distinto al actual de la App,
                            // forzamos el cambio para que prevalezca el del perfil de usuario.
                            if (currentAppLocale != savedLanguage && savedLanguage.isNotEmpty()) {
                                Log.d(
                                    "InitialViewModel",
                                    "Sincronizando idioma de usuario: $savedLanguage"
                                )
                                applyLanguagePreference(savedLanguage)
                                // IMPORTANTE: Al cambiar el idioma, la Activity se destruye.
                                // No establecemos _destination.value aquí porque la nueva instancia lo hará.
                                return@launch
                            }
                        }
                        _destination.value = RootScreen.Home.route
                    } else {
                        // Si el usuario está logueado pero no hay datos en Firestore, vamos a login
                        _destination.value = RootScreen.Login.route
                    }
                } else {
                    _destination.value = RootScreen.Login.route
                }
            } catch (e: Exception) {
                Log.e("InitialViewModel", "Error en checkSession: ${e.message}")
                // En caso de error crítico (ej. Firebase caído), enviamos a Login
                _destination.value = RootScreen.Login.route
            }
        }
    }

    private fun applyLanguagePreference(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}