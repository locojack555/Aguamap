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
    NavHost(navController = navHostController, startDestination = RootScreen.Initial.route) {

        // 1. Pantalla Inicial / Bienvenida
        composable(RootScreen.Initial.route) {
            InitialScreen(navHostController)
        }

        // 2. Pantalla de Login
        // Gestiona las navegaciones hacia el olvido de contraseña, registro o éxito en el acceso.
        composable(RootScreen.Login.route) {
            LoginScreen(
                navigateToForgotPassword = { navHostController.navigate(RootScreen.ForgotPassword.route) },
                navigateToSingUp = { navHostController.navigate(RootScreen.SignUp.route) },
                onLoginSuccess = {
                    // Al entrar al Home, se suele limpiar el historial previo para no volver al Login con 'atrás'
                    navHostController.navigate(RootScreen.Home.route) {
                        popUpTo(RootScreen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Pantalla de Registro (SignUp)
        composable(RootScreen.SignUp.route) {
            SingUpScreen(
                navigateToLogin = { navHostController.navigate(RootScreen.Login.route) })
        }

        // 4. Pantalla de Recuperación de Contraseña
        composable(RootScreen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navigateToLogin = { navHostController.navigate(RootScreen.Login.route) })
        }

        // 5. Pantalla Principal (Home)
        // Recibe el 'rootNavController' para que las pantallas internas puedan, por ejemplo,
        // cerrar sesión y volver al flujo de Login definido aquí.
        composable(RootScreen.Home.route) {
            HomeScreen(rootNavController = navHostController)
        }
    }
}