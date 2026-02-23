package cat.copernic.aguamap1.presentation.game.screens

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.presentation.game.components.GameMapView
import cat.copernic.aguamap1.presentation.home.map.OSMMapContent
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.BlancoClaro
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Rojo
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

@Composable
fun GameResultScreen(
    fountain: Fountain,
    score: Int,
    distance: Double,
    userGuessPos: GeoPoint?,
    hasLost: Boolean,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(0.45f)) {
            if (hasLost) {
                Box(modifier = Modifier.fillMaxSize()) {
                    OSMMapContent(viewModel = null, isHome = false) { map ->
                        map.controller.setZoom(17.0)
                        map.controller.setCenter(GeoPoint(fountain.latitude, fountain.longitude))

                        val sizeInPx = (35 * context.resources.displayMetrics.density).toInt()

                        val realMarker = Marker(map).apply {
                            position = GeoPoint(fountain.latitude, fountain.longitude)
                            title = "Ubicación Real"
                            val bitmap = ContextCompat.getDrawable(context, R.drawable.icon_pin)
                                ?.toBitmap()?.scale(sizeInPx, sizeInPx, false)
                            icon = BitmapDrawable(context.resources, bitmap)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        map.overlays.add(realMarker)
                        map.invalidate()
                    }
                }
            } else {
                GameMapView(
                    fountain = fountain,
                    userLocation = null,
                    isFinished = true,
                    userGuessPos = userGuessPos,
                    onMarkerPlaced = { _, _ -> },
                    distance = distance
                )
            }
        }

        Card(
            modifier = Modifier.weight(0.55f).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (hasLost) {
                        Icon(
                            painter = painterResource(R.drawable.error_24px),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Rojo
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.game_result_lost_title),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = Rojo
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.game_result_lost_message),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = BlancoClaro,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                stringResource(R.string.game_result_lost_points),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Rojo
                            )
                        }
                    } else {
                        Text(
                            stringResource(R.string.game_result_won_title),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.game_result_your_score),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            "$score ${stringResource(R.string.game_result_points)}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Blue10
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Blanco,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                stringResource(R.string.game_result_distance, distance),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Blue10
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.game_result_daily_message),
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}