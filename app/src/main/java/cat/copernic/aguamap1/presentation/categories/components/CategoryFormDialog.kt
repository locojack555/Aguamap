package cat.copernic.aguamap1.presentation.categories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.categories.CategoryViewModel
import cat.copernic.aguamap1.presentation.util.rememberImagePickerHelper
import cat.copernic.aguamap1.ui.theme.AzulOscuro
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Gris
import cat.copernic.aguamap1.ui.theme.NegroMinimal
import cat.copernic.aguamap1.ui.theme.Rojo
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun CategoryFormDialog(
    title: String,
    viewModel: CategoryViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current

    // Configuramos el helper igual que en fuentes
    val imagePickerHelper = rememberImagePickerHelper { uri ->
        viewModel.updateSelectedImage(uri)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = AzulOscuro) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // --- SECCIÓN DE IMAGEN ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(NegroMinimal, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val displayImage = viewModel.selectedImageUri ?: viewModel.currentImageUrl

                    if (!displayImage.toString().isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(displayImage)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        IconButton(
                            onClick = {
                                imagePickerHelper.clearSelection()
                                viewModel.clearSelectedImage()
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(32.dp)
                                .background(Blanco, RoundedCornerShape(50))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = Rojo,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = { imagePickerHelper.showPickerOptions() },
                            colors = ButtonDefaults.buttonColors(containerColor = Blue10)
                        ) {
                            Text(
                                stringResource(R.string.category_form_select_image),
                                color = Blanco
                            )
                        }
                    }
                }

                if (viewModel.isUploading) {
                    LinearProgressIndicator(
                        progress = viewModel.uploadProgress / 100f,
                        modifier = Modifier.fillMaxWidth(),
                        color = Blue10
                    )
                }

                // --- CAMPOS DE TEXTO ---
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    label = { Text(stringResource(R.string.category_form_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.description = it },
                    label = { Text(stringResource(R.string.category_form_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                viewModel.errorMessage?.let {
                    Text(it, color = Rojo, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = viewModel.name.isNotBlank() && !viewModel.isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro)
            ) { Text(stringResource(R.string.category_save), color = Blanco) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !viewModel.isUploading) {
                Text(stringResource(R.string.category_cancel), color = Gris)
            }
        }
    )
}