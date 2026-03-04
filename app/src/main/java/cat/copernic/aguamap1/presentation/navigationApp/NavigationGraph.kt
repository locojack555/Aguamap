package cat.copernic.aguamap1.presentation.navigationApp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cat.copernic.aguamap1.R
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
import cat.copernic.aguamap1.presentation.profile.ProfileScreen
import cat.copernic.aguamap1.presentation.profile.ProfileViewModel
import cat.copernic.aguamap1.presentation.profile.edit.EditProfileScreen
import cat.copernic.aguamap1.presentation.profile.moderation.ModerationScreen
import cat.copernic.aguamap1.presentation.profile.reports.FountainReportsScreen
import cat.copernic.aguamap1.presentation.profile.reports.FountainReportsViewModel
import cat.copernic.aguamap1.presentation.profile.settings.SettingsScreen
import cat.copernic.aguamap1.presentation.ranking.RankingScreen
import cat.copernic.aguamap1.ui.theme.Blanco

/**
 * Grafo de Navegación Principal de la Aplicación.
 * Centraliza las rutas, la inyección de ViewModels y las transiciones entre pantallas.
 */
@Composable
fun NavigationGraph(
    navController: NavHostController,      // Navegador interno (BottomNav)
    rootNavController: NavHostController,  // Navegador raíz (Login/Home)
    soundManager: SoundManager             // Gestor de audio global
) {
    val context = LocalContext.current

    // --- VIEWMODELS COMPARTIDOS ---
    // Al instanciarlos aquí, MapViewModel mantiene las fuentes cargadas aunque naveguemos
    // a Detalle o Categorías, evitando recargas innecesarias.
    val mapViewModel: MapViewModel = hiltViewModel()
    val addFountainViewModel: AddFountainViewModel = hiltViewModel()

    val currentLatitude = mapViewModel.userLat
    val currentLongitude = mapViewModel.userLng

    // Observa la ruta actual para gestionar efectos secundarios (como el sonido)
    val currentRoute by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
        initialValue = navController.currentBackStackEntry?.destination?.route
    )

    // Lógica de Audio: Detiene la música si el usuario sale de la pantalla del Juego
    LaunchedEffect(currentRoute) {
        if (currentRoute != BottomNavItem.Game.route) {
            soundManager.stopAllSounds()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Map.route,
            modifier = Modifier
                .fillMaxSize()
                .background(Blanco)
        ) {
            // --- RUTA: MAPA ---
            composable(BottomNavItem.Map.route) {
                MapScreen(
                    isHome = true,
                    viewModel = mapViewModel,
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

                // Strings localizados para Toasts
                val toastConfirm = stringResource(R.string.toast_fountain_confirmed)
                val toastError = stringResource(R.string.toast_error_fountain_not_found)

                if (selectedFountain != null) {
                    LaunchedEffect(selectedFountain) {
                        detailViewModel.selectFountain(selectedFountain)
                    }

                    DetailFountainScreen(
                        fountain = selectedFountain,
                        viewModel = detailViewModel,
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
                                // Actualiza la lista del mapa en memoria para reflejar el cambio de estado
                                detailViewModel.selectedFountain?.let { updated ->
                                    mapViewModel.updateSingleFountainInList(updated)
                                }
                                Toast.makeText(context, toastConfirm, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = {
                            detailViewModel.deleteFountain {
                                mapViewModel.loadFountains() // Recarga completa al borrar
                                navController.popBackStack()
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

            // --- RUTA: CATEGORÍAS ---
            composable(BottomNavItem.Categories.route) {
                CategoriesScreen(
                    userLat = currentLatitude,
                    userLng = currentLongitude,
                    onFountainClick = { fountain ->
                        mapViewModel.selectFountain(fountain)
                        navController.navigate("fountain_detail")
                    }
                )
            }

            // --- RUTA: JUEGO ---
            composable(BottomNavItem.Game.route) {
                // Se usa una key única para que el juego se reinicie totalmente al entrar
                val gameKey = remember { "game_${System.currentTimeMillis()}" }
                val gameViewModel: GameViewModel = hiltViewModel(key = gameKey)

                if (currentLatitude != null && currentLongitude != null) {
                    GameScreen(
                        viewModel = gameViewModel,
                        userLat = currentLatitude,
                        userLng = currentLongitude,
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
                } else {
                    cat.copernic.aguamap1.presentation.game.components.LoadingPartida()
                }
            }

            // --- RUTA: RANKING ---
            composable(BottomNavItem.Ranking.route) { RankingScreen() }

            // --- RUTA: PERFIL Y NAVEGACIÓN DE AJUSTES ---
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    navigateToLogin = {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        soundManager.stopAllSounds()
                        rootNavController.navigate(RootScreen.Login.route) {
                            popUpTo(RootScreen.Home.route) { inclusive = true }
                        }
                    },
                    navigateToEditProfile = { navController.navigate("edit_profile") },
                    navigateToSettings = { navController.navigate("settings") },
                    navigateToModeration = { navController.navigate("moderation") },
                    navigateToFountainReports = { navController.navigate("fountain_reports") }
                )
            }

            // --- RUTAS SECUNDARIAS DEL PERFIL ---
            composable("edit_profile") { backStackEntry ->
                // Recupera el ViewModel del padre (ProfileScreen) para compartir los datos
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(BottomNavItem.Profile.route)
                }
                val profileViewModel: ProfileViewModel = hiltViewModel(parentEntry)

                EditProfileScreen(
                    viewModel = profileViewModel,
                    onBack = { navController.popBackStack() },
                    onSaveComplete = {
                        profileViewModel.loadUserData()
                        navController.popBackStack()
                    }
                )
            }

            composable("settings") { SettingsScreen(onClose = { navController.popBackStack() }) }
            composable("moderation") { ModerationScreen(onBack = { navController.popBackStack() }) }

            composable("fountain_reports") {
                val reportsViewModel: FountainReportsViewModel = hiltViewModel()
                val toastError = stringResource(R.string.toast_error_fountain_not_found)

                FountainReportsScreen(
                    onBack = { navController.popBackStack() },
                    onGoToFountain = { fountainId ->
                        reportsViewModel.getFountainById(fountainId) { fountain ->
                            if (fountain != null) {
                                mapViewModel.selectFountain(fountain)
                                navController.navigate("fountain_detail")
                            } else {
                                Toast.makeText(context, toastError, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }

        // --- OVERLAY: PANTALLA AÑADIR FUENTE ---
        // Se maneja fuera del NavHost como un "overlay" condicional para que pueda
        // aparecer sobre cualquier pantalla si `isAdding` es true.
        if (addFountainViewModel.isAdding) {
            AddFountainScreen(
                onDismiss = { addFountainViewModel.closeAddFountain() },
                latitude = addFountainViewModel.selectedLocationForNewFountain?.latitude ?: 0.0,
                longitude = addFountainViewModel.selectedLocationForNewFountain?.longitude ?: 0.0,
                viewModel = addFountainViewModel,
                onFountainCreated = {
                    mapViewModel.loadFountains() // Refresca el mapa tras crear una fuente
                }
            )
        }
    }
}
