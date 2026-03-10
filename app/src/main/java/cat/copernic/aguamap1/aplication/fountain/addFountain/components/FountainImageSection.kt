package cat.copernic.aguamap1.aplication.fountain.addFountain.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.aplication.fountain.addFountain.AddFountainViewModel
import cat.copernic.aguamap1.aplication.utils.ImagePickerHelper
import cat.copernic.aguamap1.ui.theme.*
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Sección del formulario para la gestión de la imagen de la fuente.
 * * @param viewModel ViewModel que gestiona el estado de la imagen y el progreso de subida.
 * @param imagePickerHelper Utilidad para la selección de imágenes desde la galería o cámara.
 */
@Composable
fun FountainImageSection(
    viewModel: AddFountainViewModel,
    imagePickerHelper: ImagePickerHelper
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        /**
         * Etiqueta de la sección de fotografía.
         */
        Text(
            text = stringResource(R.string.add_fountain_photo_label),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Negro
        )

        /**
         * Contenedor visual para la previsualización o selección de imagen.
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(NegroMinimal, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            val displayImage = viewModel.selectedImageUri ?: viewModel.currentImageUrl

            if (displayImage != null && displayImage != "") {
                /**
                 * Renderizado de la imagen seleccionada o actual.
                 */
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(displayImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.add_fountain_preview),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                /**
                 * Botón para eliminar la imagen seleccionada.
                 */
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
                    Icon(Icons.Default.Close, null, tint = Rojo)
                }

                /**
                 * Overlay de carga con progreso porcentual durante la subida a la nube.
                 */
                if (viewModel.isUploading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Negro.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Blanco)
                            Text("${viewModel.uploadProgress}%", color = Blanco, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                /**
                 * Estado vacío que solicita al usuario la selección de una fotografía.
                 */
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(painterResource(R.drawable.pin_lleno), null, tint = GrisClaro, modifier = Modifier.size(48.dp))
                    Button(
                        onClick = { imagePickerHelper.showPickerOptions() },
                        colors = ButtonDefaults.buttonColors(containerColor = Blue10)
                    ) {
                        Text(stringResource(R.string.add_fountain_select_photo), color = Blanco)
                    }
                }
            }
        }
    }
}