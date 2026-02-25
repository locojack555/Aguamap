package cat.copernic.aguamap1.presentation.fountain.addFountain

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.util.rememberImagePickerHelper
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.GrisClaro
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.NegroMinimal
import cat.copernic.aguamap1.ui.theme.NegroSuave
import cat.copernic.aguamap1.ui.theme.Rojo
import cat.copernic.aguamap1.ui.theme.Verde
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddFountainScreen(
    onDismiss: () -> Unit,
    latitude: Double,
    longitude: Double,
    viewModel: AddFountainViewModel,
    onFountainCreated: () -> Unit
) {
    val context = LocalContext.current
    val isEditing = viewModel.fountainToEdit != null

    // Usar el helper para imágenes
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    viewModel.closeAddFountain()
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cancel),
                        tint = Negro
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Títulos dinámicos
                Column {
                    Text(
                        text = if (isEditing) stringResource(R.string.edit_fountain_title) else stringResource(
                            R.string.add_fountain_title
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = Negro
                    )
                    Text(
                        text = if (isEditing) "Modifica la información de la fuente" else stringResource(
                            R.string.add_fountain_subtitle
                        ),
                        fontSize = 14.sp,
                        color = NegroSuave
                    )
                }

                // --- SECCIÓN DE IMAGEN ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.add_fountain_photo_label),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Negro
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(NegroMinimal, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Mostrar imagen nueva o la que ya existe en Firestore
                        val displayImage = viewModel.selectedImageUri ?: viewModel.currentImageUrl

                        if (displayImage != null && displayImage != "") {
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
                                    .padding(8.dp)
                                    .background(Blanco, RoundedCornerShape(50))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Rojo)
                            }

                            if (viewModel.isUploading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Negro.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = Blanco)
                                        Text(
                                            "${viewModel.uploadProgress}%",
                                            color = Blanco,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.pin_lleno),
                                    contentDescription = null,
                                    tint = GrisClaro,
                                    modifier = Modifier.size(48.dp)
                                )
                                Button(
                                    onClick = { imagePickerHelper.showPickerOptions() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Blue10)
                                ) {
                                    Text(
                                        stringResource(R.string.add_fountain_select_photo),
                                        color = Blanco
                                    )
                                }
                            }
                        }
                    }
                }

                // Input Nombre
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    label = { Text(stringResource(R.string.fountain_name)) },
                    placeholder = { Text(stringResource(R.string.fountain_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Negro,
                        unfocusedTextColor = Negro,
                        focusedBorderColor = Negro,
                        unfocusedBorderColor = Negro
                    )
                )

                // Input Descripción
                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.description = it },
                    label = { Text(stringResource(R.string.fountain_description)) },
                    placeholder = { Text(stringResource(R.string.fountain_description_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Negro,
                        unfocusedTextColor = Negro,
                        focusedBorderColor = Negro,
                        unfocusedBorderColor = Negro
                    )
                )

                // Categorías
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.category_label),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Negro
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.categories.forEach { category ->
                            FilterChip(
                                selected = viewModel.selectedCategory?.id == category.id,
                                onClick = { viewModel.selectedCategory = category },
                                label = { Text(category.name) },
                                leadingIcon = if (viewModel.selectedCategory?.id == category.id) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }

                // Switch Estado
                Surface(
                    color = NegroMinimal,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.fountain_is_operational_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Black
                            )
                            Text(
                                text = if (viewModel.isOperational) stringResource(R.string.fountain_status_ok) else stringResource(
                                    R.string.fountain_status_error
                                ),
                                fontSize = 12.sp,
                                color = if (viewModel.isOperational) Verde else Rojo
                            )
                        }
                        Switch(
                            checked = viewModel.isOperational,
                            onCheckedChange = { viewModel.isOperational = it })
                    }
                }

                if (viewModel.errorMessage != null) {
                    Text(text = viewModel.errorMessage!!, color = Rojo, fontSize = 14.sp)
                }

                // Botón Enviar / Guardar
                Button(
                    onClick = { viewModel.saveFountain(onSuccess = onFountainCreated) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue10,
                        disabledContainerColor = NegroMinimal
                    ),
                    enabled = viewModel.isFormValid && !viewModel.isUploading
                ) {
                    if (viewModel.isUploading) {
                        CircularProgressIndicator(color = Blanco, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isEditing) stringResource(R.string.save_changes) else stringResource(
                                R.string.send_proposal
                            ),
                            color = Blanco, fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}