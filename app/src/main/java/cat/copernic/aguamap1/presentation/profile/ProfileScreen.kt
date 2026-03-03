package cat.copernic.aguamap1.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.profile.components.*

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
    val profileState by viewModel.profileState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAdmin = profileState.userRole.equals("ADMIN", ignoreCase = true)

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
            // 1. Cabecera con Stats
            CabeceraPerfil(profileState, isAdmin)

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 2. Sección Cuenta
                SeccionPerfil(titulo = stringResource(id = R.string.profile_account_subtitle)) {
                    ElementoOpcionPerfil(
                        icono = androidx.compose.material.icons.Icons.Default.Edit,
                        etiqueta = stringResource(id = R.string.profile_label_edit_profile),
                        onClick = navigateToEditProfile
                    )
                    DivisorPerfil()
                    ElementoOpcionPerfil(
                        icono = androidx.compose.material.icons.Icons.Default.Settings,
                        etiqueta = stringResource(id = R.string.profile_label_config),
                        onClick = navigateToSettings
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Sección Admin (Si aplica)
                if (isAdmin) {
                    SeccionPanelAdmin(
                        pendingCommentsCount = 0, // Conectar con ViewModel si es necesario
                        pendingFountainsCount = 0,
                        navigateToModeration = navigateToModeration,
                        navigateToFountainReports = navigateToFountainReports
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4. Botón Logout
                BotonCerrarSesion(onClick = navigateToLogin)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}