package cat.copernic.aguamap1.presentation.home.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cat.copernic.aguamap1.presentation.home.map.MapScreen
import cat.copernic.aguamap1.presentation.navigation.BottomNavItem
import cat.copernic.aguamap1.presentation.profile.ProfileScreen
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun NavigationGraph(navController: NavHostController) {
    val db = Firebase.firestore
    //Contenedor de navegación
    NavHost(
        navController = navController,
        //ruta inicial
        startDestination = BottomNavItem.Map.route
    ) {
        //Dibuja la pantalla si esta en la ruta
        composable(BottomNavItem.Map.route) {
            MapScreen(isHome = true)
        }
        composable(BottomNavItem.Categories.route) {
            // Placeholder para Categorías
        }
        composable(BottomNavItem.Game.route) {
            // Placeholder para Juego
            //MapScreen(isHome = false)
        }
        composable(BottomNavItem.Ranking.route) {
            // Placeholder para Ranking
        }
        composable(BottomNavItem.Profile.route) {
            // Placeholder para Perfil
            ProfileScreen()
        }
    }
}