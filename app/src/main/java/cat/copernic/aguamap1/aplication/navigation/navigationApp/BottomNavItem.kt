package cat.copernic.aguamap1.aplication.navigation.navigationApp

import cat.copernic.aguamap1.R

/**
 * Representa cada uno de los destinos accesibles desde la barra de navegación inferior (BottomBar).
 * Se utiliza una 'sealed class' para definir una jerarquía cerrada de rutas, asegurando que
 * el sistema de navegación solo maneje destinos válidos y conocidos.
 *
 * @param route Identificador único de la ruta para el NavController.
 * @param icon Recurso drawable del icono que se mostrará en la barra.
 * @param label Recurso string para el nombre de la sección (soporta multiidioma).
 */
sealed class BottomNavItem(val route: String, val icon: Int, val label: Int) {

    // Sección del Mapa principal de fuentes
    object Map : BottomNavItem("map", R.drawable.map_24px, R.string.map)

    // Listado de categorías de fuentes (Ej: Potable, No potable, Mascotas)
    object Categories :
        BottomNavItem("category", R.drawable.format_list_bulleted_24px, R.string.category)

    // Sección de gamificación (Retos, logros, etc.)
    object Game : BottomNavItem("game", R.drawable.sports_esports_24px, R.string.game)

    // Clasificación de usuarios basada en sus contribuciones
    object Ranking : BottomNavItem("ranking", R.drawable.rewarded_ads_24px, R.string.ranking)

    // Gestión del perfil del usuario, estadísticas personales y ajustes
    object Profile : BottomNavItem("account", R.drawable.account_circle_24px, R.string.profile)
    object Creditos : BottomNavItem("creditos", R.drawable.icon_corona, R.string.credits)
}