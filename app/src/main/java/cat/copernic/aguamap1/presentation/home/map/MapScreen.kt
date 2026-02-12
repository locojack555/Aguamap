package cat.copernic.aguamap1.presentation.home.map

import android.Manifest
import android.location.LocationManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.reusable.PermissionRequestUI
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(viewModel: MapViewModel = viewModel()) {
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (locationPermissionState.status.isGranted) {
            OSMMapContent(viewModel)
        } else {
            PermissionRequestUI {
                locationPermissionState.launchPermissionRequest()
            }
        }
    }
}

@Composable
fun OSMMapContent(viewModel: MapViewModel) {
    val context = LocalContext.current
    val sizeInPx = (16 * context.resources.displayMetrics.density).toInt()
    val sizeInPx2 = (28 * context.resources.displayMetrics.density).toInt()
    val customIcon = ContextCompat.getDrawable(context, R.drawable.icon_map_blue)
        ?.toBitmap()?.scale(sizeInPx, sizeInPx)
    val customIcon2 = ContextCompat.getDrawable(context, R.drawable.icon_map_g)
        ?.toBitmap()?.scale(sizeInPx2, sizeInPx2)
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)

                // --- CONFIGURACIÓN DE UI ---
                setMultiTouchControls(true)
                setBuiltInZoomControls(false)
                minZoomLevel = 4.0
                isHorizontalMapRepetitionEnabled = false
                isVerticalMapRepetitionEnabled = false

                // --- CONFIGURACIÓN DE ORIENTACIÓN ---
                val compassProvider = InternalCompassOrientationProvider(ctx)

                // IMPORTANTE: En la 6.1.20 usamos CompassOverlay para el haz de luz
                val compassOverlay = CompassOverlay(ctx, compassProvider, this).apply {
                    enableCompass()
                }

                // --- CONFIGURACIÓN DE UBICACIÓN ---
                val provider = GpsMyLocationProvider(ctx).apply {
                    addLocationSource(LocationManager.GPS_PROVIDER)
                    addLocationSource(LocationManager.NETWORK_PROVIDER)
                }

                val locationOverlay = MyLocationNewOverlay(provider, this).apply {
                    enableMyLocation()
                    isDrawAccuracyEnabled = true
                    customIcon?.let {
                        setPersonIcon(it)
                        setPersonAnchor(0.5f, 0.5f)
                    }

                    // Icono de dirección (el que usaremos para navegar/orientación)
                    customIcon2?.let {
                        setDirectionIcon(it)
                        setDirectionAnchor(0.5f, 0.5f)
                    }
                }

                // --- CARGA Y LISTENERS ---
                controller.setZoom(viewModel.zoomLevel)
                controller.setCenter(GeoPoint(viewModel.latitude, viewModel.longitude))

                locationOverlay.runOnFirstFix {
                    if (viewModel.isFirstLocationUpdate) {
                        val myLoc = locationOverlay.myLocation
                        if (myLoc != null) {
                            post {
                                controller.setCenter(myLoc)
                                controller.setZoom(17.0)
                                viewModel.saveState(myLoc.latitude, myLoc.longitude, 17.0)
                                viewModel.isFirstLocationUpdate = false
                            }
                        }
                    }
                }

                overlays.add(locationOverlay)
                overlays.add(compassOverlay)

                addMapListener(object : org.osmdroid.events.MapListener {
                    override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                        viewModel.saveState(
                            mapCenter.latitude,
                            mapCenter.longitude,
                            zoomLevelDouble
                        )
                        return true
                    }

                    override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                        viewModel.saveState(
                            mapCenter.latitude,
                            mapCenter.longitude,
                            zoomLevelDouble
                        )
                        return true
                    }
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
