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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import cat.copernic.aguamap1.domain.model.Comment
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
    addViewModel: AddFountainViewModel = hiltViewModel(),
    isHome: Boolean
) {
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val isMapView by viewModel.isMapView.collectAsState()

    // Estados para el diálogo separado
    var showCommentDialog by remember { mutableStateOf(false) }
    var editingComment by remember { mutableStateOf<Comment?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (locationPermissionState.status.isGranted) {
            if (isMapView) {
                OSMMapContent(viewModel, isHome, onMapLoad = { map -> mapViewRef = map })
                MapLegend(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                    //.padding(bottom = 12.dp)
                )
            } else {
                ListScreen(viewModel)
            }

            MapFloatingButtons(
                mapViewRef = mapViewRef,
                viewModel = viewModel,
                addViewModel = addViewModel,
                isMapView = isMapView,
                modifier = Modifier.align(Alignment.BottomEnd)
            )

            if (isHome) {
                HomeTopBar(isMapView = isMapView, onToggleView = { viewModel.toggleView() })
            }
        } else {
            PermissionRequestUI { locationPermissionState.launchPermissionRequest() }
        }

        // --- LÓGICA DE AÑADIR FUENTE ---
        if (addViewModel.showAddFountainSheet && addViewModel.selectedLocationForNewFountain != null) {
            AddFountainScreen(
                onDismiss = { addViewModel.closeAddFountain() },
                latitude = addViewModel.selectedLocationForNewFountain!!.latitude,
                longitude = addViewModel.selectedLocationForNewFountain!!.longitude,
                viewModel = addViewModel,
                onFountainCreated = { viewModel.loadFountains() }
            )
        }

        // --- PANTALLA DE DETALLES (CON TODA LA LÓGICA ADMIN/USER) ---
        viewModel.selectedFountain?.let { fountain ->
            DetailFountainScreen(
                fountain = fountain,
                isAdmin = viewModel.isAdmin,
                currentUserId = viewModel.currentUserId,
                onBack = { viewModel.clearSelection() },
                onDelete = { viewModel.deleteSelectedFountain() },
                onEdit = { viewModel.updateFountainData(mapOf("name" to fountain.name)) }, // Ejemplo
                onConfirm = { viewModel.confirmFountain() },
                onReportAveria = { viewModel.reportBroken() },
                onReportNoExiste = { viewModel.reportNonExistent() },
                onAddComment = {
                    editingComment = null
                    showCommentDialog = true
                },
                onCensorComment = { commentId -> viewModel.censorComment(commentId) },
                onDeleteComment = { commentId -> viewModel.deleteComment(commentId) },
                onEditComment = { comment ->
                    editingComment = comment
                    showCommentDialog = true
                }
            )
        }

        // --- DIÁLOGO DE COMENTARIO (EXTERNO) ---
        if (showCommentDialog) {
            // Nota: He adaptado la llamada para que el diálogo externo funcione con "añadir" y "editar"
            AddCommentDialog(
                onDismiss = { showCommentDialog = false },
                onConfirm = { rating, text ->
                    if (editingComment == null) {
                        viewModel.addCommentToSelectedFountain(rating, text)
                    } else {
                        viewModel.editMyComment(editingComment!!.id, rating, text)
                    }
                    showCommentDialog = false
                }
            )
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
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                if (isHome && viewModel != null) setupHomeMap(viewModel)
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
                        DrawableCompat.setTint(w, fountain.getMarkerColor())
                        w
                    }
                    val bitmap = createBitmap(120, 120)
                    wrapped?.setBounds(0, 0, 120, 120)
                    wrapped?.draw(Canvas(bitmap))

                    marker.icon = bitmap.toDrawable(context.resources)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.setOnMarkerClickListener { _, _ ->
                        viewModel.selectFountain(fountain)
                        true
                    }
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

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
    addViewModel: AddFountainViewModel,
    isMapView: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FloatingActionButton(
            onClick = {
                val locationOverlay =
                    mapViewRef?.overlays?.find { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                locationOverlay?.myLocation?.let {
                    addViewModel.openAddFountain(it.latitude, it.longitude)
                }
            },
            containerColor = Blanco,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.add_24px),
                contentDescription = null,
                tint = Rojo
            )
        }

        if (isMapView) {
            FloatingActionButton(
                onClick = {
                    val locationOverlay =
                        mapViewRef?.overlays?.find { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                    locationOverlay?.myLocation?.let { mapViewRef?.controller?.animateTo(it) }
                },
                containerColor = Blanco,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_map_blue),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
fun MapLegend(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        color = Blanco.copy(alpha = 0.9f),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LegendItem(Verde, stringResource(R.string.legend_bebible))
            LegendItem(Blue10, stringResource(R.string.legend_ornamental))
            LegendItem(Rojo, stringResource(R.string.legend_averiada))
            LegendItem(Naranja, stringResource(R.string.legend_pendiente))
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) { drawCircle(color) }
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Negro)
    }
}