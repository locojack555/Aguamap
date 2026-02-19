package cat.copernic.aguamap1.presentation.ranking

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.UserRanking

@Composable
fun RankingScreen(viewModel: RankingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    // Estado del selector
    var seleccionadoResId by remember { mutableIntStateOf(R.string.ranking_day) }

    // Disparador de carga: Cada vez que 'seleccionadoResId' cambie
    LaunchedEffect(seleccionadoResId) {
        viewModel.loadRanking(seleccionadoResId)  // Cambiado a loadRanking
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.blancoHueso))
    ) {
        HeaderSection()

        TimeSelectorSection(
            seleccionadoResId = seleccionadoResId,
            onSeleccionChange = { seleccionadoResId = it }
        )

        // Título dinámico
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

        // Gestión de estados: Carga vs. Lista
        Box(modifier = Modifier.weight(1f)) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorResource(id = R.color.magentaOscuro)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(state.players) { player ->
                        RankingItem(player)
                    }
                }
            }
        }

        // Puntos totales del usuario actual
        val puntosMios = state.players.find { it.isCurrentUser }?.points ?: 0
        TotalPointsFooter(points = puntosMios)
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
                    colors = listOf(
                        colorResource(id = R.color.amatista),
                        colorResource(id = R.color.rosaIntenso)
                    )
                )
            )
            .padding(22.dp),
        contentAlignment = Alignment.BottomStart
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
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(id = R.string.ranking_subtitle),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun TimeSelectorSection(
    seleccionadoResId: Int,
    onSeleccionChange: (Int) -> Unit
) {
    val opciones = listOf(
        R.string.ranking_day,
        R.string.ranking_month,
        R.string.ranking_year
    )

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
                modifier = Modifier
                    .weight(1f)
                    .height(35.dp),
                shadowElevation = if (esSeleccionado) 2.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (player.isCurrentUser)
                colorResource(id = R.color.cremaSuave)
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición e Icono (Medalla o número)
            Box(
                modifier = Modifier.width(40.dp),
                contentAlignment = Alignment.Center
            ) {
                when (player.position) {
                    1 -> Icon(
                        painter = painterResource(id = R.drawable.icon_corona),
                        contentDescription = stringResource(id = R.string.ranking_gold),
                        tint = colorResource(id = R.color.oro)
                    )
                    2 -> Icon(
                        painter = painterResource(id = R.drawable.icon_medalla),
                        contentDescription = stringResource(id = R.string.ranking_silver),
                        tint = colorResource(id = R.color.plata)
                    )
                    3 -> Icon(
                        painter = painterResource(id = R.drawable.icon_medalla),
                        contentDescription = stringResource(id = R.string.ranking_bronze),
                        tint = colorResource(id = R.color.bronze)
                    )
                    else -> Text(
                        text = "${player.position}",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Avatar
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.azulHielo)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = colorResource(id = R.color.azulReal)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    fontWeight = FontWeight.Bold,
                    color = if (player.isCurrentUser)
                        colorResource(id = R.color.azulReal)
                    else
                        Color.Black
                )
                Text(
                    text = stringResource(
                        id = R.string.ranking_stats,
                        player.discovered,
                        player.games
                    ),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Puntos con fondo de color
            Surface(
                color = when(player.position) {
                    1 -> colorResource(id = R.color.oro)
                    2 -> colorResource(id = R.color.plata)
                    3 -> colorResource(id = R.color.bronze)
                    else -> colorResource(id = R.color.grisHumo)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.lavandaPalido)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
                Text(
                    text = stringResource(id = R.string.ranking_total_points),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "$points",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.purpuraReal)
                )
            }
        }
    }
}