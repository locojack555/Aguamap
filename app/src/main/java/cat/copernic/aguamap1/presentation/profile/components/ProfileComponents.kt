package cat.copernic.aguamap1.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.ProfileState
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun CabeceraPerfil(profileState: ProfileState, isAdmin: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    if (!profileState.profilePictureUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profileState.profilePictureUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = painterResource(id = R.drawable.ic_placeholder)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = profileState.userName,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Surface(
                        color = if (isAdmin) Color(0xFFFFC107) else colorResource(id = R.color.azulReal),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAdmin) Icons.Default.Shield else Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isAdmin) stringResource(R.string.role_admin) else stringResource(R.string.role_user),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
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
                TarjetaEstadistica(profileState.fountainsCount.toString(), stringResource(id = R.string.profile_stat_fountains), Icons.Default.LocationOn)
                TarjetaEstadistica(profileState.ratingsCount.toString(), stringResource(id = R.string.profile_stat_ratings), Icons.Default.StarOutline)
                TarjetaEstadistica(profileState.points.toString(), stringResource(id = R.string.profile_stat_points), Icons.Outlined.EmojiEvents)
            }
        }
    }
}

@Composable
fun SeccionPanelAdmin(
    pendingCommentsCount: Int,
    pendingFountainsCount: Int,
    navigateToModeration: () -> Unit,
    navigateToFountainReports: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Column {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, null, tint = Color(0xFFFBC02D), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = R.string.profile_admin_panel_title), fontWeight = FontWeight.Bold, color = Color(0xFF5D4037), fontSize = 15.sp)
            }

            ElementoOpcionPerfil(Icons.Default.Flag, stringResource(id = R.string.profile_admin_mod_comments), onClick = navigateToModeration)
            DivisorPerfil()
            ElementoOpcionPerfil(Icons.Default.Report, stringResource(R.string.reports_title), onClick = navigateToFountainReports)
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
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Outlined.Logout, null, tint = Color.Red)
            Spacer(Modifier.width(16.dp))
            Text(stringResource(id = R.string.profile_btn_logout), color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun SeccionPerfil(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(titulo, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 8.dp), fontWeight = FontWeight.Bold, color = Color(0xFF616161), fontSize = 14.sp)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp), shape = RoundedCornerShape(16.dp), content = contenido)
    }
}

@Composable
fun ElementoOpcionPerfil(icono: ImageVector, etiqueta: String, indicador: String? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, null, tint = Color(0xFF424242), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(etiqueta, modifier = Modifier.weight(1f), fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(16.dp))
    }
}

@Composable
fun DivisorPerfil() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
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
            Icon(icono, null, tint = Color.White, modifier = Modifier.size(24.dp))
            Text(valor, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(etiqueta, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}