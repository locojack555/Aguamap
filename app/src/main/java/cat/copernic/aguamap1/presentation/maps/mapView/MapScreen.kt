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
import cat.copernic.aguamap1.presentation.fountain.comments.FountainCommentsViewModel
import cat.copernic.aguamap1.presentation.fountain.detailFountain.DetailFountainScreen
import cat.copernic.aguamap1.presentation.fountain.detailFountain.DetailFountainViewModel
import cat.copernic.aguamap1.presentation.maps.components.MapFloatingButtons
import cat.copernic.aguamap1.presentation.maps.components.MapLegend
import cat.copernic.aguamap1.presentation.maps.components.MapTopBar
import cat.copernic.aguamap1.presentation.maps.components.OSMMapContent
import cat.copernic.aguamap1.presentation.maps.listView.ListScreen
import cat.copernic.aguamap1.presentation.util.PermissionRequestUI
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.osmdroid.views.MapView

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel = hiltViewModel(),
    detailFountainViewModel: DetailFountainViewModel = hiltViewModel(),
    addViewModel: AddFountainViewModel = hiltViewModel(),
    commentsViewModel: FountainCommentsViewModel = hiltViewModel(),
    isHome: Boolean
) {
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val isMapView by mapViewModel.isMapView.collectAsState()
    val isAdding = addViewModel.isAdding
    val selectedFountain = detailFountainViewModel.selectedFountain
    // Si categories es un StateFlow, usa collectAsState
    // val categories by mapViewModel.categories.collectAsState()

    // Si categories es un mutableStateOf, úsalo directamente
    val categories = mapViewModel.categories // Asumiendo que es un State<List<Category>>

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            // Capa Superior: Pantalla Agregar
            isAdding && mapViewModel.isLocationAvailable -> {
                AddFountainScreen(
                    latitude = mapViewModel.userLat!!,
                    longitude = mapViewModel.userLng!!,
                    viewModel = addViewModel,
                    onDismiss = { addViewModel.closeAddFountain() },
                    onFountainCreated = {
                        addViewModel.closeAddFountain()
                        mapViewModel.loadFountains()
                    }
                )
            }

            // Capa Superior: Detalle de Fuente
            selectedFountain != null -> {
                DetailFountainScreen(
                    fountain = selectedFountain,
                    viewModel = detailFountainViewModel,
                    commentsViewModel = commentsViewModel,
                    onBack = { detailFountainViewModel.clearSelection() },
                    onDelete = {
                        detailFountainViewModel.deleteFountain { mapViewModel.loadFountains() }
                    },
                    onConfirm = {
                        detailFountainViewModel.confirmFountain { mapViewModel.loadFountains() }
                    },
                    onReportAveria = {
                        detailFountainViewModel.toggleOperationalStatus { mapViewModel.loadFountains() }
                    },
                    onReportNoExiste = {
                        detailFountainViewModel.reportNonExistent { mapViewModel.loadFountains() }
                    }
                )
            }

            // Capa Base: Mapa o Lista
            locationPermissionState.status.isGranted -> {
                if (isMapView) {
                    OSMMapContent(
                        viewModel = mapViewModel,
                        isHome = isHome,
                        onMapLoad = { mapViewRef = it },
                        onFountainClick = { detailFountainViewModel.selectFountain(it) }
                    )
                    MapLegend(
                        categories = categories,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                } else {
                    ListScreen(
                        viewModel = mapViewModel,
                        onFountainClick = { detailFountainViewModel.selectFountain(it) }
                    )
                }

                // UI flotante
                MapFloatingButtons(
                    mapViewRef = mapViewRef,
                    addViewModel = addViewModel,
                    mapViewModel = mapViewModel,
                    isMapView = isMapView,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )

                if (isHome) {
                    MapTopBar(
                        isMapView = isMapView,
                        onToggleView = { mapViewModel.toggleView() }
                    )
                }
            }

            else -> {
                PermissionRequestUI {
                    if (locationPermissionState.status.shouldShowRationale) {
                        locationPermissionState.launchPermissionRequest()
                    } else {
                        locationPermissionState.launchPermissionRequest()
                    }
                }
            }
        }
    }
}