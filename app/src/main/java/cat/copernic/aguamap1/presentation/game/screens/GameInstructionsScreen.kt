package cat.copernic.aguamap1.presentation.game.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.*

@Composable
fun GameInstructionsScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Blanco)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.16f)
                .background(AguaMapGradient)
                .padding(18.dp)
        ) {
            Column(Modifier.align(Alignment.CenterStart)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.map_24px),
                        contentDescription = null,
                        tint = Blanco,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.game_instructions_title_app),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blanco
                    )
                }
                Text(
                    stringResource(R.string.game_instructions_subtitle),
                    color = Blanco.copy(alpha = 0.9f),
                    fontSize = 18.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    AzulOscuro,
                                    AzulTurquesa
                                )
                            )
                        )
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_trophy),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = Blanco
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(R.string.game_instructions_play_learn_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AzulGrisaceo
            )

            Text(
                stringResource(R.string.game_instructions_play_learn_description),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp),
                color = Gris,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.padding(horizontal = 32.dp).widthIn(max = 280.dp),
                color = AzulClaro,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.game_instructions_rule_one),
                        fontSize = 14.sp,
                        color = GrisOscuro
                    )
                    Text(
                        stringResource(R.string.game_instructions_rule_two),
                        fontSize = 14.sp,
                        color = GrisOscuro
                    )
                    Text(
                        stringResource(R.string.game_instructions_rule_three),
                        fontSize = 14.sp,
                        color = GrisOscuro
                    )
                }
            }
        }

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            Text(
                stringResource(R.string.game_instructions_start_button),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Blanco,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AzulOscuro,
                                AzulTurquesa
                            )
                        ),
                        shape = RoundedCornerShape(5.dp)
                    )
                    .wrapContentSize(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.weight(0.05f))
    }
}