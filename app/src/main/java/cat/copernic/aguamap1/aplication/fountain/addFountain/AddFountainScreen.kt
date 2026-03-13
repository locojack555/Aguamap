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
import androidx.compose.material3.OutlinedButton
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
import cat.copernic.aguamap1.aplication.utils.ImagePickerHelper
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
    // En lugar del Column con tod0, usamos when:
    when (viewModel.currentStep) {
        1 -> PasoUno(
            viewModel = viewModel,
            imagePickerHelper = imagePickerHelper,
            onDismiss = onDismiss,
            onNext    = { viewModel.goToStep2() }
        )
        2 -> PasoDos(
            viewModel       = viewModel,
            onBack          = { viewModel.goToStep1() },
            onFountainCreated = onFountainCreated,
            onDismiss       = onDismiss
        )
    }
}

@Composable
fun PasoUno(
    viewModel: AddFountainViewModel,
    onDismiss: () -> Unit,
    imagePickerHelper: ImagePickerHelper,
    onNext: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Blanco) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            FountainHeader(onDismiss = { viewModel.closeAddFountain(); onDismiss() })
            Text("Paso 1 de 2 — Información básica", fontWeight = FontWeight.Bold)

            // Secciones que ya existen:
            FountainImageSection(viewModel, imagePickerHelper)

            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("Nombre de la fuente") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(), minLines = 3
            )

            OutlinedTextField(
                value = viewModel.caudal.toString(),
                onValueChange = { viewModel.updateCaudal(it.toIntOrNull() ?: 0)},
                label = { Text("Caudal (Litros/min)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Botón siguiente — solo si nombre y descripción no están vacíos
            Button(
                onClick = { onNext() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = viewModel.name.isNotBlank() && viewModel.description.isNotBlank()
            ) { Text("Continuar →") }
        }
    }
}

@Composable
fun PasoDos(
    viewModel: AddFountainViewModel,
    onBack: () -> Unit,
    onFountainCreated: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Blanco) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FountainHeader(onDismiss = { viewModel.closeAddFountain(); onDismiss() })
            Text("Paso 2 de 2 — Categoría, ubicación y estado", fontWeight = FontWeight.Bold)

            // Secciones que ya existen:
            FountainCategorySection(viewModel)

            // Secciones que ya existen:
            FountainLocationSection(viewModel)

            // Interruptor para cambiar el estado de funcionamiento
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

            // Botones atrás / guardar
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onBack() },
                    modifier = Modifier.weight(1f).height(56.dp)
                ) { Text("← Atrás") }

                Button(
                    onClick = { viewModel.saveFountain { onFountainCreated(); onDismiss() } },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = viewModel.isFormValid
                ) { Text("Crear fuente") }
            }
        }
    }
}

@Composable
fun PasoTres(
    viewModel: AddFountainViewModel,
    onBack: () -> Unit,
    onFountainCreated: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Blanco) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FountainHeader(onDismiss = { viewModel.closeAddFountain(); onDismiss() })
            Text("Paso 3 de 3 — Ubicación", fontWeight = FontWeight.Bold)

            // Secciones que ya existen:
            FountainLocationSection(viewModel)

            // Interruptor para cambiar el estado de funcionamiento
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

            // Botones atrás / guardar
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onBack() },
                    modifier = Modifier.weight(1f).height(56.dp)
                ) { Text("← Atrás") }

                Button(
                    onClick = { viewModel.saveFountain { onFountainCreated(); onDismiss() } },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = viewModel.isFormValid
                ) { Text("Crear fuente") }
            }
        }
    }
}