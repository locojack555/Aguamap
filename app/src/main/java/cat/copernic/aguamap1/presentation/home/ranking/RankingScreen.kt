package cat.copernic.aguamap1.presentation.home.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.copernic.aguamap1.R

@Composable
fun RankingScreen(viewModel: RankingViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    // 1. Elevamos el estado aquí para que toda la pantalla lo vea
    var seleccionadoResId by remember { mutableIntStateOf(R.string.ranking_month) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        HeaderSection()

        // Pasamos el ID seleccionado
        TimeSelectorSection(
            seleccionadoResId = seleccionadoResId,
            onSeleccionChange = { seleccionadoResId = it }
        )

        // El texto de clasificación ahora cambia según el ID seleccionado
        val titleRes = when (seleccionadoResId) {
            R.string.ranking_day -> R.string.ranking_title_day
            R.string.ranking_month -> R.string.ranking_title_month
            else -> R.string.ranking_title_year
        }

        Text(
            text = stringResource(id = titleRes),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            fontSize = 14.sp,
            color = Color.Gray
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(state.players) { player ->
                RankingItem(player)
            }
        }

        TotalPointsFooter(points = 2500)
    }
}

@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF8E24AA), Color(0xFFE91E63))
                )
            )
            .padding(22.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = R.drawable.icon_trofeo), contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ranking", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            Text("Mejores jugadores", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
        }
    }
}

@Composable
fun TimeSelectorSection(
    seleccionadoResId: Int,
    onSeleccionChange: (Int) -> Unit
) {
    // Lista de IDs de strings.xml
    val opciones = listOf(
        R.string.ranking_day,
        R.string.ranking_month,
        R.string.ranking_year
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFFF1F1F1), RoundedCornerShape(50.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        opciones.forEach { resId ->
            val esSeleccionado = seleccionadoResId == resId

            Surface(
                onClick = { onSeleccionChange(resId) },
                shape = RoundedCornerShape(50.dp),
                color = if (esSeleccionado) Color.White else Color.Transparent,
                modifier = Modifier.weight(1f).height(35.dp),
                shadowElevation = if (esSeleccionado) 2.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        // Aquí obtenemos el texto traducido según el ID
                        text = stringResource(id = resId),
                        fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal,
                        color = if (esSeleccionado) Color.Black else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun RankingItem(player: UserRanking) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = if (player.isCurrentUser) Color(0xFFFFFBE6) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición e Icono (Medalla o número)
            Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                when (player.position) {
                    1 -> Icon(painter = painterResource(id = R.drawable.icon_corona), "Oro", tint = Color(0xFFFFC107))
                    2 -> Icon(painter = painterResource(id = R.drawable.icon_medalla), "Plata", tint = Color(0xFF9E9E9E))
                    3 -> Icon(painter = painterResource(id = R.drawable.icon_medalla), "Bronce", tint = Color(0xFFCD7F32))
                    else -> Text("${player.position}", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }

            // Avatar
            Box(
                modifier = Modifier.size(45.dp).clip(CircleShape).background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1976D2))
            }

            Spacer(Modifier.width(12.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    fontWeight = FontWeight.Bold,
                    color = if (player.isCurrentUser) Color(0xFF1976D2) else Color.Black
                )
                Text(
                    text = "${player.discovered} descubiertas    ${player.games} partidas",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Puntos con fondo de color
            Surface(
                color = when(player.position) {
                    1 -> Color(0xFFFFC107)
                    2 -> Color(0xFF9E9E9E)
                    3 -> Color(0xFFCD7F32)
                    else -> Color(0xFFE0E0E0)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${player.points}",
                    color = if (player.position <= 3) Color.White else Color.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun TotalPointsFooter(points: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F0FF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(id = R.drawable.icon_trofeo), contentDescription = null, tint = Color(0xFF9C27B0), modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Tus puntos totales", fontSize = 14.sp, color = Color.Gray)
                Text("$points", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9C27B0))
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun RankingPreview() {
    RankingScreen()
}