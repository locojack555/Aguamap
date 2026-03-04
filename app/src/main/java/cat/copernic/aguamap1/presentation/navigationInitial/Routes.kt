package cat.copernic.aguamap1.presentation.navigationInitial

/**
 * Representa los destinos raíz de la aplicación.
 * Esta jerarquía define las pantallas principales que existen fuera del flujo
 * de navegación principal de la "Home" (mapa, ranking, etc.).
 * * Se utiliza una 'sealed class' para garantizar que el NavHost de raíz
 * solo pueda navegar a destinos predefinidos y seguros.
 */
sealed class RootScreen(val route: String) {

    // Pantalla de bienvenida / Splash inicial
    object Initial : RootScreen("initial")

    // Pantalla de acceso para usuarios registrados
    object Login : RootScreen("logIn")

    // Pantalla de registro para nuevos usuarios
    object SignUp : RootScreen("singUp")

    // Pantalla para la recuperación de cuenta mediante email
    object ForgotPassword : RootScreen("forgotPassword")

    // El contenedor principal de la aplicación (que incluye su propia navegación interna)
    object Home : RootScreen("home")
}