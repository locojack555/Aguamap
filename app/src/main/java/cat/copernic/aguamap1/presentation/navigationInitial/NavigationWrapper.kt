package cat.copernic.aguamap1.presentation.navigationInitial

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cat.copernic.aguamap1.presentation.forgotpassword.ForgotPasswordScreen
import cat.copernic.aguamap1.presentation.initial.InitialScreen
import cat.copernic.aguamap1.presentation.login.LoginScreen
import cat.copernic.aguamap1.presentation.maps.HomeScreen
import cat.copernic.aguamap1.presentation.singup.SingUpScreen

@Composable
fun NavigationWrapper(navHostController: NavHostController) {

    NavHost(navController = navHostController, startDestination = RootScreen.Initial.route) {
        composable(RootScreen.Initial.route) {
            InitialScreen(navHostController)
        }
        composable(RootScreen.Login.route) {
            LoginScreen(
                navigateToForgotPassword = { navHostController.navigate(RootScreen.ForgotPassword.route) },
                navigateToSingUp = { navHostController.navigate(RootScreen.SignUp.route) },
                onLoginSuccess = { navHostController.navigate(RootScreen.Home.route) }
            )
        }
        composable(RootScreen.SignUp.route) {
            SingUpScreen(
                navigateToLogin = { navHostController.navigate(RootScreen.Login.route) })
        }
        composable(RootScreen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navigateToLogin = { navHostController.navigate(RootScreen.Login.route) })
        }
        composable(RootScreen.Home.route) {
            HomeScreen(rootNavController = navHostController)
        }
    }
}