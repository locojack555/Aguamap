package cat.copernic.aguamap1.presentation.profile

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.presentation.util.rememberImagePickerHelper
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onSaveComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isUploadingPicture by viewModel.isUploadingPicture.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()

    var nombre by remember(profileState.userName) {
        mutableStateOf(profileState.userName)
    }

    val imagePickerHelper = rememberImagePickerHelper { uri ->
        viewModel.updateSelectedImage(uri)
    }

    BackHandler {
        viewModel.clearSelectedImage()
        onBack()
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.resetSuccess()
            viewModel.clearSelectedImage()
            onSaveComplete()
        }
    }

    val azulClaro = Color(0xFF00B4DB)
    val azulOscuro = Color(0xFF0083B0)
    val grisMuyClaro = Color(0xFFF8FAFC)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(grisMuyClaro)
        ) {
            // CABECERA - X al lado del título
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(azulClaro, azulOscuro)
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.clearSelectedImage()
                            onBack()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.config_profile_close_desc),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.edit_profile_title),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Contenido
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // SECCIÓN FOTO
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Foto de perfil",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = azulOscuro,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .clickable { imagePickerHelper.showPickerOptions() }
                                .background(grisMuyClaro, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val imageToShow = when {
                                selectedImageUri != null -> selectedImageUri
                                !profileState.profilePictureUrl.isNullOrBlank() -> profileState.profilePictureUrl
                                else -> null
                            }

                            if (imageToShow != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imageToShow)
                                        .crossfade(true)
                                        .size(Size.ORIGINAL)
                                        .build(),
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(CircleShape)
                                        .border(3.dp, azulClaro, CircleShape),
                                    error = painterResource(id = R.drawable.ic_placeholder)
                                )

                                if (selectedImageUri != null) {
                                    IconButton(
                                        onClick = {
                                            viewModel.clearSelectedImage()
                                            imagePickerHelper.clearSelection()
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(28.dp)
                                            .background(Color.White, CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Eliminar selección",
                                            tint = Color.Red,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                if (isUploadingPicture) {
                                    Box(
                                        modifier = Modifier
                                            .size(140.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(36.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Text(
                                                "$uploadProgress%",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = azulClaro,
                                        modifier = Modifier.size(70.dp)
                                    )
                                    Text(
                                        "Tocar para añadir",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Toca la foto para cambiarla",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        if (!profileState.profilePictureUrl.isNullOrBlank() && selectedImageUri == null && !isUploadingPicture) {
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    viewModel.deleteProfilePicture {}
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.Red
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color.Red, Color.Red.copy(alpha = 0.5f))
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(0.6f)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Eliminar foto", fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SECCIÓN NOMBRE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Nombre",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = azulOscuro
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving && !isUploadingPicture,
                            placeholder = { Text("Tu nombre") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = azulClaro,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.8f),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            // BOTÓN GUARDAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(grisMuyClaro)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        when {
                            selectedImageUri != null && nombre != profileState.userName -> {
                                viewModel.saveProfilePicture {
                                    viewModel.updateProfile(nombre)
                                }
                            }
                            selectedImageUri != null -> {
                                viewModel.saveProfilePicture {}
                            }
                            nombre != profileState.userName -> {
                                viewModel.updateProfile(nombre)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if ((selectedImageUri != null || nombre != profileState.userName) && !isSaving && !isUploadingPicture)
                            azulOscuro
                        else
                            Color.Gray.copy(alpha = 0.3f)
                    ),
                    enabled = (selectedImageUri != null || nombre != profileState.userName) && !isSaving && !isUploadingPicture
                ) {
                    if (isSaving || isUploadingPicture) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Guardar cambios",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        if (error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.95f),
                containerColor = Color(0xFFE74C3C),
                shape = RoundedCornerShape(12.dp),
                action = {
                    TextButton(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("OK", color = Color.White)
                    }
                }
            ) {
                Text(text = error!!, color = Color.White)
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