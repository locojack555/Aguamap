package cat.copernic.aguamap1.presentation.fountain.detailFountain.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.presentation.fountain.detailFountain.DetailFountainViewModel
import cat.copernic.aguamap1.ui.theme.*

@Composable
fun FountainSpecs(
    fountain: Fountain,
    viewModel: DetailFountainViewModel,
    isOperational: Boolean
) {
    Column {
        InfoRow(Icons.Default.LocationOn, stringResource(R.string.distancia_label), viewModel.getDistanceText(fountain.distanceFromUser), Blue10)
        InfoRow(Icons.Default.Build, stringResource(R.string.estado_label), if (isOperational) stringResource(R.string.funcionando) else stringResource(R.string.averiada), if (isOperational) Verde else Rojo)
        InfoRow(Icons.Default.CalendarMonth, stringResource(R.string.fecha_alta_label), viewModel.getFormattedDate(fountain.dateCreated), NegroSuave)
        InfoRow(Icons.Default.Map, stringResource(R.string.coordenadas_label), viewModel.getFormattedCoordinates(fountain.latitude, fountain.longitude), NegroSuave)

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.map),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Negro,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- CONTENEDOR DEL MAPA (CON BLOQUEO REAL) ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            color = GrisClaro,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, NegroMinimal)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. El Mapa Nativo con gestos desactivados
                AndroidView(
                    factory = { ctx ->
                        org.osmdroid.views.MapView(ctx).apply {
                            // DESACTIVAR TODO A NIVEL NATIVO
                            setMultiTouchControls(false)
                            setBuiltInZoomControls(false)
                            isClickable = false
                            isFocusable = false

                            // Bloquear desplazamiento y zoom nativo
                            controller.setZoom(17.5)
                            val startPoint = org.osmdroid.util.GeoPoint(fountain.latitude, fountain.longitude)
                            controller.setCenter(startPoint)

                            val marker = org.osmdroid.views.overlay.Marker(this)
                            marker.position = startPoint
                            marker.setAnchor(0.5f, 0.5f)

                            // Icono personalizado
                            val originalDrawable = ctx.getDrawable(R.drawable.pin_lleno)
                            val smallIcon = originalDrawable?.let { drawable ->
                                val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                                bitmap?.let { btm ->
                                    val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(btm, 140, 140, true)
                                    android.graphics.drawable.BitmapDrawable(ctx.resources, resizedBitmap)
                                }
                            }
                            marker.icon = smallIcon ?: ctx.getDrawable(R.drawable.pin_lleno)
                            marker.infoWindow = null

                            overlays.add(marker)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { map ->
                        val startPoint = org.osmdroid.util.GeoPoint(fountain.latitude, fountain.longitude)
                        map.controller.setCenter(startPoint)
                    }
                )

                // 2. EL ESCUDO (Invisible pero interceptor)
                // Ponemos un Box encima de todo el mapa que se "come" los toques
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .pointerInput(Unit) {
                            // Captura y consume todos los eventos táctiles para que no lleguen al AndroidView
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