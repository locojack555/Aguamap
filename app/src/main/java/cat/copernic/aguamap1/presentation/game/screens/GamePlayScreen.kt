package cat.copernic.aguamap1.presentation.game.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.presentation.game.components.GameMapView
import cat.copernic.aguamap1.presentation.util.VerdeHoja
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Rojo
import cat.copernic.aguamap1.ui.theme.VerdeClaro
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.osmdroid.util.GeoPoint

@Composable
fun GamePlayScreen(
    fountain: Fountain,
    remainingTime: Int,
    userLocation: GeoPoint?,
    onGuess: (Double, Double) -> Unit,
    onFinish: () -> Unit
) {
    var hasPlacedPin by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {

        GameMapView(
            fountain = fountain,
            userLocation = userLocation,
            isFinished = false,
            onMarkerPlaced = { lat, lng ->
                hasPlacedPin = true
                onGuess(lat, lng)
            }
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            color = Color.White.copy(alpha = 0.95f),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    // Texto del tiempo con la unidad localizada (ej: 30s)
                    Text(
                        text = "${remainingTime}${stringResource(R.string.unit_seconds_short)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingTime < 10) Rojo else Color.Black
                    )
                    Text(
                        text = stringResource(R.string.game_instructions_title_app),
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Icon(
                        painter = painterResource(R.drawable.timer_24px),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                if (fountain.imageUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.game_play_question), // Corregido a multiidioma
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(fountain.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = fountain.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        color = Color.LightGray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.pin_lleno),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = fountain.name,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (hasPlacedPin) {
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .fillMaxWidth(0.8f)
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeHoja)
            ) {
                Text(
                    text = stringResource(R.string.game_play_confirm_button),
                    color = Blanco,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        } else {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.game_play_hint_text),
                    color = Blanco,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    fontSize = 16.sp
                )
            }
        }
    }
}