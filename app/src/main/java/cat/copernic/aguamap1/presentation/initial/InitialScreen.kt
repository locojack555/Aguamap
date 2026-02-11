package cat.copernic.aguamap1.presentation.initial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cat.copernic.aguamap1.presentation.reusable.AguaMapHeader
import cat.copernic.aguamap1.ui.theme.AguaMapGradient

@Composable
fun InitialScreen(
    navController: NavController,
    viewModel: InitialViewModel = viewModel()
) {
    //Launchedeffect para que solo se ejecute una vez al abrir la pantalla
    LaunchedEffect(Unit) {
        viewModel.destination.collect { route ->
            navController.popBackStack() // Elimina la pantalla(anterior) de inicio en la pila
            navController.navigate(route) // Navega a la ruta especificada
        }
    }
    Splash()
}

@Composable
fun Splash() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AguaMapGradient),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.8f))
        AguaMapHeader(
            logoSize = 220.dp,
            innerSpacing = 40.dp,
            showSubtitle = false
        )
        Spacer(modifier = Modifier.weight(1.2f))
    }
}