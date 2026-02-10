package cat.copernic.aguamap1.presentation.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RankingScreen(viewModel: RankingViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // Cabecera Rosa
        Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(
            brush = Brush.verticalGradient(listOf(Color(0xFF9C27B0), Color(0xFFE91E63)))
        )) {
            Text("Ranking", color = Color.White, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
        }

        // Lista de Jugadores
        LazyColumn(modifier = Modifier.weight(1f).padding(16.dp)) {
            items(state.players) { player ->
                RankingItem(player)
            }
        }

        // Barra inferior de puntos totales (Cascarón)
        // TotalPointsFooter(points = 2500)
    }
}

@Composable
fun RankingItem(player: UserRanking) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${player.position}", modifier = Modifier.width(24.dp))
            // Aquí iría el icono/avatar y los textos de nombre y puntos
            Column(modifier = Modifier.weight(1f)) {
                Text(text = player.name, fontWeight = FontWeight.Bold)
                Text(text = "${player.discovered} descubiertas · ${player.games} partidas", fontSize = 12.sp)
            }
            Text(text = "${player.points}", color = Color(0xFFFFA500), fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RankingItemPreview() {
    // Usamos datos de ejemplo (Mock Data)
    val mockUser = UserRanking(
        position = 1,
        name = "Administrador (Tú)",
        points = 125,
        discovered = 15,
        games = 25,
        isCurrentUser = true
    )

    // Aquí invocas el componente que creamos antes
    RankingItem(player = mockUser)
}

@Preview(showSystemUi = true) // Muestra la barra de estado y navegación del móvil
@Composable
fun RankingScreenPreview() {
    // Creamos un estado falso con varios usuarios
    val mockState = RankingState(
        players = listOf(
            UserRanking(1, "Administrador (Tú)", 125, 15, 25, true),
            UserRanking(2, "María García", 90, 12, 18),
            UserRanking(3, "Joan Martínez", 75, 10, 15)
        )
    )

    // Diseñamos el cascarón directamente para el Preview
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // Cabecera con Degradado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Brush.verticalGradient(listOf(Color(0xFF9C27B0), Color(0xFFE91E63))))
                .padding(24.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Text("Ranking", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Mejores jugadores", color = Color.White.copy(alpha = 0.8f))
            }
        }

        // Lista de ejemplo
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(mockState.players) { player ->
                RankingItem(player)
            }
        }
    }
}