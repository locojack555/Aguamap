package cat.copernic.aguamap1.aplication.fountain.detailFountain.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.BlancoTranslucido
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.Rojo
import coil.compose.AsyncImage

/**
 * Componente visual de cabecera para el detalle de la fuente.
 * Gestiona la visualización de la imagen principal y la capa de acciones de navegación y gestión.
 *
 * @param imageUrl URL de la imagen alojada en la nube (Cloudinary).
 * @param isAdmin Determina si se muestran las acciones de moderación (Borrado/Edición).
 * @param isOwner Determina si el usuario creador puede editar la fuente.
 * @param isPending Indica si la fuente está en estado de validación (permite edición al dueño).
 * @param onBack Callback para retroceder en la navegación.
 * @param onEdit Callback para abrir el formulario de edición.
 * @param onDelete Callback para activar el diálogo de eliminación.
 */
@Composable
fun FountainDetailHeader(
    imageUrl: String,
    isAdmin: Boolean,
    isOwner: Boolean,
    isPending: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(modifier = Modifier
        .height(280.dp)
        .fillMaxWidth()) {
        /**
         * Imagen de fondo de la fuente con carga asíncrona y escala de recorte.
         */
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            placeholder = painterResource(R.drawable.pin_lleno),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        /**
         * Fila de controles superiores con soporte para el área de la barra de estado.
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            /**
             * Botón de retorno con estilo traslúcido.
             */
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(BlancoTranslucido, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Negro)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                /**
                 * Acción de edición: habilitada para admins o dueños si la fuente es pendiente.
                 */
                if (isAdmin || (isOwner && isPending)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.background(BlancoTranslucido, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Blue10)
                    }
                }
                /**
                 * Acción de eliminación: restringida únicamente a administradores.
                 */
                if (isAdmin) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.background(BlancoTranslucido, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Rojo)
                    }
                }
            }
        }
    }
}