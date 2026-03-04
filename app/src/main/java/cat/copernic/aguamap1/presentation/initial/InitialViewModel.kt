package cat.copernic.aguamap1.presentation.initial

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
 * Determina el flujo de navegación inicial basándose en el estado de autenticación
 * y la integridad de los datos del usuario en el repositorio.
 */
@HiltViewModel
class InitialViewModel @Inject constructor(
    /**
     * Repositorio de autenticación inyectado mediante Hilt.
     * Proporciona acceso a Firebase Auth y la verificación de perfiles en la base de datos.
     */
    private val repository: AuthRepository
) : ViewModel() {

    /**
     * Flujo de estado que emite la ruta de destino una vez calculada.
     * Se inicializa como null para que la UI se mantenga en el Splash hasta tener una decisión.
     */
    private val _destination = MutableStateFlow<String?>(null)

    /**
     * Exposición pública del destino como StateFlow inmutable para ser recolectado por la View.
     */
    val destination: StateFlow<String?> = _destination.asStateFlow()

    /**
     * El bloque init dispara la comprobación de sesión inmediatamente al instanciar el ViewModel.
     */
    init {
        checkSession()
    }

    /**
     * Lógica de decisión de ruta:
     * 1. ¿Está el usuario logueado en Firebase Auth?
     * 2. Si lo está, ¿existe su perfil correspondiente en la base de datos (Firestore/Realtime)?
     * 3. Si ambas son ciertas, navega a Home. En cualquier otro caso, redirige a Login.
     */
    private fun checkSession() {
        viewModelScope.launch {
            val route = if (repository.isUserLoggedIn()) {
                val uid = repository.getCurrentUserUid()
                // Verificación doble: Estar logueado != tener perfil de usuario creado
                val exists = if (uid != null) repository.checkIfUserExists(uid) else false

                if (exists) {
                    RootScreen.Home.route
                } else {
                    RootScreen.Login.route
                }
            } else {
                RootScreen.Login.route
            }

            // Emitimos el resultado para que la InitialScreen ejecute la navegación
            _destination.value = route
        }
    }
}