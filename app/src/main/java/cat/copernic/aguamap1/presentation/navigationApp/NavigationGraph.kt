package cat.copernic.aguamap1.presentation.navigationApp

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cat.copernic.aguamap1.presentation.categories.CategoriesScreen
import cat.copernic.aguamap1.presentation.fountain.addFountain.AddFountainScreen
import cat.copernic.aguamap1.presentation.fountain.addFountain.AddFountainViewModel
import cat.copernic.aguamap1.presentation.fountain.comments.FountainCommentsViewModel
import cat.copernic.aguamap1.presentation.fountain.detailFountain.DetailFountainScreen
import cat.copernic.aguamap1.presentation.fountain.detailFountain.DetailFountainViewModel
import cat.copernic.aguamap1.presentation.game.GameScreen
import cat.copernic.aguamap1.presentation.game.GameViewModel
import cat.copernic.aguamap1.presentation.maps.mapView.MapScreen
import cat.copernic.aguamap1.presentation.maps.mapView.MapViewModel
import cat.copernic.aguamap1.presentation.music.SoundManager
import cat.copernic.aguamap1.presentation.navigationInitial.RootScreen
import cat.copernic.aguamap1.presentation.profile.EditProfileScreen
import cat.copernic.aguamap1.presentation.profile.ProfileScreen
import cat.copernic.aguamap1.presentation.profile.ProfileViewModel
import cat.copernic.aguamap1.presentation.ranking.RankingScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    rootNavController: NavHostController,
    soundManager: SoundManager
) {
    val context = LocalContext.current
    val mapViewModel: MapViewModel = hiltViewModel()
    val addFountainViewModel: AddFountainViewModel = hiltViewModel()

    val currentRoute by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
        initialValue = navController.currentBackStackEntry?.destination?.route
    )

    LaunchedEffect(currentRoute) {
        if (currentRoute != BottomNavItem.Game.route) {
            soundManager.stopAllSounds()
        }
    }

    // Usamos un Box para asegurar que AddFountainScreen se dibuje ENCIMA de NavHost
    Box(modifier = Modifier.fillMaxSize()) {

        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Map.route,
            modifier = Modifier.fillMaxSize()
        ) {
            // --- MAPA ---
            composable(BottomNavItem.Map.route) {
                MapScreen(
                    isHome = true,
                    onFountainClick = { fountain ->
                        mapViewModel.selectFountain(fountain)
                        navController.navigate("fountain_detail")
                    }
                )
            }

            // --- DETALLE DE FUENTE ---
            composable("fountain_detail") {
                val detailViewModel: DetailFountainViewModel = hiltViewModel()
                val commentsViewModel: FountainCommentsViewModel = hiltViewModel()
                val selectedFountain = mapViewModel.selectedFountainForDetail

                if (selectedFountain != null) {
                    LaunchedEffect(selectedFountain) {
                        detailViewModel.selectFountain(selectedFountain)
                    }

                    DetailFountainScreen(
                        fountain = selectedFountain,
                        viewModel = detailViewModel,
                        addFountainViewModel = addFountainViewModel,
                        commentsViewModel = commentsViewModel,
                        onBack = {
                            navController.popBackStack()
                            mapViewModel.clearSelectedFountain()
                        },
                        onEdit = {
                            addFountainViewModel.openAddFountain(
                                selectedFountain.latitude,
                                selectedFountain.longitude,
                                selectedFountain
                            )
                        },
                        onConfirm = {
                            detailViewModel.confirmFountain {
                                detailViewModel.selectedFountain?.let { updated ->
                                    mapViewModel.updateSingleFountainInList(updated)
                                }
                                Toast.makeText(context, "Fuente confirmada correctamente", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = {
                            detailViewModel.deleteFountain {
                                mapViewModel.loadFountains()
                                navController.popBackStack()
                            }
                        },
                        onReportAveria = {
                            detailViewModel.toggleOperationalStatus {
                                detailViewModel.selectedFountain?.let { updated ->
                                    mapViewModel.updateSingleFountainInList(updated)
                                }
                            }
                        },
                        onReportNoExiste = {
                            detailViewModel.reportNonExistent {
                                mapViewModel.loadFountains()
                                navController.popBackStack()
                            }
                        }
                    )
                }
            }

            // --- CATEGORÍAS ---
            composable(BottomNavItem.Categories.route) {
                CategoriesScreen(
                    onFountainClick = { fountain ->
                        mapViewModel.selectFountain(fountain)
                        navController.navigate("fountain_detail")
                    }
                )
            }

            // --- JUEGO ---
            composable(BottomNavItem.Game.route) {
                val gameKey = remember { "game_${System.currentTimeMillis()}" }
                val gameViewModel: GameViewModel = hiltViewModel(key = gameKey)

                GameScreen(
                    viewModel = gameViewModel,
                    onBackToHome = {
                        gameViewModel.clearGameState()
                        soundManager.stopAllSounds()
                        navController.navigate(BottomNavItem.Map.route) {
                            popUpTo(BottomNavItem.Map.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onFountainClick = { fountain ->
                        mapViewModel.selectFountain(fountain)
                        navController.navigate("fountain_detail")
                    }
                )
            }

            composable(BottomNavItem.Ranking.route) {
                RankingScreen()
            }

            composable(BottomNavItem.Profile.route) {
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
                    }
                )
            }

            composable("edit_profile") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(BottomNavItem.Profile.route)
                }
                val profileViewModel: ProfileViewModel = hiltViewModel(parentEntry)
                val profileState by profileViewModel.profileState.collectAsStateWithLifecycle()

                EditProfileScreen(
                    initialNombre = profileState.userName,
                    viewModel = profileViewModel,
                    onBack = { navController.popBackStack() },
                    onSaveComplete = {
                        profileViewModel.loadUserData()
                        navController.popBackStack()
                    }
                )
            }
        }

        // --- SOLUCIÓN: LA PANTALLA SE DECLARA DESPUÉS DEL NAVHOST PARA QUE QUEDE ENCIMA ---
        if (addFountainViewModel.isAdding) {
            AddFountainScreen(
                onDismiss = { addFountainViewModel.closeAddFountain() },
                latitude = addFountainViewModel.selectedLocationForNewFountain?.latitude ?: 0.0,
                longitude = addFountainViewModel.selectedLocationForNewFountain?.longitude ?: 0.0,
                viewModel = addFountainViewModel,
                onFountainCreated = {
                    mapViewModel.loadFountains()
                    // Si estamos en detalle, refrescamos la fuente actual para ver los cambios
                    mapViewModel.selectedFountainForDetail?.let {
                        // Aquí podrías llamar a detailViewModel.refresh si tuvieras acceso,
                        // pero al cerrar y volver a entrar ya se verá.
                    }
                }
            )
        }
    }
}