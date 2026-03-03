package cat.copernic.aguamap1.presentation.game.components

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // <--- IMPORTANTE PARA MULTIIDIOMA
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import cat.copernic.aguamap1.R // <--- ASEGÚRATE DE QUE ESTO NO ESTÉ EN ROJO
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.presentation.maps.components.OSMMapContent
import cat.copernic.aguamap1.presentation.util.MapUtils.createDistanceTag
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.max

@Composable
fun GameMapView(
    fountain: Fountain,
    userLocation: GeoPoint?,
    isFinished: Boolean,
    userGuessPos: GeoPoint? = null,
    onMarkerPlaced: (Double, Double) -> Unit,
    distance: Double = 0.0,
    onFountainClick: (Fountain) -> Unit = {}
) {
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val context = LocalContext.current

    // Usar stringResource es la forma preferida en Compose para evitar problemas de contexto
    val strYourGuess = stringResource(id = R.string.map_your_guess)
    val strRealLocation = stringResource(id = R.string.map_real_location)
    val strUnitMeters = stringResource(id = R.string.unit_meters)

    Box(Modifier.fillMaxSize()) {
        OSMMapContent(viewModel = null, isHome = false) { map ->
            mapViewRef = map
            if (!isFinished) {
                // Centrar mapa
                val centerPoint = userLocation ?: GeoPoint(41.3851, 2.1734)
                map.controller.setZoom(17.0)
                map.controller.setCenter(centerPoint)

                val eventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        if (isFinished) return false
                        map.overlays.removeAll { it is Marker && it.title == "user_guess" }

                        val sizeInPx = (35 * context.resources.displayMetrics.density).toInt()
                        val userMarker = Marker(map).apply {
                            position = p
                            title = "user_guess"
                            subDescription = strYourGuess

                            val drawable = ContextCompat.getDrawable(context, R.drawable.icon_pin)?.mutate()
                            drawable?.setTint(android.graphics.Color.parseColor("#34A853"))
                            val bitmap = drawable?.toBitmap()?.scale(sizeInPx, sizeInPx, false)
                            icon = BitmapDrawable(context.resources, bitmap)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }

                        map.overlays.add(userMarker)
                        onMarkerPlaced(p.latitude, p.longitude)
                        map.invalidate()
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint): Boolean = false
                }
                val mapEventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(eventsReceiver)
                map.overlays.add(0, mapEventsOverlay)
            }
        }

        if (isFinished) {
            LaunchedEffect(userGuessPos, distance) {
                mapViewRef?.let { map ->
                    map.overlays.clear()

                    val sizeInPx = (35 * context.resources.displayMetrics.density).toInt()
                    val realPoint = GeoPoint(fountain.latitude, fountain.longitude)

                    val realMarker = Marker(map).apply {
                        position = realPoint
                        title = fountain.name
                        subDescription = strRealLocation

                        val drawable = ContextCompat.getDrawable(context, R.drawable.icon_pin)?.mutate()
                        drawable?.setTint(android.graphics.Color.parseColor("#FF4444"))
                        val bitmap = drawable?.toBitmap()?.scale(sizeInPx, sizeInPx, false)
                        icon = BitmapDrawable(context.resources, bitmap)

                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            onFountainClick(fountain)
                            true
                        }
                    }
                    map.overlays.add(realMarker)

                    if (userGuessPos != null) {
                        val guessPoint = userGuessPos

                        val guessMarker = Marker(map).apply {
                            position = guessPoint
                            subDescription = strYourGuess

                            val drawable = ContextCompat.getDrawable(context, R.drawable.icon_pin)?.mutate()
                            drawable?.setTint(android.graphics.Color.parseColor("#34A853"))
                            val bitmap = drawable?.toBitmap()?.scale(sizeInPx, sizeInPx, false)
                            icon = BitmapDrawable(context.resources, bitmap)

                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        map.overlays.add(guessMarker)

                        val line = Polyline().apply {
                            outlinePaint.color = android.graphics.Color.BLUE
                            outlinePaint.strokeWidth = 5f
                            setPoints(listOf(realPoint, guessPoint))
                        }
                        map.overlays.add(line)

                        val midPoint = GeoPoint(
                            (realPoint.latitude + guessPoint.latitude) / 2,
                            (realPoint.longitude + guessPoint.longitude) / 2
                        )

                        val formattedDistance = if (distance >= 1000) {
                            String.format("%.1f km", distance / 1000.0)
                        } else {
                            "${distance.toInt()} $strUnitMeters"
                        }

                        val distanceMarker = Marker(map).apply {
                            position = midPoint
                            icon = createDistanceTag(context, formattedDistance)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            setInfoWindow(null)
                        }
                        map.overlays.add(distanceMarker)

                        // Lógica de Zoom
                        try {
                            val minLat = minOf(realPoint.latitude, guessPoint.latitude)
                            val maxLat = maxOf(realPoint.latitude, guessPoint.latitude)
                            val minLon = minOf(realPoint.longitude, guessPoint.longitude)
                            val maxLon = maxOf(realPoint.longitude, guessPoint.longitude)
                            val centerLat = (minLat + maxLat) / 2
                            val centerLon = (minLon + maxLon) / 2

                            val zoomLevel = when {
                                max(maxLat - minLat, maxLon - minLon) < 0.005 -> 17.0
                                max(maxLat - minLat, maxLon - minLon) < 0.01 -> 16.0
                                else -> 14.0
                            }
                            map.controller.setZoom(zoomLevel)
                            map.controller.setCenter(GeoPoint(centerLat, centerLon))
                        } catch (e: Exception) {
                            map.controller.setCenter(realPoint)
                        }
                    } else {
                        map.controller.setZoom(17.0)
                        map.controller.setCenter(realPoint)
                    }
                    map.invalidate()
                }
            }
        }
    }
}