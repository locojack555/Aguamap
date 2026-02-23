package cat.copernic.aguamap1.presentation.game.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.presentation.game.components.GameMapView
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Rojo
import cat.copernic.aguamap1.ui.theme.VerdeClaro
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
            color = Color.White.copy(alpha = 0.9f),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp
        ) {
            Row(
                Modifier.padding(12.dp),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Text(
                    text = "${remainingTime}s",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (remainingTime < 10) Rojo else Color.Black
                )
                Text(
                    stringResource(R.string.game_instructions_title_app),
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Icon(
                    painterResource(R.drawable.timer_24px),
                    null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
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
                colors = ButtonDefaults.buttonColors(containerColor = VerdeClaro)
            ) {
                Text(
                    stringResource(R.string.game_play_confirm_button),
                    color = Blanco,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
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
                    stringResource(R.string.game_play_hint_text),
                    color = Blanco,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}