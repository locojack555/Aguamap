package cat.copernic.aguamap1.aplication.fountain.detailFountain.components

import android.graphics.PorterDuff
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.aplication.fountain.detailFountain.DetailFountainViewModel
import cat.copernic.aguamap1.aplication.utils.getStatusColor
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.GrisClaro
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.NegroMinimal
import cat.copernic.aguamap1.ui.theme.NegroSuave

/**
 * Componente que muestra las especificaciones técnicas y la ubicación geográfica de la fuente.
 * Incluye una lista de atributos detallados y una previsualización de mapa estática (no interactiva).
 *
 * @param fountain Objeto de dominio con los datos de la fuente.
 * @param viewModel ViewModel de detalle para el formateo de datos (distancia, fechas, coordenadas).
 * @param isOperational Estado de funcionamiento actual para la lógica de colores y textos.
 */
@Composable
fun FountainSpecs(
    fountain: Fountain,
    viewModel: DetailFountainViewModel,
    isOperational: Boolean
) {
    // Obtenemos el color dinámico una sola vez para usarlo en la fila y en el mapa
    val statusColor = fountain.getStatusColor()

    Column {
        /**
         * Listado de información técnica formateada.
         */
        InfoRow(
            Icons.Default.LocationOn,
            stringResource(R.string.distancia_label),
            viewModel.getDistanceText(fountain.distanceFromUser),
            Blue10
        )
        InfoRow(
            Icons.Default.Build,
            stringResource(R.string.estado_label),
            if (isOperational) stringResource(R.string.funcionando) else stringResource(R.string.averiada),
            statusColor // Usamos el color dinámico aquí
        )
        InfoRow(
            Icons.Default.CalendarMonth,
            stringResource(R.string.fecha_alta_label),
            viewModel.getFormattedDate(fountain.dateCreated),
            NegroSuave
        )
        InfoRow(
            Icons.Default.Map,
            stringResource(R.string.coordenadas_label),
            viewModel.getFormattedCoordinates(fountain.latitude, fountain.longitude),
            NegroSuave
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.map),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Negro,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        /**
         * Contenedor de la vista de mapa previsualizada.
         */
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            color = GrisClaro,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, NegroMinimal)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        org.osmdroid.views.MapView(ctx).apply {
                            setMultiTouchControls(false)
                            setBuiltInZoomControls(false)
                            isClickable = false
                            isFocusable = false

                            controller.setZoom(17.5)
                            val startPoint =
                                org.osmdroid.util.GeoPoint(fountain.latitude, fountain.longitude)
                            controller.setCenter(startPoint)

                            val marker = org.osmdroid.views.overlay.Marker(this)
                            marker.position = startPoint
                            marker.setAnchor(0.5f, 0.5f)

                            // --- LÓGICA DE COLOR PARA EL PIN ---
                            val originalDrawable = ctx.getDrawable(R.drawable.pin_lleno)?.mutate()

                            // Aplicamos el tinte al drawable usando el color de la fuente
                            originalDrawable?.setColorFilter(
                                statusColor.toArgb(),
                                PorterDuff.Mode.SRC_IN
                            )

                            val smallIcon = originalDrawable?.let { drawable ->
                                // Si es un VectorDrawable, necesitamos dibujarlo en un Canvas para obtener el Bitmap
                                val bitmap = android.graphics.Bitmap.createBitmap(
                                    drawable.intrinsicWidth,
                                    drawable.intrinsicHeight,
                                    android.graphics.Bitmap.Config.ARGB_8888
                                )
                                val canvas = android.graphics.Canvas(bitmap)
                                drawable.setBounds(0, 0, canvas.width, canvas.height)
                                drawable.draw(canvas)

                                val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(
                                    bitmap,
                                    140,
                                    140,
                                    true
                                )
                                android.graphics.drawable.BitmapDrawable(
                                    ctx.resources,
                                    resizedBitmap
                                )
                            }

                            marker.icon = smallIcon ?: originalDrawable
                            marker.infoWindow = null

                            overlays.add(marker)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { map ->
                        val startPoint =
                            org.osmdroid.util.GeoPoint(fountain.latitude, fountain.longitude)
                        map.controller.setCenter(startPoint)
                    }
                )

                /**
                 * Escudo de transparencia para captura de eventos.
                 */
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent()
                                }
                            }
                        }
                )
            }
        }
    }
}