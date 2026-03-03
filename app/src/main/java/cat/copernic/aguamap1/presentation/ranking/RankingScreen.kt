package cat.copernic.aguamap1.presentation.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Importación obligatoria para que funcione items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import cat.copernic.aguamap1.presentation.ranking.components.*

@Composable
fun RankingScreen(viewModel: RankingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var seleccionadoResId by remember { mutableIntStateOf(R.string.ranking_day) }

    LaunchedEffect(seleccionadoResId) {
        viewModel.loadRanking(seleccionadoResId)
    }

    Scaffold(
        bottomBar = {
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
            RankingHeader()

            TimeSelectorSection(
                seleccionadoResId = seleccionadoResId,
                onSeleccionChange = { seleccionadoResId = it }
            )

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

            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colorResource(id = R.color.magentaOscuro)
                    )
                } else {
                    // CORRECCIÓN: Eliminado 'key = { it.id }' porque UserRanking no tiene 'id'
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.players) { player ->
                            RankingItem(player)
                        }
                    }
                }
            }
        }
    }
}