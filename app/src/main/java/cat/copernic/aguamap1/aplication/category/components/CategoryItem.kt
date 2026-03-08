package cat.copernic.aguamap1.aplication.category.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.category.Category
import cat.copernic.aguamap1.ui.theme.AzulClaro
import cat.copernic.aguamap1.ui.theme.AzulGrisaceo
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Gris
import cat.copernic.aguamap1.ui.theme.GrisClaro
import coil.compose.AsyncImage

/**
 * Representa un elemento de lista individual para una categoría de fuentes.
 * * Presenta visualmente la imagen representativa, el nombre y el recuento de fuentes activas.
 * * Implementa semántica de accesibilidad para lectores de pantalla, identificándose como un botón.
 *
 * @param category Objeto de dominio con los datos de la categoría (nombre, URL de imagen).
 * @param count El número total de fuentes que pertenecen a esta categoría para mostrar al usuario.
 * @param onClick Acción a ejecutar cuando se pulsa el elemento, normalmente para abrir el detalle.
 */
@Composable
fun CategoryItem(category: Category, count: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(
                onClickLabel = stringResource(R.string.expand_info),
                onClick = onClick
            )
            .semantics { role = Role.Button },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de la categoría con carga asíncrona mediante Coil
            AsyncImage(
                model = category.imageUrl,
                contentDescription = category.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AzulClaro),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.gota)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Nombre de la categoría
                Text(
                    text = category.name,
                    fontWeight = FontWeight.Bold,
                    color = AzulGrisaceo,
                    fontSize = 16.sp,
                    maxLines = 1
                )

                // Texto informativo con la cantidad de fuentes (inyecta %d del recurso string)
                Text(
                    text = stringResource(R.string.category_item_count, count),
                    fontSize = 13.sp,
                    color = Gris
                )
            }

            // Indicador visual de navegación (chevron)
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = GrisClaro,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}