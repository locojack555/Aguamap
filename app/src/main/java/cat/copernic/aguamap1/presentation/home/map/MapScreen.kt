package cat.copernic.aguamap1.presentation.home.map

import android.Manifest
import android.graphics.Canvas
import android.location.LocationManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.home.list.ListScreen
import cat.copernic.aguamap1.presentation.util.getMarkerColor
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.Rojo
import cat.copernic.aguamap1.ui.theme.Verde
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    isHome: Boolean
) {
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val isMapView by viewModel.isMapView.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        if (locationPermissionState.status.isGranted) {
            if (isMapView) {
                OSMMapContent(viewModel, isHome, onMapLoad = { map -> mapViewRef = map })
                MapLegend(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(top = 60.dp)
                )
                MapFloatingButtons(
                    mapViewRef = mapViewRef,
                    viewModel = viewModel,
                    modifier = Modifier.align(Alignment.BottomEnd) // La posicionamos aquí
                )
            } else {
                ListScreen(viewModel)
            }
            if (isHome) {
                HomeTopBar(
                    isMapView = isMapView,
                    onToggleView = { viewModel.toggleView() }
                )
            }
        } else {
            PermissionRequestUI {
                locationPermissionState.launchPermissionRequest()
            }
        }
    }
}

@Composable
fun OSMMapContent(viewModel: MapViewModel? = null, isHome: Boolean, onMapLoad: (MapView) -> Unit) {
    val state = viewModel?.uiState
    val context = LocalContext.current
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
                if (isHome && viewModel != null) {
                    setupHomeMap(viewModel)
                } else {
                    //formato de juego
                }
                onMapLoad(this)
            }
        },
        update = { mapView ->
            if (viewModel != null && state != null) {
                mapView.overlays.removeAll { it is Marker }
                state.fountains.forEach { fountain ->
                    val marker = Marker(mapView)
                    marker.position = GeoPoint(fountain.latitude, fountain.longitude)
                    marker.title = fountain.name
                    val drawable = ContextCompat.getDrawable(context, R.drawable.pin_lleno)
                    val wrapped = drawable?.let {
                        val w = DrawableCompat.wrap(it).mutate()
                        val colorInt = fountain.getMarkerColor()
                        DrawableCompat.setTint(w, colorInt)
                        w
                    }
                    val width = 120
                    val height = 120
                    val bitmap = createBitmap(width, height)
                    val canvas = Canvas(bitmap)
                    wrapped?.setBounds(0, 0, width, height)
                    wrapped?.draw(canvas)
                    marker.icon = bitmap.toDrawable(context.resources)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    //marker.image = null
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
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

@Composable
fun MapFloatingButtons(
    mapViewRef: MapView?,
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Botón Añadir Fuente
        FloatingActionButton(
            onClick = {
                /*val locationOverlay =
                    mapViewRef?.overlays?.find { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                val currentLoc = locationOverlay?.myLocation
                if (currentLoc != null) {
                    viewModel.addTestFountain(
                        isAdmin = true,
                        lat = currentLoc.latitude,
                        lng = currentLoc.longitude
                    )
                }*/
            },
            containerColor = Blanco,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.add_24px),
                contentDescription = "Añadir fuente",
                modifier = Modifier.size(24.dp),
                tint = Rojo
            )
        }
        // Botón Mi Ubicación
        FloatingActionButton(
            onClick = {
                mapViewRef?.let { map ->
                    val locationOverlay =
                        map.overlays.find { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                    locationOverlay?.myLocation?.let { lastFix ->
                        map.controller.animateTo(lastFix)
                    }
                }
            },
            containerColor = Blanco,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.icon_map_blue),
                contentDescription = "Ir a mi ubicación",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun MapLegend(modifier: Modifier = Modifier) {
    androidx.compose.material3.Surface(
        modifier = modifier.padding(16.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = Blanco.copy(alpha = 0.9f),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LegendItem(color = Verde, text = stringResource(R.string.legend_bebible))
            LegendItem(color = Blue10, text = stringResource(R.string.legend_ornamental))
            LegendItem(color = Rojo, text = stringResource(R.string.legend_averiada))
            LegendItem(color = Naranja, text = stringResource(R.string.legend_pendiente))
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Negro
        )
    }
}
