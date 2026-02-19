package cat.copernic.aguamap1.presentation.initial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.presentation.navigation.RootScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InitialViewModel @Inject constructor(
    //Inyectamos el repositorio de autenticación con sus métodos
    private val repository: AuthRepository
) : ViewModel() {

    //MutableSharedFlow para emitir la ruta
    private val _destination = MutableSharedFlow<String>()

    //asSharedFlow para que solo se pueda leer desde el ViewModel
    val destination = _destination.asSharedFlow()

    //Inicializamos la sesión y verificamos si hay una sesión activa
    init {
        checkSession()
    }

    //Función para verificar la sesión y redirige a la pantalla correspondiente
    private fun checkSession() {
        viewModelScope.launch {
            delay(1000)
            if (repository.isUserLoggedIn()) {
                val uid = repository.getCurrentUserUid()
                val exists = if (uid != null) repository.checkIfUserExists(uid) else false
                if (exists) {
                    _destination.emit(RootScreen.Home.route)
                } else {
                    _destination.emit(RootScreen.Login.route)
                }
            } else {
                _destination.emit(RootScreen.Login.route)
            }
        }
    }
}