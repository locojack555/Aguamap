package cat.copernic.aguamap1.aplication.profile

import android.graphics.fonts.FontStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.aplication.profile.components.BotonCerrarSesion
import cat.copernic.aguamap1.aplication.profile.components.CabeceraPerfil
import cat.copernic.aguamap1.aplication.profile.components.DivisorPerfil
import cat.copernic.aguamap1.aplication.profile.components.ElementoOpcionPerfil
import cat.copernic.aguamap1.aplication.profile.components.SeccionPanelAdmin
import cat.copernic.aguamap1.aplication.profile.components.SeccionPerfil

/**
 * Pantalla principal de Perfil de Usuario.
 * Organiza la información del usuario, estadísticas, opciones de configuración
 * y el panel de administración si el usuario tiene los permisos necesarios.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit = {},
    navigateToEditProfile: () -> Unit = {},
    navigateToSettings: () -> Unit = {},
    navigateToModeration: () -> Unit = {},
    navigateToFountainReports: () -> Unit = {}
) {
    // Observación de estados del ViewModel
    val profileState by viewModel.profileState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Determinamos si el usuario tiene rol de administrador
    val isAdmin = profileState.userRole.equals("ADMIN", ignoreCase = true)


    // Contenedor con soporte para deslizar y refrescar datos
    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.loadUserData() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F3F4))
                .verticalScroll(rememberScrollState())
        ) {
            // 1. CABECERA: Muestra foto, nombre y estadísticas (puntos, fuentes, etc.)
            CabeceraPerfil(profileState, isAdmin, )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 2. SECCIÓN CUENTA: Opciones básicas de usuario
                SeccionPerfil(titulo = stringResource(id = R.string.profile_account_subtitle)) {
                    ElementoOpcionPerfil(
                        icono = Icons.Default.Edit,
                        etiqueta = stringResource(id = R.string.profile_label_edit_profile),
                        onClick = navigateToEditProfile
                    )
                    DivisorPerfil()
                    ElementoOpcionPerfil(
                        icono = Icons.Default.Settings,
                        etiqueta = stringResource(id = R.string.profile_label_config),
                        onClick = navigateToSettings
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. SECCIÓN ADMIN: Panel exclusivo para administradores
                if (isAdmin) {
                    SeccionPanelAdmin(
                        pendingCommentsCount = 0, // Nota: Se puede conectar con un State del VM para mostrar insignias numéricas
                        pendingFountainsCount = 0,
                        navigateToModeration = navigateToModeration,
                        navigateToFountainReports = navigateToFountainReports
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                // 4. ACCIÓN FINAL: Botón de cierre de sesión
                BotonCerrarSesion(onClick = {
                    navigateToLogin()
                })

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}