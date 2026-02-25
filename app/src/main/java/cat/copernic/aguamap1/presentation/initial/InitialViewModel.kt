package cat.copernic.aguamap1.presentation.initial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.presentation.navigationInitial.RootScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InitialViewModel @Inject constructor(
    //Inyectamos el repositorio de autenticación con sus métodos
    private val repository: AuthRepository
) : ViewModel() {

    //MutableSharedFlow para emitir la ruta
    private val _destination = MutableStateFlow<String?>(null)

    //asSharedFlow para que solo se pueda leer desde el ViewModel
    val destination = _destination.asStateFlow()

    //Inicializamos la sesión y verificamos si hay una sesión activa
    init {
        checkSession()
    }

    //Función para verificar la sesión y redirige a la pantalla correspondiente
    private fun checkSession() {
        viewModelScope.launch {
            val route = if (repository.isUserLoggedIn()) {
                val uid = repository.getCurrentUserUid()
                val exists = if (uid != null) repository.checkIfUserExists(uid) else false
                if (exists) RootScreen.Home.route else RootScreen.Login.route
            } else {
                RootScreen.Login.route
            }
            _destination.value = route
        }
    }
}