package cat.copernic.aguamap1.aplication.profile.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import cat.copernic.aguamap1.aplication.profile.ProfileViewModel
import cat.copernic.aguamap1.aplication.profile.edit.components.EditProfileBottomBar
import cat.copernic.aguamap1.aplication.profile.edit.components.ProfileImageSection
import cat.copernic.aguamap1.aplication.profile.edit.components.ProfileNameSection
import cat.copernic.aguamap1.aplication.utils.rememberImagePickerHelper
import cat.copernic.aguamap1.ui.theme.GrisClaro
import cat.copernic.aguamap1.ui.theme.PerfilGradient

/**
 * Pantalla de edición de perfil.
 * Permite al usuario modificar su nombre y su foto de perfil.
 * Gestiona estados complejos de carga (subida a Storage y guardado en Firestore)
 * y asegura la sincronización de datos con el ViewModel compartido.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onSaveComplete: () -> Unit = {}
) {
    // Suscripción a los flujos de estado del ViewModel
    val profileState by viewModel.profileState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isUploadingPicture by viewModel.isUploadingPicture.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val errorResId by viewModel.errorResId.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()

    // Estado local para el campo de texto (iniciado con el nombre actual del perfil)
    var nombre by remember(profileState.userName) { mutableStateOf(profileState.userName) }

    // Helper para la selección de imágenes (Galería/Cámara)
    val imagePickerHelper = rememberImagePickerHelper { uri ->
        viewModel.updateSelectedImage(uri)
    }


    // Manejo del botón atrás físico del dispositivo
    BackHandler {
        viewModel.clearSelectedImage()
        onBack()
    }

    // Efecto que reacciona al éxito del guardado
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.resetSuccess()
            viewModel.clearSelectedImage()
            onSaveComplete()
        }
    }

    Scaffold(
        topBar = {
            EditProfileHeader(
                onClose = {
                    viewModel.clearSelectedImage()
                    onBack()
                }
            )
        },
        bottomBar = {
            // El botón solo se habilita si hay cambios pendientes y no se está procesando nada
            EditProfileBottomBar(
                isEnabled = (selectedImageUri != null || nombre != profileState.userName) && !isSaving && !isUploadingPicture,
                isLoading = isSaving || isUploadingPicture,
                onSave = {
                    when {
                        // Caso 1: Cambia foto y nombre
                        selectedImageUri != null && nombre != profileState.userName -> {
                            viewModel.saveProfilePicture { viewModel.updateProfile(nombre) {} }
                        }
                        // Caso 2: Solo cambia la foto
                        selectedImageUri != null -> viewModel.saveProfilePicture {}
                        // Caso 3: Solo cambia el nombre
                        nombre != profileState.userName -> viewModel.updateProfile(nombre) {}
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(GrisClaro)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // SECCIÓN: FOTO DE PERFIL
                ProfileImageSection(
                    // Muestra la nueva selección o la URL actual si no hay cambios
                    imageToShow = selectedImageUri ?: profileState.profilePictureUrl,
                    isLocalSelection = selectedImageUri != null,
                    isUploading = isUploadingPicture,
                    progress = uploadProgress,
                    onAddPhoto = { imagePickerHelper.showPickerOptions() },
                    onClearSelection = {
                        viewModel.clearSelectedImage()
                        imagePickerHelper.clearSelection()
                    },
                    onDeleteExisting = { viewModel.deleteProfilePicture {} }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // SECCIÓN: NOMBRE DE USUARIO
                ProfileNameSection(
                    value = nombre,
                    onValueChange = { nombre = it },
                    enabled = !isSaving && !isUploadingPicture
                )
            }

            // FEEDBACK: Snackbar de Error en caso de fallo en Firebase
            errorResId?.let { id ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFFE74C3C),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text(stringResource(R.string.common_ok), color = Color.White)
                        }
                    }
                ) { Text(stringResource(id), color = Color.White) }
            }
        }
    }
}

/**
 * Cabecera personalizada para la pantalla de edición.
 */
@Composable
private fun EditProfileHeader(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(PerfilGradient)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
            }
            Text(
                text = stringResource(R.string.edit_profile_title),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/*
package cat.copernic.aguamap1.aplication.profile.edit

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import cat.copernic.aguamap1.aplication.profile.ProfileViewModel
import cat.copernic.aguamap1.aplication.profile.edit.components.ProfileImageSection
import cat.copernic.aguamap1.aplication.profile.edit.components.ProfileNameSection
import cat.copernic.aguamap1.aplication.utils.rememberImagePickerHelper
import cat.copernic.aguamap1.ui.theme.AzulOscuro
import cat.copernic.aguamap1.ui.theme.GrisClaro
import cat.copernic.aguamap1.ui.theme.PerfilGradient

/**
 * Pantalla de edición de perfil PARTIDA EN 2 PASOS:
 *  - Paso 1: Foto de perfil
 *  - Paso 2: Nombre de usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onSaveComplete: () -> Unit = {}
) {
    val profileState by viewModel.profileState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isUploadingPicture by viewModel.isUploadingPicture.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val errorResId by viewModel.errorResId.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()

    var nombre by remember(profileState.userName) { mutableStateOf(profileState.userName) }

    // Estado local que controla en qué paso estamos (0 = Foto, 1 = Nombre)
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 2

    val imagePickerHelper = rememberImagePickerHelper { uri ->
        viewModel.updateSelectedImage(uri)
    }

    BackHandler {
        if (currentStep > 0) {
            currentStep--   // Retroceder un paso antes de salir
        } else {
            viewModel.clearSelectedImage()
            onBack()
        }
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.resetSuccess()
            viewModel.clearSelectedImage()
            onSaveComplete()
        }
    }

    Scaffold(
        topBar = {
            EditProfileHeader(
                currentStep = currentStep,
                totalSteps = totalSteps,
                onClose = {
                    viewModel.clearSelectedImage()
                    onBack()
                }
            )
        },
        bottomBar = {
            EditProfileStepBottomBar(
                currentStep = currentStep,
                totalSteps = totalSteps,
                isSaving = isSaving || isUploadingPicture,
                canSave = (selectedImageUri != null || nombre != profileState.userName) && !isSaving && !isUploadingPicture,
                onNext = { currentStep++ },
                onPrev = { currentStep-- },
                onSave = {
                    when {
                        selectedImageUri != null && nombre != profileState.userName ->
                            viewModel.saveProfilePicture { viewModel.updateProfile(nombre) {} }
                        selectedImageUri != null -> viewModel.saveProfilePicture {}
                        nombre != profileState.userName -> viewModel.updateProfile(nombre) {}
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(GrisClaro)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                when (currentStep) {

                    // ─── PASO 1: FOTO DE PERFIL ──────────────────────────────
                    0 -> {
                        ProfileImageSection(
                            imageToShow = selectedImageUri ?: profileState.profilePictureUrl,
                            isLocalSelection = selectedImageUri != null,
                            isUploading = isUploadingPicture,
                            progress = uploadProgress,
                            onAddPhoto = { imagePickerHelper.showPickerOptions() },
                            onClearSelection = {
                                viewModel.clearSelectedImage()
                                imagePickerHelper.clearSelection()
                            },
                            onDeleteExisting = { viewModel.deleteProfilePicture {} }
                        )
                    }

                    // ─── PASO 2: NOMBRE DE USUARIO ───────────────────────────
                    1 -> {
                        ProfileNameSection(
                            value = nombre,
                            onValueChange = { nombre = it },
                            enabled = !isSaving && !isUploadingPicture
                        )
                    }
                }
            }

            errorResId?.let { id ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFFE74C3C),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text(stringResource(R.string.common_ok), color = Color.White)
                        }
                    }
                ) { Text(stringResource(id), color = Color.White) }
            }
        }
    }
}

// ─── HEADER CON INDICADOR DE PASOS ──────────────────────────────────────────

@Composable
private fun EditProfileHeader(
    currentStep: Int,
    totalSteps: Int,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PerfilGradient)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
            }
            Text(
                text = stringResource(R.string.edit_profile_title),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
            // Indicador de paso en texto
            Text(
                text = "${currentStep + 1}/$totalSteps",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
        }

        // Barra de progreso de pasos
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / totalSteps },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
    }
}

// ─── BARRA INFERIOR CON NAVEGACIÓN ENTRE PASOS ──────────────────────────────

@Composable
private fun EditProfileStepBottomBar(
    currentStep: Int,
    totalSteps: Int,
    isSaving: Boolean,
    canSave: Boolean,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GrisClaro)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón "Anterior" visible si no estamos en el primer paso
        if (currentStep > 0) {
            Button(
                onClick = onPrev,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Anterior")
            }
        }

        // Botón principal: "Siguiente" o "Guardar" según el paso
        Button(
            onClick = if (currentStep < totalSteps - 1) onNext else onSave,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            enabled = if (currentStep < totalSteps - 1) !isSaving else canSave,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro)
        ) {
            if (isSaving && currentStep == totalSteps - 1) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else if (currentStep < totalSteps - 1) {
                Text("Siguiente")
            } else {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.edit_profile_save_btn))
            }
        }
    }
}
*/