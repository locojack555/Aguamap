package cat.copernic.aguamap1.presentation.profile.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.profile.ProfileViewModel
import cat.copernic.aguamap1.presentation.profile.edit.components.*
import cat.copernic.aguamap1.presentation.util.rememberImagePickerHelper
import cat.copernic.aguamap1.ui.theme.*

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
            EditProfileBottomBar(
                isEnabled = (selectedImageUri != null || nombre != profileState.userName) && !isSaving && !isUploadingPicture,
                isLoading = isSaving || isUploadingPicture,
                onSave = {
                    when {
                        selectedImageUri != null && nombre != profileState.userName -> {
                            viewModel.saveProfilePicture { viewModel.updateProfile(nombre) {} }
                        }
                        selectedImageUri != null -> viewModel.saveProfilePicture {}
                        nombre != profileState.userName -> viewModel.updateProfile(nombre) {}
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(GrisClaro)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Sección Foto
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

                Spacer(modifier = Modifier.height(12.dp))

                // Sección Nombre
                ProfileNameSection(
                    value = nombre,
                    onValueChange = { nombre = it },
                    enabled = !isSaving && !isUploadingPicture
                )
            }

            // Snackbar de Error
            errorResId?.let { id ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
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

@Composable
private fun EditProfileHeader(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Brush.verticalGradient(colors = listOf(AzulClaro, AzulOscuro)))
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
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