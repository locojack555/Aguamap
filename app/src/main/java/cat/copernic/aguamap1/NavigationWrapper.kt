package cat.copernic.aguamap1

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cat.copernic.aguamap1.presentation.initial.InitialScreen
import cat.copernic.aguamap1.presentation.login.LoginScreen
import cat.copernic.aguamap1.presentation.singup.SingUpScreen

@Composable
fun NavigationWrapper(navHostController: NavHostController) {

    NavHost(navController = navHostController, startDestination = "initial") {
        composable("initial") {
            InitialScreen(navHostController)
        }
        composable("login") {
            LoginScreen()
        }
        composable("SingUpScreen") {
            SingUpScreen(navHostController)
        }
    }
}