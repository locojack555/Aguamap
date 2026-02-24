package cat.copernic.aguamap1.presentation.navigationApp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cat.copernic.aguamap1.presentation.categories.CategoriesScreen
import cat.copernic.aguamap1.presentation.game.GameScreen
import cat.copernic.aguamap1.presentation.maps.mapView.MapScreen
import cat.copernic.aguamap1.presentation.music.SoundManager
import cat.copernic.aguamap1.presentation.navigationInitial.RootScreen
import cat.copernic.aguamap1.presentation.profile.ProfileScreen
import cat.copernic.aguamap1.presentation.ranking.RankingScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    rootNavController: NavHostController,
    soundManager: SoundManager
) {
    val currentRoute by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
        initialValue = navController.currentBackStackEntry?.destination?.route
    )

    // DETENER TODOS LOS SONIDOS cuando NO estamos en la pantalla de juego
    LaunchedEffect(currentRoute) {
        if (currentRoute != BottomNavItem.Game.route) {
            soundManager.stopAllSounds()
        }
    }

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
            // ¡Aquí llamamos a tu pantalla!
            CategoriesScreen()
        }
        composable(BottomNavItem.Game.route) {
            GameScreen(
                onBackToHome = {
                    soundManager.stopAllSounds() // Detener TODO al volver a home
                    navController.navigate(BottomNavItem.Map.route) {
                        popUpTo(BottomNavItem.Map.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(BottomNavItem.Ranking.route) {
            RankingScreen()
        }
        composable(BottomNavItem.Profile.route) {
            // Placeholder para Perfil
            ProfileScreen(
                navigateToLogin = {
                    soundManager.stopAllSounds()
                    rootNavController.navigate(RootScreen.Login.route) {
                        popUpTo(RootScreen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                })
        }
    }
}