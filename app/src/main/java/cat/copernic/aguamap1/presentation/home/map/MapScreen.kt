package cat.copernic.aguamap1.presentation.home.map

import android.Manifest
import android.location.LocationManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.reusable.PermissionRequestUI
import cat.copernic.aguamap1.ui.theme.Blanco
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(viewModel: MapViewModel = viewModel(), isHome: Boolean) {
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    Box(modifier = Modifier.fillMaxSize()) {
        if (locationPermissionState.status.isGranted) {
            OSMMapContent(viewModel, isHome)
            if (isHome) {
                FloatingActionButton(
                    onClick = {
                        mapViewRef?.let { map ->
                            val locationOverlay =
                                map.overlays.find { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                            locationOverlay?.myLocation?.let { lastFix ->
                                map.controller.animateTo(lastFix)
                                map.controller.setZoom(17.0)
                                viewModel.isFirstLocationUpdate = true
                                viewModel.onFirstLocationFound(lastFix.latitude, lastFix.longitude)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = Blanco,
                    contentColor = Color(0xFF1A73E8)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_map_blue),
                        contentDescription = "Ir a mi ubicación",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            PermissionRequestUI {
                locationPermissionState.launchPermissionRequest()
            }
        }
    }
}

@Composable
fun OSMMapContent(viewModel: MapViewModel, isHome: Boolean) {
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                minZoomLevel = 3.0
                isVerticalMapRepetitionEnabled = false
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                val maxLat = MapView.getTileSystem().maxLatitude
                val minLat = MapView.getTileSystem().minLatitude
                setScrollableAreaLimitLatitude(maxLat, minLat, 0)
                if (isHome) {
                    setupHomeMap(viewModel)
                } else {
                    //formato de juego
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

// Función de extensión para configurar el mapa si estamos en HomeMap
fun MapView.setupHomeMap(viewModel: MapViewModel) {
    val ctx = context
    val sizeInPx = (28 * ctx.resources.displayMetrics.density).toInt()
    val customIcon = ContextCompat.getDrawable(ctx, R.drawable.icon_map_g)
        ?.toBitmap()?.scale(sizeInPx, sizeInPx, false)
    val provider = GpsMyLocationProvider(ctx).apply {
        addLocationSource(LocationManager.GPS_PROVIDER)
    }
    val locationOverlay = MyLocationNewOverlay(provider, this).apply {
        enableMyLocation()
        isDrawAccuracyEnabled = false
        customIcon?.let {
            setPersonIcon(it)
            setDirectionIcon(it)
            setPersonAnchor(0.5f, 0.5f)
        }
    }
    controller.setZoom(viewModel.zoomLevel)
    controller.setCenter(GeoPoint(viewModel.latitude, viewModel.longitude))
    locationOverlay.runOnFirstFix {
        if (viewModel.isFirstLocationUpdate) {
            val myLoc = locationOverlay.myLocation
            if (myLoc != null) {
                post {
                    controller.setCenter(myLoc)
                    controller.setZoom(17.0)
                    viewModel.onFirstLocationFound(myLoc.latitude, myLoc.longitude)
                }
            }
        }
    }
    overlays.add(locationOverlay)
    addMapListener(object : org.osmdroid.events.MapListener {
        override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
            viewModel.onMapMoved(mapCenter.latitude, mapCenter.longitude, zoomLevelDouble)
            return true
        }

        override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
            viewModel.onMapMoved(mapCenter.latitude, mapCenter.longitude, zoomLevelDouble)
            return true
        }
    })
}
