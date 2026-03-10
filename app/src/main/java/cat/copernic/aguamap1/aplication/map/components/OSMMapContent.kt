package cat.copernic.aguamap1.aplication.map.components

import android.graphics.drawable.BitmapDrawable
import android.location.LocationManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.graphics.scale
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.category.Category
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.aplication.fountain.addFountain.AddFountainViewModel
import cat.copernic.aguamap1.aplication.map.mapView.MapViewModel
import cat.copernic.aguamap1.aplication.utils.getCategoryColor
import cat.copernic.aguamap1.aplication.utils.getMarkerColor
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Gris
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.Rojo
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * Componente principal que integra la vista de mapa de OpenStreetMap (OSMDroid) en Jetpack Compose.
 * Gestiona la renderización de marcadores de fuentes, la ubicación del usuario y la interacción táctil.
 */
@Composable
fun OSMMapContent(
    viewModel: MapViewModel?,
    isHome: Boolean,
    onFountainClick: ((Fountain) -> Unit)? = null,
    onMapLoad: (MapView) -> Unit
) {
    val context = LocalContext.current
    val fountains = viewModel?.uiState?.fountains ?: emptyList()

    val titleGuess = stringResource(R.string.map_marker_guess)
    val titleReal = stringResource(R.string.map_marker_real_location)

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    // Sincronización de la cámara con la ubicación inicial del usuario
    LaunchedEffect(viewModel?.userLat, viewModel?.userLng) {
        if (viewModel != null && viewModel.isFirstLocationUpdate && viewModel.isLocationAvailable) {
            mapViewRef?.let { map ->
                val userPoint = GeoPoint(viewModel.userLat!!, viewModel.userLng!!)
                map.controller.animateTo(userPoint)
                map.controller.zoomTo(17.0)
            }
        }
    }



    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

                if (isHome && viewModel != null) {
                    setupHomeMap(viewModel)
                }

                mapViewRef = this
                onMapLoad(this)
            }
        },
        update = { mapView ->
            // Limpiar marcadores antiguos (excepto marcadores especiales de juego)
            mapView.overlays.removeAll { it is Marker && it.title != titleGuess && it.title != titleReal }

            // Añadir marcadores para cada fuente en el estado actual
            fountains.forEach { fountain ->
                val marker = Marker(mapView)
                marker.position = GeoPoint(fountain.latitude, fountain.longitude)
                marker.title = fountain.name

                // Personalización del color del PIN según estado/categoría
                val drawable = ContextCompat.getDrawable(context, R.drawable.pin_lleno)
                val wrapped = drawable?.let {
                    val w = DrawableCompat.wrap(it).mutate()
                    DrawableCompat.setTint(w, fountain.getMarkerColor())
                    w
                }

                val bitmap = createBitmap(120, 120)
                wrapped?.setBounds(0, 0, 120, 120)
                wrapped?.draw(android.graphics.Canvas(bitmap))

                marker.icon = BitmapDrawable(context.resources, bitmap)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.setOnMarkerClickListener { _, _ ->
                    onFountainClick?.invoke(fountain)
                    true
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate() // Forzar repintado
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Configuración específica para el mapa principal (Home).
 * Activa el overlay de ubicación en tiempo real y los listeners de movimiento.
 */
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
        enableFollowLocation()
        isDrawAccuracyEnabled = false

        // --- CAMBIO PARA EL ANCLAJE (ANCHOR) ---
        // Esto asegura que el centro del icono sea el punto exacto del GPS.
        // Sin esto, al hacer zoom out, el icono se desplaza visualmente.
        setPersonHotspot(sizeInPx / 2f, sizeInPx / 2f)
        //setDirectionHotspot(sizeInPx / 2f, sizeInPx / 2f)

        customIcon?.let {
            setPersonIcon(it)
            setDirectionIcon(it)
        }
    }

    // Posicionamiento inicial
    controller.setZoom(viewModel.zoomLevel)
    controller.setCenter(GeoPoint(viewModel.latitude, viewModel.longitude))

    locationOverlay.runOnFirstFix {
        val myLoc = locationOverlay.myLocation
        if (myLoc != null) {
            post {
                viewModel.onFirstLocationFound(myLoc.latitude, myLoc.longitude)
                if (viewModel.isFirstLocationUpdate) {
                    controller.animateTo(myLoc)
                    controller.setZoom(17.0)
                }
            }
        }
    }
    overlays.add(locationOverlay)

    // Sincronizar estado del ViewModel con el movimiento del mapa
    addMapListener(object : MapListener {
        override fun onScroll(event: ScrollEvent?): Boolean {
            viewModel.onMapMoved(mapCenter.latitude, mapCenter.longitude, zoomLevelDouble)
            return true
        }

        override fun onZoom(event: ZoomEvent?): Boolean {
            viewModel.onMapMoved(mapCenter.latitude, mapCenter.longitude, zoomLevelDouble)
            return true
        }
    })
}

/**
 * Botonera flotante para acciones rápidas (Añadir fuente y Centrar ubicación).
 */
@Composable
fun MapFloatingButtons(
    mapViewRef: MapView?,
    addViewModel: AddFountainViewModel,
    mapViewModel: MapViewModel,
    isMapView: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Botón Añadir Fuente
        FloatingActionButton(
            onClick = {
                if (mapViewModel.isLocationAvailable) {
                    addViewModel.openAddFountain(
                        mapViewModel.userLat!!,
                        mapViewModel.userLng!!
                    )
                }
            },
            containerColor = if (mapViewModel.isLocationAvailable) Blanco else Gris.copy(alpha = 0.5f),
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.add_24px),
                contentDescription = stringResource(R.string.map_desc_add_fountain),
                tint = if (mapViewModel.isLocationAvailable) Rojo else Negro.copy(alpha = 0.3f)
            )
        }

        // Botón Centrar Ubicación (Solo visible en modo Mapa)
        if (isMapView) {
            FloatingActionButton(
                onClick = {
                    val locationOverlay =
                        mapViewRef?.overlays?.find { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                    locationOverlay?.myLocation?.let {
                        mapViewRef.controller.animateTo(it)
                        mapViewRef.controller.setZoom(17.0)
                    }
                },
                containerColor = Blanco,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_map_blue),
                    contentDescription = stringResource(R.string.map_desc_center_location),
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

/**
 * Leyenda visual para ayudar al usuario a identificar los estados y categorías por color.
 */
@Composable
fun MapLegend(
    categories: List<Category>,
    modifier: Modifier = Modifier
) {
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
            // Estados fijos
            LegendItem(color = Naranja, text = stringResource(R.string.legend_pendiente))
            LegendItem(color = Rojo, text = stringResource(R.string.legend_averiada))

            // Categorías dinámicas mapeadas a sus colores correspondientes
            categories.forEach { category ->
                LegendItem(
                    color = getCategoryColor(category.name, category.id),
                    text = category.name
                )
            }
        }
    }
}

/**
 * Representación individual de un elemento en la leyenda.
 */
@Composable
fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Negro
        )
    }
}