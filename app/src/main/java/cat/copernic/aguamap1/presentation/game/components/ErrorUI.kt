package cat.copernic.aguamap1.presentation.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.Rojo

@Composable
fun ErrorScreen(message: String, onRetry: (() -> Unit)?, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        Icon(
            painterResource(R.drawable.error_24px),
            null,
            Modifier.size(64.dp),
            tint = Rojo
        )
        Spacer(Modifier.height(16.dp))
        Text(message, textAlign = TextAlign.Center, fontSize = 18.sp)
        Spacer(Modifier.height(24.dp))
        onRetry?.let {
            Button(onClick = it, Modifier.fillMaxWidth().height(50.dp)) {
                Text(stringResource(R.string.game_error_retry_button))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}