package cat.copernic.aguamap1.aplication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.aplication.fountain.addFountain.AddFountainViewModel
import cat.copernic.aguamap1.aplication.fountain.addFountain.components.FountainCategorySection
import cat.copernic.aguamap1.aplication.fountain.addFountain.components.FountainLocationSection
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro

@Composable
fun Practicar(
    viewModel: AddFountainViewModel, // Lo seguimos necesitando para el save y las categorías
    onFountainCreated: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onDismiss: () -> Unit
) {

    val isEditing = viewModel.fountainToEdit != null
    Column {
        Text(
            text = "Nombre: ${viewModel.name}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = viewModel.description,
            onValueChange = {
                viewModel.description = it
            },
            label = { Text(stringResource(R.string.fountain_description)) },
            placeholder = { Text(stringResource(R.string.fountain_description_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Negro,
                unfocusedBorderColor = Negro,
                focusedTextColor = Negro
            )
        )
        // 5. Componente para seleccionar categorías (Ej: Potable, No potable, etc.)
        FountainCategorySection(viewModel)

        // 6. Sección de Ubicación: Muestra coordenadas o permite ajuste manual
        FountainLocationSection(viewModel)
        Row {
            Button(
                onClick = {
                    /*viewModel.saveFountain(
                        onSuccess = {
                            onFountainCreated()
                            onDismiss()
                        }
                    )*/ onNavigateToAdd()
                }
            ) { Text("Volver") }
            Button(
                onClick = {
                    viewModel.saveFountain(
                        onSuccess = {
                            onFountainCreated()
                            onDismiss()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                // Deshabilitado si se está subiendo o si el formulario no cumple requisitos
                //enabled = !viewModel.isUploading && viewModel.isFormValid
            ) {
                // Muestra un indicador de carga mientras se comunica con el servidor
                if (viewModel.isUploading) {
                    CircularProgressIndicator(color = Blanco, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isEditing) stringResource(R.string.btn_update_fountain)
                        else stringResource(R.string.btn_create_fountain),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}