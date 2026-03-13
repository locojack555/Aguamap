package cat.copernic.aguamap1.aplication.map.mapView

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.aplication.fountain.addFountain.AddFountainScreen
import cat.copernic.aguamap1.aplication.fountain.addFountain.AddFountainViewModel
import cat.copernic.aguamap1.aplication.map.components.MapFloatingButtons
import cat.copernic.aguamap1.aplication.map.components.MapLegend
import cat.copernic.aguamap1.aplication.map.components.MapTopBar
import cat.copernic.aguamap1.aplication.map.components.OSMMapContent
import cat.copernic.aguamap1.aplication.map.listView.ListScreen
import cat.copernic.aguamap1.aplication.utils.PermissionRequestUI
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.osmdroid.views.MapView

/**
 * Pantalla principal que orquesta la visualización del Mapa y la Lista de fuentes.
 * Gestiona los permisos de ubicación en tiempo real y decide qué capa de UI mostrar
 * (Filtros, Mapa, Lista o el formulario de Añadir Fuente).
 *
 * @param viewModel Lógica de estado para la búsqueda, filtros y datos de fuentes.
 * @param addViewModel Lógica para el proceso de creación de nuevas fuentes.
 * @param onFountainClick Navegación al detalle de una fuente específica.
 * @param isHome Define si se deben mostrar los controles de navegación superior (TopBar).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    addViewModel: AddFountainViewModel = hiltViewModel(),
    onFountainClick: (Fountain) -> Unit,
    isHome: Boolean
) {
    // Gestión de permisos mediante la librería Accompanist
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Referencia al objeto MapView de OSMDroid para centrar la cámara manualmente
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    // Observación de estados reactivos
    val isMapView by viewModel.isMapView.collectAsState()
    val isAdding = addViewModel.isAdding
    val categories = viewModel.categories

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            /**
             * 1. CAPA DE EDICIÓN:
             * Si el usuario ha pulsado el botón '+' y tenemos su ubicación,
             * superponemos la pantalla de registro de fuente.
             */
            isAdding && viewModel.isLocationAvailable -> {
                AddFountainScreen(
                    latitude = viewModel.userLat!!,
                    longitude = viewModel.userLng!!,
                    viewModel = addViewModel,
                    onDismiss = { addViewModel.closeAddFountain() },
                    onFountainCreated = {
                        addViewModel.closeAddFountain()
                        viewModel.loadFountains() // Refrescar puntos tras la creación
                    }
                )
            }

            /**
             * 2. CAPA PRINCIPAL (MAPA O LISTA):
             * Se renderiza solo si el usuario ha concedido permisos de GPS.
             */
            locationPermissionState.status.isGranted -> {
                if (isMapView) {
                    // Vista de Mapa (OpenStreetMap)
                    OSMMapContent(
                        viewModel = viewModel,
                        isHome = isHome,
                        onMapLoad = { mapViewRef = it },
                        onFountainClick = { fountain ->
                            onFountainClick(fountain)
                        }
                    )

                    // Leyenda de colores explicativa
                    MapLegend(
                        categories = categories,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                } else {
                    // Vista de Lista (Scroll vertical con detalles)
                    ListScreen(
                        viewModel = viewModel,
                        onFountainClick = { fountain ->
                            onFountainClick(fountain)
                        }
                    )
                }

                /**
                 * ELEMENTOS FLOTANTES:
                 * Botones de acción rápida y Barra de búsqueda (si estamos en Home).
                 */
                MapFloatingButtons(
                    mapViewRef = mapViewRef,
                    addViewModel = addViewModel,
                    mapViewModel = viewModel,
                    isMapView = isMapView,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )

                if (isHome) {
                    MapTopBar(
                        isMapView = isMapView,
                        onToggleView = { viewModel.toggleView() },
                        viewModel = viewModel,
                        //poner el buscador abajo
                        //modifier=Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            /**
             * 3. ESTADO DE PERMISOS:
             * Si el usuario deniega o no ha solicitado permisos, mostramos una UI informativa.
             */
            else -> {
                PermissionRequestUI {
                    locationPermissionState.launchPermissionRequest()
                }
            }
        }
    }
}