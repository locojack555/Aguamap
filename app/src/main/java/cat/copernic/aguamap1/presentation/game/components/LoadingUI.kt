package cat.copernic.aguamap1.presentation.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.aguamap1.R

@Composable
fun LoadingPartida() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF007BFF))
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.game_loading_message),
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }
    }
}