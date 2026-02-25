package cat.copernic.aguamap1.presentation.home.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cat.copernic.aguamap1.presentation.game.GameScreen
import cat.copernic.aguamap1.presentation.home.map.MapScreen
import cat.copernic.aguamap1.presentation.music.SoundManager
import cat.copernic.aguamap1.presentation.navigation.RootScreen
import cat.copernic.aguamap1.presentation.profile.ProfileScreen
import cat.copernic.aguamap1.presentation.ranking.RankingScreen
import cat.copernic.aguamap1.presentation.categories.CategoriesScreen
import cat.copernic.aguamap1.presentation.profile.EditProfileScreen
import cat.copernic.aguamap1.presentation.profile.ProfileViewModel

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
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    soundManager.stopAllSounds()
                    rootNavController.navigate(RootScreen.Login.route) {
                        popUpTo(RootScreen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                navigateToEditProfile = {
                    navController.navigate("edit_profile")
                })

        }

        composable("edit_profile") { backStackEntry ->
            // Buscamos la entrada de la pantalla de perfil
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(BottomNavItem.Profile.route)
            }

            // Recuperamos el ViewModel compartido
            val profileViewModel: ProfileViewModel = hiltViewModel(parentEntry)
            val profileState by profileViewModel.profileState.collectAsState()

            EditProfileScreen(
                initialNombre = profileState.userName,
                initialEmail = profileState.userEmail,
                viewModel = profileViewModel,
                onBack = { navController.popBackStack() },
                onSaveComplete = {
                    profileViewModel.loadUserData()
                    navController.popBackStack()
                }
            )
        }
    }
}