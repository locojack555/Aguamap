package cat.copernic.aguamap1.presentation.initial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import cat.copernic.aguamap1.presentation.reusable.AguaMapHeader
import cat.copernic.aguamap1.ui.theme.AguaMapGradient

@Composable
fun InitialScreen(
    navController: NavController,
    viewModel: InitialViewModel = hiltViewModel()
) {
    //Launchedeffect para que solo se ejecute una vez al abrir la pantalla
    val destination by viewModel.destination.collectAsState()

    LaunchedEffect(destination) {
        destination?.let { route ->
            navController.popBackStack()
            navController.navigate(route)
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
            isSplash = false
        )
        Spacer(modifier = Modifier.weight(1.2f))
    }
}