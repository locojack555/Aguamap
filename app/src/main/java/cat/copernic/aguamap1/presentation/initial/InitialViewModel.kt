package cat.copernic.aguamap1.presentation.initial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.aguamap1.data.repository.FirebaseAuthRepository
import cat.copernic.aguamap1.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class InitialViewModel(
    //Inyectamos el repositorio de autenticación con sus métodos
    private val repository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    //MutableSharedFlow para emitir la ruta
    private val _destination = MutableSharedFlow<String>()

    //asSharedFlow para que solo se pueda leer desde el ViewModel
    val destination = _destination.asSharedFlow()

    //Inicializamos la sesión y verificamos si hay una sesión activa
    init {
        //FirebaseAuth.getInstance().signOut()
        checkSession()
    }

    //Función para verificar la sesión y redirige a la pantalla correspondiente
    private fun checkSession() {
        viewModelScope.launch {
            delay(1000)
            val route = if (repository.isUserLoggedIn()) "home" else "logIn"
            _destination.emit(route)
        }
    }
}