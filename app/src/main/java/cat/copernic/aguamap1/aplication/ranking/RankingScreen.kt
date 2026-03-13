package cat.copernic.aguamap1.aplication.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.aplication.ranking.components.RankingHeader
import cat.copernic.aguamap1.aplication.ranking.components.RankingItem
import cat.copernic.aguamap1.aplication.ranking.components.TimeSelectorSection
import cat.copernic.aguamap1.aplication.ranking.components.TotalPointsFooter

/**
 * Pantalla de Ranking de AguaMap.
 * Permite visualizar la clasificación de usuarios por puntos en diferentes intervalos
 * de tiempo (Día, Mes, Año).
 */
@Composable
//para lo del objeto userranking
fun RankingScreen(viewModel: RankingViewModel = hiltViewModel()/*,onPlayerClick: (UserRanking) -> Unit = {}*/) {
    // Suscripción al estado del ViewModel con gestión de ciclo de vida
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Estado local para controlar el filtro de tiempo seleccionado
    var seleccionadoResId by remember { mutableIntStateOf(R.string.ranking_day) }

    // Disparamos la carga de datos cada vez que cambia el filtro seleccionado
    LaunchedEffect(seleccionadoResId) {
        viewModel.loadRanking(seleccionadoResId)
    }

    Scaffold(
        bottomBar = {
            // Calculamos los puntos del usuario actual para mostrarlos en el footer persistente
            val puntosMios = state.players.find { it.isCurrentUser }?.points ?: 0
            TotalPointsFooter(points = puntosMios)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(color = colorResource(id = R.color.blancoHueso))
        ) {
            // Cabecera con gradiente y título
            RankingHeader()

            // Selector de pestañas para el periodo temporal
            TimeSelectorSection(
                seleccionadoResId = seleccionadoResId,
                onSeleccionChange = { seleccionadoResId = it }
            )

            // Título dinámico según la selección
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


            // Contenedor de la lista o estado de carga
            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colorResource(id = R.color.magentaOscuro)
                    )
                } else {
                    // Lista optimizada de jugadores
                    // Nota: Se omite 'key' porque el modelo UserRanking usa el nombre/posición
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.players) { player ->
                            RankingItem(player)
                            //para lo del objeto userranking
                            /*
                            RankingItem(player = player, onClick = { onPlayerClick(player) })
                             */
                        }
                    }
                }
            }
        }
    }
}