package cat.copernic.aguamap1.presentation.navigationApp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cat.copernic.aguamap1.presentation.categories.CategoriesScreen
import cat.copernic.aguamap1.presentation.fountain.addFountain.detailFountain.DetailFountainViewModel
import cat.copernic.aguamap1.presentation.fountain.comments.FountainCommentsViewModel
import cat.copernic.aguamap1.presentation.game.GameScreen
import cat.copernic.aguamap1.presentation.game.GameViewModel
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

    LaunchedEffect(currentRoute) {
        if (currentRoute != BottomNavItem.Game.route) {
            soundManager.stopAllSounds()
        }
    }

    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Map.route
    ) {
        composable(BottomNavItem.Map.route) {
            MapScreen(isHome = true)
        }
        composable(BottomNavItem.Categories.route) {
            CategoriesScreen()
        }
        composable(BottomNavItem.Game.route) {
            // Usamos un key único que cambia cada vez que entramos
            val gameKey = remember { "game_${System.currentTimeMillis()}" }
            val gameViewModel: GameViewModel = hiltViewModel(key = gameKey)
            val detailFountainViewModel: DetailFountainViewModel = hiltViewModel()
            val commentsViewModel: FountainCommentsViewModel = hiltViewModel()

            // Limpia el estado cuando la pantalla se destruye
            DisposableEffect(Unit) {
                onDispose {
                    gameViewModel.clearGameState()
                }
            }

            GameScreen(
                viewModel = gameViewModel,
                detailFountainViewModel = detailFountainViewModel,
                commentsViewModel = commentsViewModel,
                onBackToHome = {
                    gameViewModel.clearGameState()  // Limpia antes de navegar
                    soundManager.stopAllSounds()
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
            ProfileScreen(
                navigateToLogin = {
                    soundManager.stopAllSounds()
                    rootNavController.navigate(RootScreen.Login.route) {
                        popUpTo(RootScreen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}