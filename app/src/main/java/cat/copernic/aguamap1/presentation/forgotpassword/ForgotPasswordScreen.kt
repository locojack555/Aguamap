package cat.copernic.aguamap1.presentation.forgotpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import cat.copernic.aguamap1.ui.theme.Rojo

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
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
                        text = stringResource(R.string.recovery_password),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Negro
                    )

                    Text(
                        text = stringResource(R.string.text_forgot_password),
                        textAlign = TextAlign.Justify,
                        fontSize = 16.sp,
                        color = Negro,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    AguaMapInput(
                        label = stringResource(R.string.email),
                        placeholder = stringResource(R.string.email_example),
                        value = viewModel.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        isError = viewModel.isError
                    )

                    if (viewModel.isSent) {
                        Text(
                            text = stringResource(R.string.link_sent),
                            color = Blue10,
                            modifier = Modifier.padding(top = 8.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (viewModel.isError) {
                        Text(
                            text = stringResource(R.string.incorrect_email),
                            color = Rojo,
                            modifier = Modifier.padding(top = 8.dp),
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.onResetPasswordClick() },
                        // DESHABILITADO si el email está vacío para evitar clics innecesarios
                        enabled = viewModel.email.isNotBlank(),
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .fillMaxWidth(0.7f)
                            .align(Alignment.CenterHorizontally)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue10,
                            disabledContainerColor = Blue10.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.recovery_password),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blanco
                        )
                    }

                    Text(
                        text = stringResource(R.string.back_home),
                        color = Blue10,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 20.dp)
                            .clickable(
                                onClickLabel = stringResource(R.string.back_home),
                                onClick = navigateToLogin
                            )
                    )
                }
            }
        }
    }
}