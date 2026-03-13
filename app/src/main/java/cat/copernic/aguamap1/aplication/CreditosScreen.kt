package cat.copernic.aguamap1.aplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.aplication.commons.AguaMapHeader
import cat.copernic.aguamap1.aplication.commons.AguaMapInput
import cat.copernic.aguamap1.ui.theme.AguaMapGradient
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.PurpleGrey40
import cat.copernic.aguamap1.ui.theme.Rojo

@Composable
fun CreditosScreen(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AguaMapGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.gota),
                contentDescription = stringResource(R.string.logo),
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = stringResource(R.string.creditos),
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(56.dp))

            Text(
                text = "Jack Arévalo",
                fontSize = 23.sp,
                color = colorResource(R.color.blancoHueso),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cristina Jimenez",
                fontSize = 23.sp,
                color = colorResource(R.color.blancoHueso),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Luis Mariño",
                fontSize = 23.sp,
                color = colorResource(R.color.blancoHueso),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Adrià Gonzalez",
                fontSize = 23.sp,
                color = colorResource(R.color.blancoHueso),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(300.dp))

            Text(
                text = "13/03/2026",
                fontSize = 20.sp,
                color = colorResource(R.color.blancoHueso),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}