package cat.copernic.aguamap1.presentation.categories.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.presentation.categories.CategoryViewModel
import cat.copernic.aguamap1.ui.theme.AzulClaro
import cat.copernic.aguamap1.ui.theme.AzulOscuro
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Gris
import cat.copernic.aguamap1.ui.theme.Rojo

@Composable
fun CategoryFormDialog(
    title: String,
    viewModel: CategoryViewModel,
    launcher: androidx.activity.result.ActivityResultLauncher<String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = AzulOscuro) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Button(
                    onClick = { launcher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulClaro),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AzulOscuro)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (viewModel.selectedImageUri != null) "Imagen Cambiada" else "Seleccionar Imagen",
                        color = AzulOscuro
                    )
                }

                if (viewModel.isUploading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(
                            progress = viewModel.uploadProgress / 100f,
                            modifier = Modifier.fillMaxWidth(),
                            color = Blue10
                        )
                        Text("${viewModel.uploadProgress}%", fontSize = 12.sp, color = Gris)
                    }
                }

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
            ) { Text("Guardar", color = Blanco) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !viewModel.isUploading) {
                Text("Cancelar", color = Gris)
            }
        }
    )
}