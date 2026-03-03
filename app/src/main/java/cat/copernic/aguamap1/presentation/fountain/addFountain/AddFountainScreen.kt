package cat.copernic.aguamap1.presentation.fountain.addFountain

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.fountain.addFountain.components.*
import cat.copernic.aguamap1.presentation.util.rememberImagePickerHelper
import cat.copernic.aguamap1.ui.theme.*

@Composable
fun AddFountainScreen(
    onDismiss: () -> Unit,
    latitude: Double,
    longitude: Double,
    viewModel: AddFountainViewModel,
    onFountainCreated: () -> Unit
) {
    val isEditing = viewModel.fountainToEdit != null
    val imagePickerHelper = rememberImagePickerHelper { uri ->
        viewModel.updateSelectedImage(uri)
    }

    LaunchedEffect(imagePickerHelper.selectedImageUri) {
        viewModel.updateSelectedImage(imagePickerHelper.selectedImageUri)
    }

    BackHandler {
        viewModel.closeAddFountain()
        onDismiss()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Blanco) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Header (Botón cerrar)
            FountainHeader(onDismiss = {
                viewModel.closeAddFountain()
                onDismiss()
            })

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 2. Títulos
                Column {
                    Text(
                        text = if (isEditing) stringResource(R.string.edit_fountain_title)
                        else stringResource(R.string.add_fountain_title),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = Negro
                    )
                    Text(
                        text = if (isEditing) stringResource(R.string.edit_fountain_subtitle)
                        else stringResource(R.string.add_fountain_subtitle),
                        fontSize = 14.sp,
                        color = NegroSuave
                    )
                }

                // 3. Sección de Imagen
                FountainImageSection(viewModel, imagePickerHelper)

                // 4. Inputs de Texto (Nombre y Descripción)
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    label = { Text(stringResource(R.string.fountain_name)) },
                    placeholder = { Text(stringResource(R.string.fountain_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Negro, unfocusedBorderColor = Negro)
                )

                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.description = it },
                    label = { Text(stringResource(R.string.fountain_description)) },
                    placeholder = { Text(stringResource(R.string.fountain_description_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Negro, unfocusedBorderColor = Negro)
                )

                // 5. Sección de Categorías
                FountainCategorySection(viewModel)

                // 6. Sección de Ubicación (GPS / Manual)
                FountainLocationSection(viewModel)

                // 7. Estado Operativo (Switch funcional)
                Surface(
                    color = if (viewModel.isOperational) Verde.copy(alpha = 0.1f) else Rojo.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.fountain_operational_status), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = if (viewModel.isOperational) stringResource(R.string.operational_yes)
                                else stringResource(R.string.operational_no),
                                fontSize = 12.sp,
                                color = if (viewModel.isOperational) Verde else Rojo
                            )
                        }
                        Switch(
                            checked = viewModel.isOperational,
                            onCheckedChange = { viewModel.isOperational = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Blanco, checkedTrackColor = Verde)
                        )
                    }
                }

                // 8. Botón de Guardado / Actualización
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
                    enabled = !viewModel.isUploading && viewModel.isFormValid
                ) {
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
}