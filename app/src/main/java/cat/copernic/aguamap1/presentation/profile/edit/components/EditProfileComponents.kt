package cat.copernic.aguamap1.presentation.profile.edit.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.util.AzulAgua
import cat.copernic.aguamap1.ui.theme.*
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ProfileImageSection(
    imageToShow: Any?,
    isLocalSelection: Boolean,
    isUploading: Boolean,
    progress: Int,
    onAddPhoto: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteExisting: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth() // Aseguramos que la columna ocupe todo el ancho para poder centrar
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Centra los hijos horizontalmente
        ) {
            // Título alineado a la izquierda (Start)
            Text(
                text = stringResource(R.string.edit_profile_photo_label),
                fontWeight = FontWeight.Bold,
                color = AzulOscuro,
                modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho para que el align(Start) funcione
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contenedor de la foto (Box)
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(GrisClaro, CircleShape)
                    .clickable { onAddPhoto() }
                    .border(2.dp, AzulClaro.copy(alpha = 0.3f), CircleShape), // Un borde sutil si está vacío
                contentAlignment = Alignment.Center
            ) {
                if (imageToShow != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageToShow)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .border(3.dp, AzulClaro, CircleShape),
                        error = painterResource(R.drawable.ic_placeholder)
                    )

                    // Botón de cerrar (Solo si es selección local)
                    if (isLocalSelection) {
                        Surface(
                            onClick = onClearSelection,
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }

                    // Overlay de carga
                    if (isUploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(36.dp),
                                    strokeWidth = 3.dp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "$progress%",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    // Estado vacío (Placeholder)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = AzulClaro,
                            modifier = Modifier.size(70.dp)
                        )
                        Text(
                            text = stringResource(R.string.edit_profile_tap_to_add),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Texto de ayuda centrado (por el horizontalAlignment de la Column)
            Text(
                text = stringResource(R.string.edit_profile_tap_to_change),
                fontSize = 12.sp,
                color = Color.Gray
            )

            // Botón de borrar si existe foto previa
            if (imageToShow != null && !isLocalSelection && !isUploading) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDeleteExisting,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.edit_profile_delete_photo), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ProfileNameSection(value: String, onValueChange: (String) -> Unit, enabled: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.edit_profile_name_label), fontWeight = FontWeight.SemiBold, color = AzulOscuro)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Negro, unfocusedBorderColor = Negro, focusedTextColor = Negro)
            )
        }
    }
}

@Composable
fun EditProfileBottomBar(isEnabled: Boolean, isLoading: Boolean, onSave: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(GrisClaro).padding(16.dp)) {
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = isEnabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulAgua)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.edit_profile_save_btn))
            }
        }
    }
}