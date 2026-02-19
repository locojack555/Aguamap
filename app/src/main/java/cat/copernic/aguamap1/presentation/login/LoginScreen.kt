package cat.copernic.aguamap1.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.reusable.AguaMapHeader
import cat.copernic.aguamap1.presentation.reusable.AguaMapInput
import cat.copernic.aguamap1.ui.theme.AguaMapGradient
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.PurpleGrey40
import cat.copernic.aguamap1.ui.theme.Rojo

//@Preview
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    navigateToForgotPassword: () -> Unit = {},
    navigateToSingUp: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    //Se ejecuta una vez al abrir esta pantalla
    LaunchedEffect(Unit) {
        viewModel.resetState()
        viewModel.checkPendingRegistration()
        viewModel.navigateToHome.collect {
            onLoginSuccess()
        }
    }
    if (viewModel.needsName) {
        AlertDialog(
            onDismissRequest = { /* Opcional: podrías poner needsName = false */ },
            title = {
                Text(
                    text = stringResource(R.string.info),
                    fontWeight = FontWeight.Bold,
                    color = Blanco
                )
            },
            text = {
                Column {
                    Text(text = stringResource(R.string.welcome), color = Blanco)
                    Spacer(modifier = Modifier.height(8.dp))
                    AguaMapInput(
                        label = stringResource(R.string.complete_name),
                        placeholder = stringResource(R.string.name_example),
                        value = viewModel.name,
                        onValueChange = { viewModel.onNameChanged(it) },
                        color = Blanco
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onCompleteRegistration() },
                    enabled = viewModel.name.trim()
                        .split(" ").size >= 2,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Negro.copy(alpha = 0.8f),
                        disabledContainerColor = Negro.copy(alpha = 0.8f)
                    )
                ) {
                    Text(stringResource(R.string.map), color = Blanco)
                }
            },
            containerColor = Color.Transparent,
            modifier = Modifier.background(AguaMapGradient)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AguaMapGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AguaMapHeader()
            Spacer(modifier = Modifier.height(56.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
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
                        text = stringResource(R.string.sing_in),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Negro
                    )
                    AguaMapInput(
                        stringResource(R.string.email),
                        stringResource(R.string.email_example),
                        viewModel.email,
                        onValueChange = {
                            viewModel.onEmailChanged(it)
                        },
                        isError = viewModel.isError
                    )
                    AguaMapInput(
                        stringResource(R.string.password),
                        stringResource(R.string.password_example),
                        viewModel.password,
                        onValueChange = {
                            viewModel.onPasswordChanged(it)
                        },
                        isPasswordField = true,
                        isError = viewModel.isError
                    )
                    if (viewModel.isError) {
                        Text(
                            text = stringResource(R.string.incorrect_sing_in),
                            color = Rojo,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    if (viewModel.isEmailVerifiedError) {
                        Text(
                            text = stringResource(R.string.error_email_not_verified),
                            color = Rojo,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.text_recovery_password),
                        color = Blue10,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(vertical = 20.dp)
                            .clickable { navigateToForgotPassword() }
                    )
                    Button(
                        onClick = { viewModel.onLoginClick() },
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .align(Alignment.CenterHorizontally)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue10)
                    ) {
                        Text(
                            stringResource(R.string.login),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.text_sing_in), color = PurpleGrey40)
                        TextButton(onClick = { navigateToSingUp() }) {
                            Text(
                                stringResource(R.string.text_sing_in_link),
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