package cat.copernic.aguamap1.presentation.reusable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.Blanco

@Composable
fun AguaMapHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 160.dp,
    innerSpacing: Dp = 0.dp,
    showSubtitle: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.gota),
            contentDescription = "Logo",
            modifier = Modifier.size(logoSize)
        )
        Spacer(modifier = Modifier.size(innerSpacing))
        Text(
            text = "AguaMap",
            fontSize = 50.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Blanco
        )
        if (showSubtitle) {
            Text(
                text = "Encuentra fuentes en Terrassa",
                fontSize = 20.sp,
                color = Blanco,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}