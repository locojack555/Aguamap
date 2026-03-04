package cat.copernic.aguamap1.presentation.navigationApp

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro

/**
 * Componente visual de la barra de navegación inferior.
 * Se encarga de renderizar los iconos y etiquetas de las secciones principales
 * y gestionar la lógica de navegación para mantener el estado de cada pestaña.
 *
 * @param navController Controlador de navegación encargado de realizar las transiciones.
 */
@Composable
fun HomeBottomNavigation(navController: NavHostController) {
    // Definición de los elementos que aparecerán en la barra inferior
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
        /**
         * Observamos la entrada actual de la pila de navegación (BackStack).
         * Esto permite que la UI se actualice automáticamente cuando la ruta cambia,
         * marcando el icono correspondiente como "seleccionado".
         */
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = stringResource(item.label),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.label),
                        fontSize = 10.sp
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            /**
                             * popUpTo: Limpia la pila hasta el destino inicial para evitar
                             * una acumulación masiva de pantallas en memoria.
                             */
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Evita múltiples copias del mismo destino en la parte superior
                            launchSingleTop = true
                            // Restaura el estado (scroll, filtros, etc.) al volver a una pestaña
                            restoreState = true
                        }
                    }

                    /**
                     * Lógica especial para el Perfil:
                     * Si el usuario está en una subpantalla (como editar perfil) y vuelve
                     * a pulsar el icono de Perfil en la barra, lo devolvemos a la raíz del perfil.
                     */
                    if (item.route == BottomNavItem.Profile.route && currentRoute == "edit_profile") {
                        navController.popBackStack(BottomNavItem.Profile.route, inclusive = false)
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