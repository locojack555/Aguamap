package cat.copernic.aguamap1.aplication.fountain.addFountain

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.aplication.fountain.addFountain.components.FountainCategorySection
import cat.copernic.aguamap1.aplication.fountain.addFountain.components.FountainHeader
import cat.copernic.aguamap1.aplication.fountain.addFountain.components.FountainImageSection
import cat.copernic.aguamap1.aplication.fountain.addFountain.components.FountainLocationSection
import cat.copernic.aguamap1.aplication.utils.rememberImagePickerHelper
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.NegroSuave
import cat.copernic.aguamap1.ui.theme.Rojo
import cat.copernic.aguamap1.ui.theme.Verde

/**
 * Pantalla principal para añadir o editar una fuente.
 * Utiliza un ViewModel para gestionar el estado del formulario y la lógica de negocio.
 */
@Composable
fun AddFountainScreen(
    onDismiss: () -> Unit,             // Función para cerrar la pantalla
    latitude: Double,                  // Latitud inicial (si se abre desde el mapa)
    longitude: Double,                 // Longitud inicial
    viewModel: AddFountainViewModel,   // ViewModel inyectado
    onFountainCreated: () -> Unit      // Callback para ejecutar tras éxito
) {
    // Determina si estamos en modo edición o creación basándose en si existe un objeto cargado
    val isEditing = viewModel.fountainToEdit != null

    // Helper personalizado para gestionar la selección de imágenes de la galería
    val imagePickerHelper = rememberImagePickerHelper { uri ->
        viewModel.updateSelectedImage(uri)
    }

    // Efecto que sincroniza el URI de la imagen seleccionada con el estado del ViewModel
    LaunchedEffect(imagePickerHelper.selectedImageUri) {
        viewModel.updateSelectedImage(imagePickerHelper.selectedImageUri)
    }

    // Gestiona el comportamiento del botón "Atrás" físico o de sistema
    BackHandler {
        viewModel.closeAddFountain()
        onDismiss()
    }

    // Contenedor principal de la interfaz
    Surface(modifier = Modifier.fillMaxSize(), color = Blanco) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Evita que el contenido quede debajo de la barra de estado
                .verticalScroll(rememberScrollState()) // Permite scroll si el formulario es largo
        ) {
            // 1. Cabecera personalizada con el botón de cierre (X)
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
                // 2. Títulos dinámicos (Cambian según si es Edición o Creación)
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

                // 3. Sección para seleccionar/mostrar la imagen de la fuente
                FountainImageSection(viewModel, imagePickerHelper)

                // 4. Inputs de Texto: Nombre de la fuente
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    label = { Text(stringResource(R.string.fountain_name)) },
                    placeholder = { Text(stringResource(R.string.fountain_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Negro,
                        unfocusedBorderColor = Negro,
                        focusedTextColor = Negro
                    )
                )

                // 4. Inputs de Texto: Descripción (Multilínea)
                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.description = it },
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

                OutlinedTextField(
                    value = viewModel.moreInformation,
                    onValueChange = { viewModel.moreInformation = it },
                    label = { Text("Más información") },
                    placeholder = { Text("Horario, accesibilidad, notas...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
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

                // 7. Estado Operativo: Switch visual con feedback de color
                Surface(
                    color = if (viewModel.isOperational) Verde.copy(alpha = 0.1f) else Rojo.copy(
                        alpha = 0.1f
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.fountain_operational_status),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Negro
                            )
                            Text(
                                text = if (viewModel.isOperational) stringResource(R.string.operational_yes)
                                else stringResource(R.string.operational_no),
                                fontSize = 12.sp,
                                color = if (viewModel.isOperational) Verde else Rojo
                            )
                        }
                        // Interruptor para cambiar el estado de funcionamiento
                        Switch(
                            checked = viewModel.isOperational,
                            onCheckedChange = { viewModel.isOperational = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Blanco,
                                checkedTrackColor = Verde
                            )
                        )
                    }
                }

                // 8. Botón de Acción Principal (Guardar / Actualizar)
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
                    enabled = !viewModel.isUploading && viewModel.isFormValid
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
}