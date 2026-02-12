package cat.copernic.aguamap1.presentation.reusable

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import cat.copernic.aguamap1.presentation.navigation.BottomNavItem
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro

@Composable
fun HomeBottomNavigation(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Map,
        BottomNavItem.Categories,
        BottomNavItem.Game,
        BottomNavItem.Ranking,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = Blanco,
        tonalElevation = 8.dp
    ) {
        //Observa la pila de navegación en tiempo real para la sincronización
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        //Obtiene la ruta actual para saber en que pantalla estamos
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(text = item.label, fontSize = 10.sp) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            //guarda el estado de la pantalla anterior
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            //Mantiene una sola instancia de cada pantalla
                            launchSingleTop = true
                            //Restaura el estado de la pantalla anterior
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Blue10,
                    selectedTextColor = Blue10,
                    unselectedIconColor = Negro,
                    unselectedTextColor = Negro,
                    indicatorColor = Blue10.copy(alpha = 0.1f)
                )
            )
        }
    }
}