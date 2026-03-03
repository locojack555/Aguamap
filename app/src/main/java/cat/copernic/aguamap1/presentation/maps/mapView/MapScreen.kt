package cat.copernic.aguamap1.presentation.maps.mapView

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
import cat.copernic.aguamap1.presentation.fountain.addFountain.AddFountainScreen
import cat.copernic.aguamap1.presentation.fountain.addFountain.AddFountainViewModel
import cat.copernic.aguamap1.presentation.maps.components.MapFloatingButtons
import cat.copernic.aguamap1.presentation.maps.components.MapLegend
import cat.copernic.aguamap1.presentation.maps.components.MapTopBar
import cat.copernic.aguamap1.presentation.maps.components.OSMMapContent
import cat.copernic.aguamap1.presentation.maps.listView.ListScreen
import cat.copernic.aguamap1.presentation.util.PermissionRequestUI
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.osmdroid.views.MapView

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    addViewModel: AddFountainViewModel = hiltViewModel(),
    onFountainClick: (cat.copernic.aguamap1.domain.model.Fountain) -> Unit,
    isHome: Boolean
) {
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val isMapView by viewModel.isMapView.collectAsState()
    val isAdding = addViewModel.isAdding
    val categories = viewModel.categories

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            // 1. Capa Superior: Pantalla Agregar (Los textos internos de AddFountainScreen deben estar en su propia clase)
            isAdding && viewModel.isLocationAvailable -> {
                AddFountainScreen(
                    latitude = viewModel.userLat!!,
                    longitude = viewModel.userLng!!,
                    viewModel = addViewModel,
                    onDismiss = { addViewModel.closeAddFountain() },
                    onFountainCreated = {
                        addViewModel.closeAddFountain()
                        viewModel.loadFountains()
                    }
                )
            }

            // 2. Capa Base: Mapa o Lista si hay permisos
            locationPermissionState.status.isGranted -> {
                if (isMapView) {
                    OSMMapContent(
                        viewModel = viewModel,
                        isHome = isHome,
                        onMapLoad = { mapViewRef = it },
                        onFountainClick = { fountain ->
                            onFountainClick(fountain)
                        }
                    )
                    // La leyenda ya usa stringResource internamente según lo que hicimos antes
                    MapLegend(
                        categories = categories,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                } else {
                    ListScreen(
                        viewModel = viewModel,
                        onFountainClick = { fountain ->
                            onFountainClick(fountain)
                        }
                    )
                }

                // UI flotante (Botones de añadir y centrar con descripciones localizadas)
                MapFloatingButtons(
                    mapViewRef = mapViewRef,
                    addViewModel = addViewModel,
                    mapViewModel = viewModel,
                    isMapView = isMapView,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )

                // Barra de búsqueda y filtros
                if (isHome) {
                    MapTopBar(
                        isMapView = isMapView,
                        onToggleView = { viewModel.toggleView() }
                    )
                }
            }

            // 3. Caso sin permisos: Se muestra la UI de solicitud
            else -> {
                PermissionRequestUI {
                    locationPermissionState.launchPermissionRequest()
                }
            }
        }
    }
}