package cat.copernic.aguamap1.aplication.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import cat.copernic.aguamap1.aplication.sound.SoundManagerViewModel
import cat.copernic.aguamap1.aplication.navigation.navigationApp.HomeBottomNavigation
import cat.copernic.aguamap1.aplication.navigation.navigationApp.NavigationGraph

/**
 * Pantalla principal que actúa como contenedor de la aplicación tras el Login.
 * Implementa un sistema de navegación anidada: un NavController externo para
 * flujos globales (como salir al Login) y uno interno para las secciones de la BottomBar.
 *
 * @param rootNavController Controlador de navegación principal de la aplicación.
 * @param soundManagerViewModel Gestor de efectos de sonido y música ambiental.
 */
@Composable
fun HomeScreen(
    rootNavController: NavHostController,
    soundManagerViewModel: SoundManagerViewModel = hiltViewModel()
) {
    // NavController dedicado exclusivamente a las pestañas de la Home (Mapa, Ranking, Perfil)
    val internalNavController = rememberNavController()
    val soundManager = soundManagerViewModel.soundManager


    /**
     * Componente Scaffold:
     * Proporciona la estructura visual estándar de Material Design 3.
     * Gestiona automáticamente el espacio para la barra de navegación inferior.
     */
    Scaffold(
        bottomBar = {
            // Barra de navegación inferior con acceso a las secciones principales
            HomeBottomNavigation(internalNavController)
        }
    ) { paddingValues ->
        /* * El Box consume el paddingValues proporcionado por el Scaffold.
         * Esto evita que el contenido (como el mapa) se dibuje por debajo de la BottomBar.
         */
        Box(modifier = Modifier.padding(paddingValues)) {
            // Grafo de navegación interno que renderiza las pantallas de la Home
            NavigationGraph(
                navController = internalNavController,
                rootNavController = rootNavController,
                soundManager = soundManager
            )
        }
    }
}