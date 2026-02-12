package cat.copernic.aguamap1.presentation.navigation

import cat.copernic.aguamap1.R

/*
sealed class es como un enum pero con más flexibilidad, todas las
clases hijas de esta clase deben tener la misma estructura
*/
sealed class BottomNavItem(val route: String, val icon: Int, val label: String) {
    object Map : BottomNavItem("map", R.drawable.map_24px, "Mapa")
    object Categories :
        BottomNavItem("category", R.drawable.format_list_bulleted_24px, "Categorías")

    object Game : BottomNavItem("game", R.drawable.sports_esports_24px, "Juego")
    object Ranking : BottomNavItem("ranking", R.drawable.rewarded_ads_24px, "Ranking")
    object Profile : BottomNavItem("account", R.drawable.account_circle_24px, "Perfil")
}