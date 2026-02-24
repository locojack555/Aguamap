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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import cat.copernic.aguamap1.R
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
    onFountainClick: (Fountain) -> Unit = {} // Callback para cuando se hace clic en la fuente
) {
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val context = LocalContext.current

    Box(Modifier.fillMaxSize()) {
        OSMMapContent(viewModel = null, isHome = false) { map ->
            mapViewRef = map
            if (!isFinished) {
                // Modo juego: centrar en ubicación del usuario
                if (userLocation != null) {
                    map.controller.setZoom(17.0)
                    map.controller.setCenter(userLocation)
                } else {
                    map.controller.setZoom(17.0)
                    map.controller.setCenter(GeoPoint(41.3851, 2.1734)) // Fallback Barcelona
                }

                // Configurar listener para taps en el mapa
                val eventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        if (isFinished) return false
                        map.overlays.removeAll { it is Marker && it.title == "user_guess" }

                        val sizeInPx = (35 * context.resources.displayMetrics.density).toInt()
                        val userMarker = Marker(map).apply {
                            position = p
                            title = "user_guess" // Título interno, no se muestra
                            val drawable = ContextCompat.getDrawable(context, R.drawable.icon_pin)?.mutate()
                            drawable?.setTint(android.graphics.Color.parseColor("#34A853")) // Verde
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

        // Mostrar resultado cuando el juego ha terminado (tanto si hay guess como si no)
        if (isFinished) {
            LaunchedEffect(userGuessPos) {
                mapViewRef?.let { map ->
                    map.overlays.clear()

                    val sizeInPx = (35 * context.resources.displayMetrics.density).toInt()
                    val realPoint = GeoPoint(fountain.latitude, fountain.longitude)
                    val realDrawable =
                        ContextCompat.getDrawable(context, R.drawable.icon_pin)?.mutate()
                    realDrawable?.setTint(android.graphics.Color.parseColor("#FF4444"))
                    val realBitmap = realDrawable?.toBitmap()?.scale(sizeInPx, sizeInPx, false)

                    val realMarker = Marker(map).apply {
                        position = realPoint
                        icon = BitmapDrawable(context.resources, realBitmap)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        // Al hacer clic en el marcador rojo, abre los detalles de la fuente
                        setOnMarkerClickListener { _, _ ->
                            onFountainClick(fountain)
                            true
                        }
                    }
                    map.overlays.add(realMarker)

                    // --- MARCADOR VERDE, LÍNEA Y DISTANCIA (SOLO SI HAY GUESS) ---
                    if (userGuessPos != null) {
                        val guessPoint = userGuessPos

                        // Marcador de apuesta del usuario (verde)
                        val guessDrawable =
                            ContextCompat.getDrawable(context, R.drawable.icon_pin)?.mutate()
                        guessDrawable?.setTint(android.graphics.Color.parseColor("#34A853"))
                        val guessBitmap = guessDrawable?.toBitmap()?.scale(sizeInPx, sizeInPx, false)

                        val guessMarker = Marker(map).apply {
                            position = guessPoint
                            icon = BitmapDrawable(context.resources, guessBitmap)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            // SIN OnMarkerClickListener - no abre detalles
                        }
                        map.overlays.add(guessMarker)

                        // Línea entre los dos puntos
                        val line = Polyline().apply {
                            outlinePaint.color = android.graphics.Color.BLUE
                            outlinePaint.strokeWidth = 5f
                            setPoints(listOf(realPoint, guessPoint))
                        }
                        map.overlays.add(line)

                        // Marcador de distancia
                        val midPoint = GeoPoint(
                            (realPoint.latitude + guessPoint.latitude) / 2,
                            (realPoint.longitude + guessPoint.longitude) / 2
                        )
                        val distanceMarker = Marker(map).apply {
                            position = midPoint
                            icon = createDistanceTag(context, "${distance.toInt()}m")
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            setInfoWindow(null)
                        }
                        map.overlays.add(distanceMarker)

                        // Calcular zoom para ver ambos puntos
                        try {
                            val minLat = minOf(realPoint.latitude, guessPoint.latitude)
                            val maxLat = maxOf(realPoint.latitude, guessPoint.latitude)
                            val minLon = minOf(realPoint.longitude, guessPoint.longitude)
                            val maxLon = maxOf(realPoint.longitude, guessPoint.longitude)

                            val centerLat = (minLat + maxLat) / 2
                            val centerLon = (minLon + maxLon) / 2

                            val latSpan = maxLat - minLat
                            val lonSpan = maxLon - minLon

                            val zoomLevel = when {
                                max(latSpan, lonSpan) < 0.005 -> 17.0
                                max(latSpan, lonSpan) < 0.01 -> 16.0
                                max(latSpan, lonSpan) < 0.02 -> 15.0
                                max(latSpan, lonSpan) < 0.05 -> 14.0
                                max(latSpan, lonSpan) < 0.1 -> 13.0
                                else -> 12.0
                            }

                            map.controller.setZoom(zoomLevel)
                            map.controller.setCenter(GeoPoint(centerLat, centerLon))
                        } catch (e: Exception) {
                            map.controller.setZoom(15.0)
                            map.controller.setCenter(realPoint)
                        }
                    } else {
                        // Si no hay userGuessPos (cuando pierde), centramos solo en la fuente
                        map.controller.setZoom(17.0)
                        map.controller.setCenter(realPoint)
                    }

                    map.invalidate()
                }
            }
        }
    }
}