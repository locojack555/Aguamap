package cat.copernic.aguamap1.aplication
/*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.aplication.commons.AguaMapHeader
import cat.copernic.aguamap1.aplication.login.LoginViewModel
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.PerfilGradient

@Composable
fun CreditosScreen() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PerfilGradient)
            .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        AguaMapHeader(isSplash = false)
        Spacer(modifier = Modifier.padding(16.dp))
        Column(
        ) {
            Text("Creditos:", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Blanco)
            Spacer(modifier = Modifier.padding(16.dp))
            Text("Integrantes:", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Blanco)
            Spacer(modifier = Modifier.padding(8.dp))
            Text("* Luis Mariño", color = Blanco)
            Text("* Cristina Jimenez", color = Blanco)
            Text("* Adria Gonzalez", color = Blanco)
            Text("* Jack Arevalo", color = Blanco)
            Spacer(modifier = Modifier.padding(16.dp))
            Text("13/03/2026", color = Blanco)
        }
    }
}*/

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.aplication.commons.AguaMapHeader
import cat.copernic.aguamap1.ui.theme.AguaMapGradient
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Negro

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
fun CreditosScreen(
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AguaMapGradient),
        contentAlignment = Alignment.Center
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
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Creditos:",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Negro
                    )
                    Spacer(modifier = Modifier.padding(20.dp))
                    Text(
                        "Integrantes:",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Negro
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text("* Luis Mariño", color = Negro, fontSize = 20.sp)
                    Text("* Cristina Jimenez", color = Negro, fontSize = 20.sp)
                    Text("* Adria Gonzalez", color = Negro, fontSize = 20.sp)
                    Text("* Jack Arevalo", color = Negro, fontSize = 20.sp)
                    Spacer(modifier = Modifier.padding(16.dp))
                    Text("13/03/2026", color = Negro, fontSize = 20.sp)
                }
            }
        }
    }
}

