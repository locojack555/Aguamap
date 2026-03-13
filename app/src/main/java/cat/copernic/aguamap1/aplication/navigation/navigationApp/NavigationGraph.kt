package cat.copernic.aguamap1.aplication.navigation.navigationApp

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
import cat.copernic.aguamap1.aplication.Luis
import cat.copernic.aguamap1.aplication.category.CategoriesScreen
import cat.copernic.aguamap1.aplication.fountain.addFountain.AddFountainScreen
import cat.copernic.aguamap1.aplication.fountain.addFountain.AddFountainViewModel
import cat.copernic.aguamap1.aplication.fountain.comments.FountainCommentsViewModel
import cat.copernic.aguamap1.aplication.fountain.detailFountain.DetailFountainScreen
import cat.copernic.aguamap1.aplication.fountain.detailFountain.DetailFountainViewModel
import cat.copernic.aguamap1.aplication.game.GameScreen
import cat.copernic.aguamap1.aplication.game.GameViewModel
import cat.copernic.aguamap1.aplication.game.components.LoadingPartida
import cat.copernic.aguamap1.aplication.map.mapView.MapScreen
import cat.copernic.aguamap1.aplication.map.mapView.MapViewModel
import cat.copernic.aguamap1.aplication.sound.SoundManager
import cat.copernic.aguamap1.aplication.navigation.navigationInitial.RootScreen
import cat.copernic.aguamap1.aplication.profile.ProfileScreen
import cat.copernic.aguamap1.aplication.profile.ProfileViewModel
import cat.copernic.aguamap1.aplication.profile.edit.EditProfileScreen
import cat.copernic.aguamap1.aplication.profile.moderation.ModerationScreen
import cat.copernic.aguamap1.aplication.profile.moderation.ModerationViewModel
import cat.copernic.aguamap1.aplication.profile.reports.FountainReportsScreen
import cat.copernic.aguamap1.aplication.profile.reports.FountainReportsViewModel
import cat.copernic.aguamap1.aplication.profile.settings.SettingsScreen
import cat.copernic.aguamap1.aplication.ranking.RankingScreen
import cat.copernic.aguamap1.ui.theme.Blanco
import com.google.firebase.auth.FirebaseAuth

/**
 * Grafo de navegación principal de la aplicación.
 * Define todas las rutas internas, la gestión de sonidos y la lógica de transición entre pantallas.
 */
@Composable
fun NavigationGraph(
    navController: NavHostController,
    rootNavController: NavHostController,
    soundManager: SoundManager
) {
    val context = LocalContext.current

    // ViewModels compartidos o persistentes durante la navegación principal
    val mapViewModel: MapViewModel = hiltViewModel()
    val addFountainViewModel: AddFountainViewModel = hiltViewModel()

    // Coordenadas actuales obtenidas del ViewModel del mapa
    val currentLatitude = mapViewModel.userLat
    val currentLongitude = mapViewModel.userLng

    // Observa la ruta actual para gestionar efectos secundarios (como el sonido)
    val currentRoute by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
        initialValue = navController.currentBackStackEntry?.destination?.route
    )

    /**
     * Gestión del sonido: Si el usuario sale de la pantalla del Juego,
     * se detienen automáticamente todos los sonidos activos.
     */
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
            // --- RUTA: MAPA PRINCIPAL ---
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

            // --- DETALLE DE FUENTE (GLOBAL) ---
            composable("fountain_detail") {
                val detailViewModel: DetailFountainViewModel = hiltViewModel()
                val commentsViewModel: FountainCommentsViewModel = hiltViewModel()
                val selectedFountain = mapViewModel.selectedFountainForDetail

                val toastConfirm = stringResource(R.string.toast_fountain_confirmed)

                // Inicializa el detalle solo si hay una fuente seleccionada en el estado global
                if (selectedFountain != null) {
                    LaunchedEffect(selectedFountain, currentLatitude, currentLongitude) {
                        detailViewModel.selectFountain(
                            fountain = selectedFountain,
                            userLat = currentLatitude,
                            userLng = currentLongitude
                        )
                    }

                    DetailFountainScreen(
                        fountain = selectedFountain,
                        viewModel = detailViewModel,
                        commentsViewModel = commentsViewModel,
                        userLat = currentLatitude,
                        userLng = currentLongitude,
                        onBack = {
                            // Limpieza de estados y flujos antes de cerrar la pantalla
                            commentsViewModel.stopObserving()
                            detailViewModel.clearSelection()
                            mapViewModel.clearSelectedFountain()
                            navController.popBackStack()
                        },
                        onEdit = {
                            // Abre el overlay de edición de fuente
                            addFountainViewModel.openAddFountain(
                                selectedFountain.latitude,
                                selectedFountain.longitude,
                                selectedFountain
                            )
                        },
                        onConfirm = {
                            // Lógica de confirmación de existencia de fuente
                            detailViewModel.confirmFountain {
                                detailViewModel.selectedFountain?.let { updated ->
                                    mapViewModel.updateSingleFountainInList(updated)
                                }
                                Toast.makeText(context, toastConfirm, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = {
                            // Lógica de borrado definitivo
                            detailViewModel.deleteFountain {
                                mapViewModel.loadFountains()
                                mapViewModel.clearSelectedFountain()
                                navController.popBackStack()
                            }
                        },
                        onReportNoExiste = {
                            // Reporte de fuente inexistente
                            detailViewModel.reportNonExistent {
                                mapViewModel.loadFountains()
                                mapViewModel.clearSelectedFountain()
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
                    },
                    onLuisClick = {
                        navController.navigate("Luis")
                    }
                )
            }

            composable("Luis") {
                Luis(
                    onLuisClick = {
                        navController.navigate(BottomNavItem.Categories.route)
                    }
                )
            }


            // --- RUTA: JUEGO (GAMIFICACIÓN) ---
            composable(BottomNavItem.Game.route) {
                // Se genera una clave única por instancia para asegurar que el juego se reinicie correctamente
                val gameKey = remember { "game_${System.currentTimeMillis()}" }
                val gameViewModel: GameViewModel = hiltViewModel(key = gameKey)

                // El juego requiere ubicación activa para funcionar
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
                    // Pantalla de carga mientras se obtienen las coordenadas
                    LoadingPartida()
                }
            }

            // --- RUTA: RANKING DE USUARIOS ---
            composable(BottomNavItem.Ranking.route) { RankingScreen() }
            /* pasar objeto ranking a otra pantalla
            val rankingViewModel: RankingViewModel = hiltViewModel()
            // Ruta ranking
            composable(BottomNavItem.Ranking.route) {
                RankingScreen(
                    viewModel = rankingViewModel,
                    onPlayerClick = { player ->
                        rankingViewModel.selectPlayer(player)
                        navController.navigate("player_detail")
                    }
                )
            }
            composable("player_detail") {
            val player = rankingViewModel.selectedPlayer
            player?.let { PlayerDetailScreen(it) }
            }
             */
            // --- RUTA: PERFIL DEL USUARIO ---
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    navigateToLogin = {
                        // Cierra sesión y redirige al grafo de navegación inicial (Login)
                        FirebaseAuth.getInstance().signOut()
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

            // --- RUTA: MODERACIÓN (PANEL ADMIN PARA REPORTES DE USUARIOS) ---
            composable("moderation") {
                val moderationViewModel: ModerationViewModel = hiltViewModel()
                val toastError = stringResource(R.string.toast_error_fountain_not_found)

                ModerationScreen(
                    userLat = currentLatitude,
                    userLng = currentLongitude,
                    onBack = {
                        navController.popBackStack()
                    },
                    onGoToFountain = { fountainId ->
                        // Busca la fuente reportada y navega a su detalle
                        moderationViewModel.getFountainById(fountainId) { fountain ->
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

            // --- RUTA: REPORTES DE FUENTES (GESTIÓN DE INCIDENCIAS FÍSICAS) ---
            composable("fountain_reports") {
                val reportsViewModel: FountainReportsViewModel = hiltViewModel()
                val toastError = stringResource(R.string.toast_error_fountain_not_found)

                FountainReportsScreen(
                    userLat = currentLatitude,
                    userLng = currentLongitude,
                    onBack = {
                        navController.popBackStack()
                    },
                    onGoToFountain = { fountainId ->
                        // Busca la fuente con incidencia y navega a su detalle
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

            // --- RUTA: EDICIÓN DE PERFIL ---
            composable("edit_profile") { backStackEntry ->
                // Recupera el ViewModel de la pantalla de perfil padre para mantener coherencia de datos
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

            // --- RUTA: AJUSTES ---
            composable("settings") { SettingsScreen(onClose = { navController.popBackStack() }) }
        }

        /**
         * OVERLAY: AÑADIR/EDITAR FUENTE
         * Se muestra sobre cualquier pantalla del NavHost cuando isAdding es verdadero.
         * Esto permite editar una fuente desde el detalle o añadir una nueva desde el mapa.
         */
        if (addFountainViewModel.isAdding) {
            AddFountainScreen(
                onDismiss = { addFountainViewModel.closeAddFountain() },
                latitude = addFountainViewModel.selectedLocationForNewFountain?.latitude ?: 0.0,
                longitude = addFountainViewModel.selectedLocationForNewFountain?.longitude ?: 0.0,
                viewModel = addFountainViewModel,
                onFountainCreated = { mapViewModel.loadFountains() }
            )
        }
    }
}