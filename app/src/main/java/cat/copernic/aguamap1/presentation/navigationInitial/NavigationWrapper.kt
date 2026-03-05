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

/**
 * NavigationWrapper es el punto de entrada de la navegación de la App.
 * Se encarga de alternar entre el flujo de autenticación (Login/Registro)
 * y el flujo principal de la aplicación (Home/Mapa).
 */
@Composable
fun NavigationWrapper(navHostController: NavHostController) {

    // Se define el NavHost que orquestará todas las pantallas de nivel raíz.
    // El 'startDestination' indica que la primera pantalla al abrir la app será 'Initial'.
    NavHost(
        navController = navHostController,
        startDestination = RootScreen.Initial.route
    ) {

        // 1. Pantalla Inicial / Bienvenida
        composable(RootScreen.Initial.route) {
            // El InitialScreen internamente ya usa hiltViewModel(),
            // lo que asegura la inyección de AuthRepository.
            InitialScreen(navController = navHostController)
        }

        // 2. Pantalla de Login
        composable(RootScreen.Login.route) {
            LoginScreen(
                navigateToForgotPassword = { navHostController.navigate(RootScreen.ForgotPassword.route) },
                navigateToSingUp = { navHostController.navigate(RootScreen.SignUp.route) },
                onLoginSuccess = {
                    // Limpieza del backstack para seguridad
                    navHostController.navigate(RootScreen.Home.route) {
                        popUpTo(RootScreen.Initial.route) { inclusive = true }
                        popUpTo(RootScreen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Pantalla de Registro (SignUp)
        composable(RootScreen.SignUp.route) {
            SingUpScreen(
                navigateToLogin = {
                    navHostController.navigate(RootScreen.Login.route) {
                        popUpTo(RootScreen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        // 4. Pantalla de Recuperación de Contraseña
        composable(RootScreen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navigateToLogin = {
                    navHostController.navigate(RootScreen.Login.route) {
                        popUpTo(RootScreen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }

        // 5. Pantalla Principal (Home)
        composable(RootScreen.Home.route) {
            HomeScreen(rootNavController = navHostController)
        }
    }
}