package cat.copernic.aguamap1.aplication.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.aplication.commons.AguaMapHeader
import cat.copernic.aguamap1.aplication.commons.AguaMapInput
import cat.copernic.aguamap1.ui.theme.AguaMapGradient
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.PurpleGrey40
import cat.copernic.aguamap1.ui.theme.Rojo

/**
 * Pantalla de inicio de sesión principal.
 * Gestiona la autenticación de usuarios existentes y ofrece un flujo para completar
 * el perfil (nombre y apellidos) en caso de que los datos sean insuficientes tras el login.
 *
 * @param viewModel Lógica de estado y validación de credenciales.
 * @param navigateToForgotPassword Redirección para recuperar contraseña.
 * @param navigateToSingUp Redirección al flujo de registro de nuevos usuarios.
 * @param onLoginSuccess Callback ejecutado tras una autenticación exitosa.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    navigateToForgotPassword: () -> Unit = {},
    navigateToSingUp: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    /**
     * Escucha eventos de navegación exitosa emitidos por el ViewModel.
     */
    LaunchedEffect(Unit) {
        viewModel.resetState()
        viewModel.navigateToHome.collect {
            onLoginSuccess()
        }
    }

    /**
     * DIÁLOGO DE COMPLETAR REGISTRO:
     * Se activa si el usuario se ha logueado pero su perfil en la base de datos
     * carece de un nombre válido. Obliga a introducir Nombre y Apellido.
     */
    if (viewModel.needsName) {
        AlertDialog(
            onDismissRequest = { /* Bloqueado para asegurar integridad de datos */ },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = stringResource(R.string.login_dialog_info_title),
                    fontWeight = FontWeight.Bold,
                    color = Blanco
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.login_dialog_welcome_message),
                        color = Blanco
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AguaMapInput(
                        label = stringResource(R.string.login_label_full_name),
                        placeholder = stringResource(R.string.login_placeholder_name_example),
                        value = viewModel.name,
                        onValueChange = { viewModel.onNameChanged(it) },
                        color = Blanco
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onCompleteRegistration() },
                    // Validación: El nombre debe contener al menos dos palabras
                    enabled = viewModel.name.trim().split(" ").filter { it.isNotEmpty() }.size >= 2,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Negro.copy(alpha = 0.8f),
                        disabledContainerColor = Negro.copy(alpha = 0.4f)
                    )
                ) {
                    Text(stringResource(R.string.login_btn_save_continue), color = Blanco)
                }
            },
            containerColor = Color.Transparent,
            modifier = Modifier.background(AguaMapGradient, shape = RoundedCornerShape(28.dp))
        )
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AguaMapGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Cabecera con Logo
            AguaMapHeader()

            Spacer(modifier = Modifier.height(56.dp))

            // Contenedor de formulario con bordes redondeados superiores
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(R.string.login_title_sign_in),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Negro
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de Email
                    AguaMapInput(
                        label = stringResource(R.string.login_label_email),
                        placeholder = stringResource(R.string.login_placeholder_email),
                        value = viewModel.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        isError = viewModel.isError
                    )

                    // Campo de Contraseña
                    AguaMapInput(
                        label = stringResource(R.string.login_label_password),
                        placeholder = stringResource(R.string.login_placeholder_password),
                        value = viewModel.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        isPasswordField = true,
                        isError = viewModel.isError
                    )

                    /**
                     * Gestión de Errores: Credenciales o Verificación de Email.
                     */
                    if (viewModel.isError) {
                        Text(
                            text = stringResource(R.string.login_error_invalid_credentials),
                            color = Rojo,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (viewModel.isEmailVerifiedError) {
                        Text(
                            text = stringResource(R.string.login_error_email_not_verified),
                            color = Rojo,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Text(
                        text = stringResource(R.string.login_btn_forgot_password),
                        color = Blue10,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(vertical = 24.dp)
                            .clickable { navigateToForgotPassword() }
                    )

                    // Botón principal de entrada
                    Button(
                        onClick = { viewModel.onLoginClick() },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .align(Alignment.CenterHorizontally)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue10)
                    ) {
                        Text(
                            text = stringResource(R.string.login_btn_enter),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blanco
                        )
                    }

                    // Enlace a Registro
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.login_text_no_account),
                            color = PurpleGrey40
                        )
                        TextButton(onClick = { navigateToSingUp() }) {
                            Text(
                                text = stringResource(R.string.login_btn_register_now),
                                fontWeight = FontWeight.Bold,
                                color = Blue10
                            )
                        }
                    }
                }
            }
        }
    }
}


/*package cat.copernic.aguamap1.aplication.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.aplication.commons.AguaMapHeader
import cat.copernic.aguamap1.aplication.commons.AguaMapInput
import cat.copernic.aguamap1.ui.theme.AguaMapGradient
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.PurpleGrey40
import cat.copernic.aguamap1.ui.theme.Rojo

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    navigateToForgotPassword: () -> Unit = {},
    navigateToSingUp: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    var currentStep by rememberSaveable { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        viewModel.resetState()
        viewModel.navigateToHome.collect {
            onLoginSuccess()
        }
    }

    // DIÁLOGO DE COMPLETAR REGISTRO
    if (viewModel.needsName) {
        AlertDialog(
            onDismissRequest = {},
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = stringResource(R.string.login_dialog_info_title),
                    fontWeight = FontWeight.Bold,
                    color = Blanco
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.login_dialog_welcome_message),
                        color = Blanco
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AguaMapInput(
                        label = stringResource(R.string.login_label_full_name),
                        placeholder = stringResource(R.string.login_placeholder_name_example),
                        value = viewModel.name,
                        onValueChange = { viewModel.onNameChanged(it) },
                        color = Blanco
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onCompleteRegistration() },
                    enabled = viewModel.name.trim().split(" ").filter { it.isNotEmpty() }.size >= 2,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Negro.copy(alpha = 0.8f),
                        disabledContainerColor = Negro.copy(alpha = 0.4f)
                    )
                ) {
                    Text(stringResource(R.string.login_btn_save_continue), color = Blanco)
                }
            },
            containerColor = Color.Transparent,
            modifier = Modifier.background(AguaMapGradient, shape = RoundedCornerShape(28.dp))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AguaMapGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AguaMapHeader()

            Spacer(modifier = Modifier.height(56.dp))

            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(R.string.login_title_sign_in),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Negro
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (currentStep) {

                        // ── PASO 1: Email ─────────────────────────────────────────
                        1 -> {
                            AguaMapInput(
                                label = stringResource(R.string.login_label_email),
                                placeholder = stringResource(R.string.login_placeholder_email),
                                value = viewModel.email,
                                onValueChange = { viewModel.onEmailChanged(it) },
                                isError = viewModel.isError
                            )

                            if (viewModel.isError) {
                                Text(
                                    text = stringResource(R.string.login_error_invalid_credentials),
                                    color = Rojo,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { currentStep = 2 },
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .align(Alignment.CenterHorizontally)
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                                enabled = viewModel.email.isNotBlank()
                            ) {
                                Text(
                                    text = "Siguiente",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Blanco
                                )
                            }

                            // Enlace a Registro
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.login_text_no_account),
                                    color = PurpleGrey40
                                )
                                TextButton(onClick = { navigateToSingUp() }) {
                                    Text(
                                        text = stringResource(R.string.login_btn_register_now),
                                        fontWeight = FontWeight.Bold,
                                        color = Blue10
                                    )
                                }
                            }
                        }

                        // ── PASO 2: Contraseña ────────────────────────────────────
                        2 -> {
                            AguaMapInput(
                                label = stringResource(R.string.login_label_password),
                                placeholder = stringResource(R.string.login_placeholder_password),
                                value = viewModel.password,
                                onValueChange = { viewModel.onPasswordChanged(it) },
                                isPasswordField = true,
                                isError = viewModel.isError
                            )

                            if (viewModel.isError) {
                                Text(
                                    text = stringResource(R.string.login_error_invalid_credentials),
                                    color = Rojo,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            if (viewModel.isEmailVerifiedError) {
                                Text(
                                    text = stringResource(R.string.login_error_email_not_verified),
                                    color = Rojo,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            TextButton(
                                onClick = { navigateToForgotPassword() },
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.login_btn_forgot_password),
                                    color = Blue10,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { currentStep-- },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PurpleGrey40)
                                ) {
                                    Text(
                                        text = "Atrás",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco
                                    )
                                }

                                Button(
                                    onClick = { viewModel.onLoginClick() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                                    enabled = viewModel.password.isNotBlank()
                                ) {
                                    Text(
                                        text = stringResource(R.string.login_btn_enter),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
*/