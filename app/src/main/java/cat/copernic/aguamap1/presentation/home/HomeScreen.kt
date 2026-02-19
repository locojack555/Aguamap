package cat.copernic.aguamap1.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import cat.copernic.aguamap1.presentation.home.navigation.HomeBottomNavigation
import cat.copernic.aguamap1.presentation.home.navigation.NavigationGraph
import cat.copernic.aguamap1.presentation.music.SoundManager
import cat.copernic.aguamap1.presentation.music.SoundManagerViewModel


@Composable
fun HomeScreen(rootNavController: NavHostController,  soundManagerViewModel: SoundManagerViewModel = hiltViewModel()) {
    val internalNavController = rememberNavController()
    val soundManager = soundManagerViewModel.soundManager
    //Componente que ya tiene huecos listos para usar topbar, bottombar
    Scaffold(
        bottomBar = { HomeBottomNavigation(internalNavController) }
    ) { /*paddingValues calcula el espacio que ocupan los elementos dentro
    del Scaffold y asigna elresto de espacio al contenido*/
            paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavigationGraph(
                navController = internalNavController,
                rootNavController = rootNavController,
                soundManager = soundManager
            )
        }
    }
}