package cat.copernic.aguamap1.aplication.singup

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import cat.copernic.aguamap1.ui.theme.Rojo

/**
 * Pantalla de registro de nuevos usuarios (SignUp).
 * Utiliza el AguaMapHeader reutilizable y una Card inferior para el formulario,
 * siguiendo la estética de "hoja deslizante" de la aplicación.
 */
@Composable
fun SingUpScreen(
    viewModel: SingUpViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AguaMapGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // CABECERA: Incluye logo y selector de idioma (isSplash = true por defecto)
            AguaMapHeader()

            Spacer(modifier = Modifier.height(56.dp))


            // CUERPO: Formulario de registro en una Card con esquinas redondeadas superiores
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Text(
                        text = stringResource(R.string.text_sing_in_link),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Negro
                    )

                    // INPUT: Correo electrónico
                    AguaMapInput(
                        label = stringResource(R.string.email),
                        placeholder = stringResource(R.string.email_example),
                        value = viewModel.email,
                        onValueChange = { viewModel.email = it },
                        isError = viewModel.emailError != null
                    )
                    viewModel.emailError?.let {
                        Text(
                            text = stringResource(it),
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    // INPUT: Contraseña
                    AguaMapInput(
                        label = stringResource(R.string.password),
                        placeholder = stringResource(R.string.password_example),
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        isError = viewModel.passwordError != null,
                        isPasswordField = true
                    )
                    viewModel.passwordError?.let {
                        Text(
                            text = stringResource(it),
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    // Feedback de verificación (Si el registro fue exitoso pero falta confirmar email)
                    if (viewModel.isWaitingVerification) {
                        Text(
                            text = stringResource(R.string.link_sent),
                            color = Rojo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    // BOTÓN: Ejecutar registro
                    Button(
                        onClick = { viewModel.onSingUpClick() },
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .fillMaxWidth(0.6f)
                            .align(Alignment.CenterHorizontally)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue10)
                    ) {
                        Text(
                            text = stringResource(R.string.text_sing_in_link),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // PIE DE PÁGINA: Enlace para volver al login
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.has_acc), color = Color.Gray)
                        TextButton(onClick = { navigateToLogin() }) {
                            Text(
                                text = stringResource(R.string.init),
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
/*
package cat.copernic.aguamap1.aplication.singup

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
fun SingUpScreen(
    viewModel: SingUpViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit = {}
) {
    var currentStep by rememberSaveable { mutableStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AguaMapGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AguaMapHeader()

            Spacer(modifier = Modifier.height(56.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Text(
                        text = stringResource(R.string.text_sing_in_link),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Negro
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (currentStep) {

                        // ── PASO 1: Email ─────────────────────────────────────────
                        1 -> {
                            AguaMapInput(
                                label = stringResource(R.string.email),
                                placeholder = stringResource(R.string.email_example),
                                value = viewModel.email,
                                onValueChange = { viewModel.email = it },
                                isError = viewModel.emailError != null
                            )
                            viewModel.emailError?.let {
                                Text(
                                    text = stringResource(it),
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { currentStep = 2 },
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .align(Alignment.CenterHorizontally)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                                enabled = viewModel.email.isNotBlank() && viewModel.emailError == null
                            ) {
                                Text(
                                    text = "Siguiente",
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
                                Text(stringResource(R.string.has_acc), color = Color.Gray)
                                TextButton(onClick = { navigateToLogin() }) {
                                    Text(
                                        text = stringResource(R.string.init),
                                        fontWeight = FontWeight.Bold,
                                        color = Blue10
                                    )
                                }
                            }
                        }

                        // ── PASO 2: Contraseña ────────────────────────────────────
                        2 -> {
                            AguaMapInput(
                                label = stringResource(R.string.password),
                                placeholder = stringResource(R.string.password_example),
                                value = viewModel.password,
                                onValueChange = { viewModel.password = it },
                                isError = viewModel.passwordError != null,
                                isPasswordField = true
                            )
                            viewModel.passwordError?.let {
                                Text(
                                    text = stringResource(it),
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }

                            if (viewModel.isWaitingVerification) {
                                Text(
                                    text = stringResource(R.string.link_sent),
                                    color = Rojo,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { currentStep-- },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
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
                                    onClick = { viewModel.onSingUpClick() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                                    enabled = viewModel.password.isNotBlank()
                                ) {
                                    Text(
                                        text = stringResource(R.string.text_sing_in_link),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
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