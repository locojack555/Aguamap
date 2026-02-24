package cat.copernic.aguamap1.presentation.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class ImagePicker(
    private val onImageSelected: (Uri?) -> Unit
) {
    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    lateinit var launchPicker: () -> Unit

    fun clearSelection() {
        selectedImageUri = null
        onImageSelected(null)
    }

    fun handleResult(uri: Uri?) {
        selectedImageUri = uri
        onImageSelected(uri)
    }
}

@Composable
fun rememberImagePicker(
    onImageSelected: (Uri?) -> Unit
): ImagePicker {
    val picker = remember {
        ImagePicker(
            onImageSelected = onImageSelected
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        picker.handleResult(uri)
    }

    // Asignamos la función launchPicker después de crear el launcher
    picker.launchPicker = {
        launcher.launch("image/*")
    }

    return picker
}