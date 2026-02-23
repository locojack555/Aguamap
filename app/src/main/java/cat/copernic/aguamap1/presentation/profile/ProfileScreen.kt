package cat.copernic.aguamap1.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.UserRanking

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel(),navigateToLogin: () -> Unit = {}) {
    val estadoScroll = rememberScrollState()
    val profileState by viewModel.profileState.collectAsState()
    val isAdmin = profileState.userRole.equals("ADMIN", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F4))
            .verticalScroll(estadoScroll)
    ) {
        CabeceraPerfil(profileState, isAdmin)

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

            // SECCIÓN CUENTA
            SeccionPerfil(titulo = "Cuenta") {
                ElementoOpcionPerfil(icono = Icons.Default.Edit, etiqueta = "Editar perfil", onClick = navigateToEditProfile)
                DivisorPerfil()
                ElementoOpcionPerfil(icono = Icons.Default.Settings, etiqueta = "Configuración")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECCIÓN ADMIN
            if (isAdmin) {
                SeccionPanelAdmin()
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN CERRAR SESIÓN
            BotonCerrarSesion(onClick = navigateToLogin)

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun CabeceraPerfil(profileState: ProfileState, isAdmin: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00B4DB), Color(0xFF0083B0))
                )
            )
            .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                )

                Spacer(modifier = Modifier.width(20.dp))

                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = profileState.userName,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    if (isAdmin) {
                        // Badge de ADMIN (amarillo)
                        Surface(
                            color = Color(0xFFFFC107),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    profileState.userRole,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Badge de USUARIO (azul)
                        Surface(
                            color = colorResource(id = R.color.azulReal),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    profileState.userRole,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = profileState.userEmail,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TarjetaEstadistica(
                    valor = profileState.fountainsCount.toString(),
                    etiqueta = "fuentes",
                    icono = Icons.Default.LocationOn
                )
                TarjetaEstadistica(
                    valor = profileState.ratingsCount.toString(),
                    etiqueta = "Valoraciones",
                    icono = Icons.Default.StarOutline
                )
                TarjetaEstadistica(
                    valor = profileState.points.toString(),
                    etiqueta = "Puntos",
                    icono = Icons.Outlined.EmojiEvents
                )
            }
        }
    }
}

@Composable
fun TarjetaEstadistica(valor: String, etiqueta: String, icono: ImageVector) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.size(width = 105.dp, height = 90.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Text(valor, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(etiqueta, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

@Composable
fun SeccionPanelAdmin() {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Column {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFFBC02D), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Panel de Administrador", fontWeight = FontWeight.Bold, color = Color(0xFF5D4037), fontSize = 15.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gestionar fuentes", modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Surface(color = Color(0xFFFFF59D), shape = RoundedCornerShape(8.dp)) {
                    Text("2 pendiente de validación", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = Color(0xFF7F6D00))
                }
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
            }

            DivisorPerfil()
            ElementoOpcionPerfil(icono = Icons.Default.EditNote, etiqueta = "Gestionar categorías")
        }
    }
}

@Composable
fun BotonCerrarSesion(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = Color.Red)
            Spacer(Modifier.width(16.dp))
            Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun SeccionPerfil(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = titulo,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            fontWeight = FontWeight.Bold,
            color = Color(0xFF616161),
            fontSize = 14.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(content = contenido)
        }
    }
}

@Composable
fun ElementoOpcionPerfil(icono: ImageVector, etiqueta: String, indicador: String? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clickable { onClick() },  // ← AÑADE clickable AQUÍ
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = Color(0xFF424242), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(etiqueta, modifier = Modifier.weight(1f), fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        if (indicador != null) {
            Surface(color = Color(0xFFECEFF1), shape = CircleShape) {
                Text(indicador, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            }
            Spacer(Modifier.width(8.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(16.dp))
    }
}

@Composable
fun DivisorPerfil() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
}
