package cat.copernic.aguamap1

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cat.copernic.aguamap1.presentation.forgotpassword.ForgotPasswordScreen
import cat.copernic.aguamap1.presentation.home.HomeScreen
import cat.copernic.aguamap1.presentation.initial.InitialScreen
import cat.copernic.aguamap1.presentation.login.LoginScreen
import cat.copernic.aguamap1.presentation.singup.SingUpScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationWrapper(navHostController: NavHostController, auth: FirebaseAuth) {

    NavHost(navController = navHostController, startDestination = "initial") {
        composable("initial") {
            InitialScreen(navHostController)
        }
        composable("logIn") {
            LoginScreen(
                navigateToForgotPassword = { navHostController.navigate("forgotPassword") },
                navigateToHome = { navHostController.navigate("home") },
                navigateToSingUp = { navHostController.navigate("singUp") },
                auth
            )
        }
        composable("singUp") {
            SingUpScreen(auth)
        }
        composable("forgotPassword") {
            ForgotPasswordScreen(auth)
        }
        composable("home") {
            HomeScreen(auth)
        }
    }
}