package cat.copernic.aguamap1.aplication.category.components

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
import cat.copernic.aguamap1.aplication.category.CategoryViewModel
import cat.copernic.aguamap1.aplication.utils.rememberImagePickerHelper
import cat.copernic.aguamap1.ui.theme.AzulOscuro
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Gris
import cat.copernic.aguamap1.ui.theme.NegroMinimal
import cat.copernic.aguamap1.ui.theme.Rojo
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Renderiza un diálogo interactivo para la gestión (alta/edición) de categorías.
 * * Gestiona de forma integrada:
 * 1. Selección de imágenes locales mediante un helper de galería.
 * 2. Visualización de progreso de subida a la nube (Cloudinary).
 * 3. Validación de campos de texto obligatorios (nombre y descripción).
 * 4. Control de estados de carga para evitar acciones duplicadas durante la persistencia.
 *
 * @param title El encabezado del diálogo (ej: "Nueva Categoría" o "Editar Categoría").
 * @param viewModel El [CategoryViewModel] que mantiene el estado temporal de los campos.
 * @param onDismiss Callback para cancelar la operación y cerrar el diálogo.
 * @param onConfirm Callback para disparar la lógica de guardado en el repositorio.
 */
@Composable
fun CategoryFormDialog(
    title: String,
    viewModel: CategoryViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current

    // Helper personalizado para abstraer la complejidad de los contratos de la galería/cámara
    val imagePickerHelper = rememberImagePickerHelper { uri ->
        viewModel.updateSelectedImage(uri)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = AzulOscuro) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // --- SECCIÓN DE IMAGEN ---
                // Espacio dedicado a la previsualización de la imagen seleccionada o actual
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
                            contentDescription = stringResource(R.string.add_fountain_preview),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Botón para limpiar la selección actual
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
                                contentDescription = stringResource(R.string.add_fountain_remove_image),
                                tint = Rojo,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        // Estado vacío: incita a seleccionar una imagen
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

                // Feedback visual de la subida a Cloudinary
                if (viewModel.isUploading) {
                    LinearProgressIndicator(
                        progress = { viewModel.uploadProgress / 100f },
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
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.description = it },
                    label = { Text(stringResource(R.string.category_form_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                // Visualización de errores de validación o red
                viewModel.errorMessage?.let {
                    Text(it, color = Rojo, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                // El botón solo es accionable si hay un nombre y no hay una subida en curso
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

/*
package cat.copernic.aguamap1.aplication.category.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.aplication.category.CategoryViewModel
import cat.copernic.aguamap1.aplication.utils.rememberImagePickerHelper
import cat.copernic.aguamap1.ui.theme.AzulOscuro
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Gris
import cat.copernic.aguamap1.ui.theme.NegroMinimal
import cat.copernic.aguamap1.ui.theme.Rojo
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Formulario de categoría PARTIDO EN 2 PASOS:
 *  - Paso 1: Imagen de la categoría
 *  - Paso 2: Nombre y descripción
 */
@Composable
fun CategoryFormDialog(
    title: String,
    viewModel: CategoryViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current

    // Estado local que controla en qué paso estamos (0 = Imagen, 1 = Datos)
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 2

    val imagePickerHelper = rememberImagePickerHelper { uri ->
        viewModel.updateSelectedImage(uri)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = AzulOscuro)
                // Indicador visual del paso actual
                Text(
                    text = "Paso ${currentStep + 1} de $totalSteps",
                    fontSize = 12.sp,
                    color = Gris
                )
                // Barra de progreso de pasos
                LinearProgressIndicator(
                    progress = { (currentStep + 1).toFloat() / totalSteps },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    color = AzulOscuro
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                when (currentStep) {

                    // ─── PASO 1: IMAGEN ───────────────────────────────────────
                    0 -> {
                        Text(
                            text = stringResource(R.string.category_form_select_image),
                            fontWeight = FontWeight.SemiBold,
                            color = AzulOscuro
                        )
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
                                    contentDescription = stringResource(R.string.add_fountain_preview),
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
                                        contentDescription = stringResource(R.string.add_fountain_remove_image),
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
                                progress = { viewModel.uploadProgress / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = Blue10
                            )
                        }
                    }

                    // ─── PASO 2: NOMBRE Y DESCRIPCIÓN ────────────────────────
                    1 -> {
                        Text(
                            text = "Datos de la categoría",
                            fontWeight = FontWeight.SemiBold,
                            color = AzulOscuro
                        )
                        OutlinedTextField(
                            value = viewModel.name,
                            onValueChange = { viewModel.name = it },
                            label = { Text(stringResource(R.string.category_form_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
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
                }
            }
        },
        confirmButton = {
            // En el último paso: Guardar. En los demás: Siguiente
            if (currentStep < totalSteps - 1) {
                Button(
                    onClick = { currentStep++ },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro)
                ) {
                    Text("Siguiente", color = Blanco)
                }
            } else {
                Button(
                    onClick = onConfirm,
                    enabled = viewModel.name.isNotBlank() && !viewModel.isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro)
                ) {
                    Text(stringResource(R.string.category_save), color = Blanco)
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Botón "Anterior" visible solo si no estamos en el primer paso
                if (currentStep > 0) {
                    TextButton(
                        onClick = { currentStep-- },
                        enabled = !viewModel.isUploading
                    ) {
                        Text("Anterior", color = AzulOscuro)
                    }
                }
                TextButton(onClick = onDismiss, enabled = !viewModel.isUploading) {
                    Text(stringResource(R.string.category_cancel), color = Gris)
                }
            }
        }
    )
}
*/