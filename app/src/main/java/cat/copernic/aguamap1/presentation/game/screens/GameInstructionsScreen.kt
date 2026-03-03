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
        // --- CABECERA ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.20f) // Ajustado un poco para que respire más la cabecera
                .background(AzulGradient)
                .padding(24.dp)
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
                        text = stringResource(R.string.game_instructions_title_app),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blanco
                    )
                }
                Text(
                    text = stringResource(R.string.game_instructions_subtitle),
                    color = Blanco.copy(alpha = 0.9f),
                    fontSize = 18.sp
                )
            }
        }

        // --- CUERPO CENTRAL ---
        Column(
            modifier = Modifier
                .weight(0.60f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono de Trofeo con Gradiente
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(AzulOscuro, AzulTurquesa)
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

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.game_instructions_play_learn_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AzulGrisaceo,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.game_instructions_play_learn_description),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 12.dp),
                color = Gris,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(16.dp))

            // Caja de reglas
            Surface(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .widthIn(max = 300.dp),
                color = AzulClaro,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• " + stringResource(R.string.game_instructions_rule_one),
                        fontSize = 14.sp,
                        color = GrisOscuro
                    )
                    Text(
                        text = "• " + stringResource(R.string.game_instructions_rule_two),
                        fontSize = 14.sp,
                        color = GrisOscuro
                    )
                    Text(
                        text = "• " + stringResource(R.string.game_instructions_rule_three),
                        fontSize = 14.sp,
                        color = GrisOscuro
                    )
                }
            }
        }

        // --- BOTÓN INFERIOR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(AzulOscuro, AzulTurquesa)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Text(
                    text = stringResource(R.string.game_instructions_start_button),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blanco
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}