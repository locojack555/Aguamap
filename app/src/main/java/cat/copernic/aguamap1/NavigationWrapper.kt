package cat.copernic.aguamap1

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cat.copernic.aguamap1.presentation.forgotpassword.ForgotPasswordScreen
import cat.copernic.aguamap1.presentation.home.HomeScreen
import cat.copernic.aguamap1.presentation.initial.InitialScreen
import cat.copernic.aguamap1.presentation.login.LoginScreen
import cat.copernic.aguamap1.presentation.ranking.RankingScreen
import cat.copernic.aguamap1.presentation.singup.SingUpScreen

@Composable
fun NavigationWrapper(navHostController: NavHostController) {

    NavHost(navController = navHostController, startDestination = "initial") { //poner inital
        composable("initial") {
            InitialScreen(navHostController)
        }
        composable("logIn") {
            LoginScreen(
                navigateToForgotPassword = { navHostController.navigate("forgotPassword") },
                navigateToSingUp = { navHostController.navigate("singUp") },
                onLoginSuccess = { navHostController.navigate("home") }
            )
        }
        composable("singUp") {
            SingUpScreen(
                navigateToLogin = { navHostController.navigate("logIn") })
        }
        composable("forgotPassword") {
            ForgotPasswordScreen(
                navigateToLogin = { navHostController.navigate("logIn") })
        }
        composable("home") {
            HomeScreen()
        }
        composable("ranking") {
            RankingScreen()
        }
    }
}