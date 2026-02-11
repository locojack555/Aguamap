package cat.copernic.aguamap1.presentation.singup

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.copernic.aguamap1.presentation.reusable.AguaMapHeader
import cat.copernic.aguamap1.presentation.reusable.AguaMapInput
import cat.copernic.aguamap1.ui.theme.AguaMapGradient
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro

@Composable
fun SingUpScreen(
    viewModel: SingUpViewModel = viewModel(),
    navigateToLogin: () -> Unit = {}
) {
    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) navigateToLogin()
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
                        text = "Registrarse",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Negro
                    )
                    AguaMapInput(
                        "Correo electrónico",
                        "tu@correo.com",
                        viewModel.email,
                        onValueChange = { viewModel.email = it }
                    )
                    AguaMapInput(
                        "Contraseña",
                        "**********",
                        viewModel.password,
                        onValueChange = {
                            viewModel.password = it
                            viewModel.passwordError = null
                        },
                        isError = viewModel.passwordError != null,
                        isPasswordField = true
                    )
                    if (viewModel.passwordError != null) {
                        Text(
                            text = viewModel.passwordError!!,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
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
                        Text("Registrarse", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("¿Ya tienes cuenta?", color = Color.Gray)
                        TextButton(onClick = { navigateToLogin() }) {
                            Text("Inicia sesión", fontWeight = FontWeight.Bold, color = Blue10)
                        }
                    }
                }
            }
        }
    }
}