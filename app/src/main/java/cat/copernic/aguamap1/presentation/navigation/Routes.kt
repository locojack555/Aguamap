package cat.copernic.aguamap1.presentation.navigation

sealed class RootScreen(val route: String) {
    object Initial : RootScreen("initial")
    object Login : RootScreen("logIn")
    object SignUp : RootScreen("singUp")
    object ForgotPassword : RootScreen("forgotPassword")
    object Home : RootScreen("home")
}