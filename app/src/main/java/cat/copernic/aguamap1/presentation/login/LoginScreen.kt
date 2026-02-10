package cat.copernic.aguamap1.presentation.login

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Blue20
import cat.copernic.aguamap1.ui.theme.Negro

@Preview(showBackground = true)
@Composable
fun LoginScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.horizontalGradient(listOf(Blue10, Blue20)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.gota),
                    contentDescription = "Logo",
                    modifier = Modifier.size(160.dp)
                )
                Text(
                    text = "AguaMap",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Blanco
                )
                Text(
                    text = "Encuentra fuentes en Terrassa",
                    fontSize = 20.sp,
                    color = Blanco,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
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
                        text = "Iniciar Sesión",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Negro
                    )
                    Text(
                        text = "Correo electrónico",
                        fontSize = 16.sp,
                        color = Negro,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text(text = "tu@email.com", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "Contraseña",
                        fontSize = 16.sp,
                        color = Negro,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text(text = "**********", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = Blue10,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(vertical = 20.dp)
                            .clickable { /* Navegar */ }
                    )

                    Button(
                        onClick = { /* Login */ },
                        modifier = Modifier
                            .fillMaxWidth(0.6f) // Centrado y con buen tamaño
                            .align(Alignment.CenterHorizontally)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue10)
                    ) {
                        Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("¿No tienes cuenta?", color = Color.Gray)
                        TextButton(onClick = { /* Registro */ }) {
                            Text("Regístrate", fontWeight = FontWeight.Bold, color = Blue10)
                        }
                    }
                }
            }
        }
    }
}