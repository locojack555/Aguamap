package cat.copernic.aguamap1.aplication.initial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cat.copernic.aguamap1.aplication.commons.AguaMapHeader
import cat.copernic.aguamap1.ui.theme.AguaMapGradient

/**
 * Pantalla de inicio (Splash Screen) y punto de entrada de la aplicación.
 * Esta pantalla se encarga de gestionar el redireccionamiento inicial del usuario:
 * - Si el usuario ya está autenticado, navega al Mapa/Home.
 * - Si el usuario no está autenticado, navega a la pantalla de Login.
 *
 * @param navController Controlador de navegación para redirigir al usuario.
 * @param viewModel ViewModel que gestiona la lógica de autenticación y destino.
 */
@Composable
fun InitialScreen(
    navController: NavController,
    viewModel: InitialViewModel = hiltViewModel()
) {
    /**
     * Observamos el estado del destino definido por el ViewModel.
     */
    val destination by viewModel.destination.collectAsState()

    /**
     * LaunchedEffect para ejecutar la navegación de forma segura una vez que
     * el destino se ha calculado (basado en el estado de Firebase Auth).
     */
    LaunchedEffect(destination) {
        destination?.let { route ->
            // Eliminamos la pantalla inicial del backstack para que el usuario
            // no pueda volver al splash pulsando 'atrás'.
            navController.popBackStack()
            navController.navigate(route)
        }
    }

    /**
     * Mientras se decide el destino, mostramos la UI visual del Splash.
     */
    Splash()
}

/**
 * Componente visual de la Splash Screen.
 * Presenta el logotipo de la aplicación centrado con el gradiente corporativo.
 */
@Composable
fun Splash() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AguaMapGradient),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Espaciado dinámico para centrar visualmente el logotipo
        Spacer(modifier = Modifier.weight(0.8f))

        /**
         * Cabecera reutilizable con el branding de AguaMap.
         */
        AguaMapHeader(
            logoSize = 220.dp,
            innerSpacing = 40.dp,
            isSplash = true // Indicamos que es modo splash para posibles ajustes internos
        )

        Spacer(modifier = Modifier.weight(1.2f))
    }
}