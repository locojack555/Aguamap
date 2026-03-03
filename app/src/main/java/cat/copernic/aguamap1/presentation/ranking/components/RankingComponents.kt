package cat.copernic.aguamap1.presentation.ranking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.UserRanking

@Composable
fun RankingHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colorResource(id = R.color.amatista),
                        colorResource(id = R.color.rosaIntenso)
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_trofeo),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.ranking_title),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(id = R.string.ranking_subtitle),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TimeSelectorSection(
    seleccionadoResId: Int,
    onSeleccionChange: (Int) -> Unit
) {
    val opciones = listOf(R.string.ranking_day, R.string.ranking_month, R.string.ranking_year)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(colorResource(id = R.color.blancoHumo), RoundedCornerShape(50.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        opciones.forEach { resId ->
            val esSeleccionado = seleccionadoResId == resId
            Surface(
                onClick = { onSeleccionChange(resId) },
                shape = RoundedCornerShape(50.dp),
                color = if (esSeleccionado) Color.White else Color.Transparent,
                modifier = Modifier.weight(1f).height(36.dp),
                shadowElevation = if (esSeleccionado) 2.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(id = resId),
                        fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal,
                        color = if (esSeleccionado) Color.Black else Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RankingItem(player: UserRanking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (player.isCurrentUser) colorResource(id = R.color.cremaSuave) else Color.White
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medallas o Posición
            Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                when (player.position) {
                    1 -> Icon(painterResource(R.drawable.icon_corona), null, tint = colorResource(R.color.oro))
                    2 -> Icon(painterResource(R.drawable.icon_medalla), null, tint = colorResource(R.color.plata))
                    3 -> Icon(painterResource(R.drawable.icon_medalla), null, tint = colorResource(R.color.bronze))
                    else -> Text("${player.position}", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }

            // Avatar
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(colorResource(R.color.azulHielo)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = colorResource(R.color.azulReal))
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    fontWeight = FontWeight.Bold,
                    color = if (player.isCurrentUser) colorResource(R.color.azulReal) else Color.Black
                )
                Text(
                    text = stringResource(R.string.ranking_stats, player.discovered, player.games),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Puntos
            Surface(
                color = when(player.position) {
                    1 -> colorResource(R.color.oro)
                    2 -> colorResource(R.color.plata)
                    3 -> colorResource(R.color.bronze)
                    else -> colorResource(R.color.grisHumo)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${player.points}",
                    color = if (player.position <= 3) Color.White else Color.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun TotalPointsFooter(points: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colorResource(id = R.color.lavandaPalido),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp).navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_trofeo),
                contentDescription = null,
                tint = colorResource(id = R.color.purpuraReal),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(stringResource(R.string.ranking_total_points), fontSize = 12.sp, color = Color.Gray)
                Text("$points", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.purpuraReal))
            }
        }
    }
}