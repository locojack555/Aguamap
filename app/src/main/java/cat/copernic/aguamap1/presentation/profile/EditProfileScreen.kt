package cat.copernic.aguamap1.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    initialNombre: String,
    initialEmail: String,
    viewModel: ProfileViewModel,
    onBack: () -> Unit = {},
    onSaveComplete: () -> Unit = {}
) {
    // Usamos el estado interno para que el usuario pueda escribir
    var nombre by remember(initialNombre) { mutableStateOf(initialNombre) }
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }

    LaunchedEffect(initialNombre, initialEmail) {
        if (nombre.isEmpty()) nombre = initialNombre
        if (email.isEmpty()) email = initialEmail
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    // Manejo de éxito
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.resetSuccess()
            onSaveComplete()
        }
    }


    Scaffold(
        bottomBar = {
            // Aquí iría tu BottomNavigationBar existente
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF8F9FA))
                    .verticalScroll(rememberScrollState())
            ) {
                // --- CABECERA CON GRADIENTE ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF00B4DB), Color(0xFF0083B0))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 40.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Editar perfil",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // --- CUERPO DE LA PANTALLA ---
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    // Tarjeta Nombre
                    SeccionCampoEdicion(
                        titulo = "Nombre",
                        valor = nombre,
                        onValueChange = { nombre = it },
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tarjeta Email
                    SeccionCampoEdicion(
                        titulo = "Correo electrónico",
                        valor = email,
                        onValueChange = { email = it },
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tarjeta Avatar (deshabilitada)
                    SeccionCampoEdicion(
                        titulo = "Avatar (no editable)",
                        valor = "",
                        onValueChange = { /* No hacer nada */ },
                        enabled = false
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- BOTÓN GUARDAR ---
                    Button(
                        onClick = {
                            viewModel.updateProfile(nombre, email) {
                                onSaveComplete()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D0D2B),
                            disabledContainerColor = Color.Gray
                        ),
                        enabled = !isLoading && nombre.isNotBlank() && email.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Guardar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            // --- MANEJO DE ERRORES (SNACKBAR) ---
            if (error != null) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    containerColor = Color(0xFFB00020), // Rojo error
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Text(text = error!!)
                }
            }
        }
    }
}

@Composable
fun SeccionCampoEdicion(
    titulo: String,
    valor: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = titulo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = valor,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F3F4),
                    unfocusedContainerColor = Color(0xFFF1F3F4),
                    disabledContainerColor = Color(0xFFE0E0E0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    disabledTextColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }
    }
}