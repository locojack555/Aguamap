package cat.copernic.aguamap1.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.reusable.AguaMapHeader
import cat.copernic.aguamap1.presentation.reusable.AguaMapInput
import cat.copernic.aguamap1.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    navigateToForgotPassword: () -> Unit = {},
    navigateToSingUp: () -> Unit = {}, // Mantengo tu nombre de función 'SingUp'
    onLoginSuccess: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.resetState()
        viewModel.navigateToHome.collect {
            onLoginSuccess()
        }
    }

    // Diálogo para completar el nombre tras el primer login (Social o Registro incompleto)
    if (viewModel.needsName) {
        AlertDialog(
            onDismissRequest = { /* Bloqueado hasta completar */ },
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
                    Text(text = stringResource(R.string.login_dialog_welcome_message), color = Blanco)
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
                    // Validación: Nombre y al menos un apellido
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

                    AguaMapInput(
                        label = stringResource(R.string.login_label_email),
                        placeholder = stringResource(R.string.login_placeholder_email),
                        value = viewModel.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        isError = viewModel.isError
                    )

                    AguaMapInput(
                        label = stringResource(R.string.login_label_password),
                        placeholder = stringResource(R.string.login_placeholder_password),
                        value = viewModel.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        isPasswordField = true,
                        isError = viewModel.isError
                    )

                    // Errores dinámicos
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