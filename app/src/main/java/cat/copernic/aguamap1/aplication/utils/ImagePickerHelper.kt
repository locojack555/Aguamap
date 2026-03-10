package cat.copernic.aguamap1.aplication.utils

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clase de ayuda para gestionar la selección de imágenes desde la cámara o galería.
 * Mantiene el estado del URI seleccionado y la visibilidad del diálogo de opciones.
 */
class ImagePickerHelper(
    val onImageSelected: (Uri?) -> Unit
) {
    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    var showOptionsDialog by mutableStateOf(false)
        private set

    private var cameraUri: Uri? = null

    fun showPickerOptions() {
        showOptionsDialog = true
    }

    fun hideDialog() {
        showOptionsDialog = false
    }

    fun setImageFromGallery(uri: Uri?) {
        selectedImageUri = uri
        onImageSelected(uri)
        hideDialog()
    }

    fun setImageFromCamera(uri: Uri?) {
        selectedImageUri = uri
        cameraUri = uri
        onImageSelected(uri)
        hideDialog()
    }

    fun clearSelection() {
        selectedImageUri = null
        cameraUri = null
        onImageSelected(null)
    }

    fun getCameraUri(): Uri? = cameraUri
}

/**
 * Composable que inicializa y recuerda el ImagePickerHelper.
 * Gestiona los Launchers de actividad para obtener contenido y los estados de permisos.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberImagePickerHelper(
    onImageSelected: (Uri?) -> Unit
): ImagePickerHelper {
    val context = LocalContext.current
    val helper = remember { ImagePickerHelper(onImageSelected) }

    // Estado temporal para el URI generado para la cámara
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para seleccionar contenido de la Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        helper.setImageFromGallery(uri)
    }

    // Función interna para crear un archivo temporal donde se guardará la foto de la cámara
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.cacheDir
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    // Launcher para capturar foto con la Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { uri ->
                helper.setImageFromCamera(uri)
            }
        }
        photoUri = null
    }

    // Gestión de permisos mediante Accompanist Permissions
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    // Adaptación de permisos de almacenamiento según la versión de Android (Tiramisu+)
    val storagePermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        rememberPermissionState(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    // Lógica de validación de permisos
    fun checkAndRequestCameraPermissions(onGranted: () -> Unit) {
        if (cameraPermissionState.status.isGranted) {
            onGranted()
        } else {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    fun checkAndRequestStoragePermissions(onGranted: () -> Unit) {
        if (storagePermissionState.status.isGranted) {
            onGranted()
        } else {
            storagePermissionState.launchPermissionRequest()
        }
    }


    // Diálogo de selección: Aparece cuando el usuario solicita cambiar su imagen
    if (helper.showOptionsDialog) {
        Dialog(onDismissRequest = { helper.hideDialog() }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Blanco,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.image_picker_title),
                        fontSize = 20.sp,
                        color = Negro
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // BOTÓN GALERÍA
                        Button(
                            onClick = {
                                checkAndRequestStoragePermissions {
                                    galleryLauncher.launch("image/*")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_gallery),
                                contentDescription = stringResource(R.string.image_picker_gallery_description),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.image_picker_gallery))
                        }

                        // BOTÓN CÁMARA
                        Button(
                            onClick = {
                                checkAndRequestCameraPermissions {
                                    val file = createImageFile()
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    photoUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_camera),
                                contentDescription = stringResource(R.string.image_picker_camera_description),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.image_picker_camera))
                        }
                    }
                }
            }
        }
    }

    return helper
}