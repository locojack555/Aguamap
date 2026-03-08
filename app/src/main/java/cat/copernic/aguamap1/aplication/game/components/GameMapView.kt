package cat.copernic.aguamap1.aplication.game.components

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
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.aplication.map.components.OSMMapContent
import cat.copernic.aguamap1.aplication.utils.MapUtils.createDistanceTag
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.max

/**
 * Vista de mapa especializada para el modo de juego "Adivina la ubicación".
 * Gestiona dos estados críticos:
 * 1. Selección: El usuario coloca un marcador en su posición estimada.
 * 2. Resultado: Muestra la ubicación real, la del usuario y la distancia entre ambas.
 *
 * @param fountain La fuente objetivo del juego.
 * @param userLocation Ubicación actual del dispositivo (para centrar inicialmente).
 * @param isFinished Define si el juego ha terminado y debe mostrarse la solución.
 * @param userGuessPos Coordenadas donde el usuario colocó su marcador.
 * @param onMarkerPlaced Callback que se dispara al colocar un marcador en el mapa.
 * @param distance Distancia calculada en metros entre los puntos.
 * @param onFountainClick Navegación al detalle de la fuente tras finalizar.
 */
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

    // Recursos de cadena para internacionalización
    val strYourGuess = stringResource(id = R.string.map_your_guess)
    val strRealLocation = stringResource(id = R.string.map_real_location)
    val strUnitMeters = stringResource(id = R.string.unit_meters)

    Box(Modifier.fillMaxSize()) {
        /**
         * Contenedor base de OpenStreetMap.
         */
        OSMMapContent(viewModel = null, isHome = false) { map ->
            mapViewRef = map
            if (!isFinished) {
                // Configuración inicial del mapa en modo "Jugando"
                val centerPoint = userLocation ?: GeoPoint(41.3851, 2.1734)
                map.controller.setZoom(17.0)
                map.controller.setCenter(centerPoint)

                /**
                 * Receptor de eventos táctiles para colocar el marcador de adivinanza.
                 */
                val eventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        if (isFinished) return false

                        // Eliminar marcador previo antes de poner el nuevo
                        map.overlays.removeAll { it is Marker && it.title == "user_guess" }

                        val sizeInPx = (35 * context.resources.displayMetrics.density).toInt()
                        val userMarker = Marker(map).apply {
                            position = p
                            title = "user_guess"
                            subDescription = strYourGuess

                            // Estilización del pin (Verde para el usuario)
                            val drawable =
                                ContextCompat.getDrawable(context, R.drawable.icon_pin)?.mutate()
                            drawable?.setTint(android.graphics.Color.parseColor("#34A853"))
                            val bitmap = drawable?.toBitmap()?.scale(sizeInPx, sizeInPx, false)
                            icon = BitmapDrawable(context.resources, bitmap)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }

                        map.overlays.add(userMarker)
                        onMarkerPlaced(p.latitude, p.longitude)
                        map.invalidate() // Refrescar vista
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint): Boolean = false
                }

                val mapEventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(eventsReceiver)
                map.overlays.add(0, mapEventsOverlay)
            }
        }

        /**
         * Lógica de visualización de resultados (isFinished == true).
         * Dibuja la línea entre puntos y ajusta el zoom automáticamente.
         */
        if (isFinished) {
            LaunchedEffect(userGuessPos, distance) {
                mapViewRef?.let { map ->
                    map.overlays.clear()

                    val sizeInPx = (35 * context.resources.displayMetrics.density).toInt()
                    val realPoint = GeoPoint(fountain.latitude, fountain.longitude)

                    // 1. Marcador Real (Rojo)
                    val realMarker = Marker(map).apply {
                        position = realPoint
                        title = fountain.name
                        subDescription = strRealLocation

                        val drawable =
                            ContextCompat.getDrawable(context, R.drawable.icon_pin)?.mutate()
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

                    // 2. Marcador de Adivinanza y Línea de Conexión
                    if (userGuessPos != null) {
                        val guessPoint = userGuessPos

                        val guessMarker = Marker(map).apply {
                            position = guessPoint
                            subDescription = strYourGuess

                            val drawable =
                                ContextCompat.getDrawable(context, R.drawable.icon_pin)?.mutate()
                            drawable?.setTint(android.graphics.Color.parseColor("#34A853"))
                            val bitmap = drawable?.toBitmap()?.scale(sizeInPx, sizeInPx, false)
                            icon = BitmapDrawable(context.resources, bitmap)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        map.overlays.add(guessMarker)

                        // Dibujar línea azul entre ambos puntos
                        val line = Polyline().apply {
                            outlinePaint.color = android.graphics.Color.BLUE
                            outlinePaint.strokeWidth = 5f
                            setPoints(listOf(realPoint, guessPoint))
                        }
                        map.overlays.add(line)

                        // 3. Etiqueta de Distancia en el punto medio
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

                        // 4. Ajuste dinámico del encuadre (Bounding Box)
                        try {
                            val minLat = minOf(realPoint.latitude, guessPoint.latitude)
                            val maxLat = maxOf(realPoint.latitude, guessPoint.latitude)
                            val minLon = minOf(realPoint.longitude, guessPoint.longitude)
                            val maxLon = maxOf(realPoint.longitude, guessPoint.longitude)

                            val zoomLevel = when {
                                max(maxLat - minLat, maxLon - minLon) < 0.005 -> 17.0
                                max(maxLat - minLat, maxLon - minLon) < 0.01 -> 16.0
                                else -> 14.0
                            }
                            map.controller.setZoom(zoomLevel)
                            map.controller.setCenter(
                                GeoPoint(
                                    (minLat + maxLat) / 2,
                                    (minLon + maxLon) / 2
                                )
                            )
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