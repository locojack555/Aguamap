package cat.copernic.aguamap1.presentation.categories.components

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
import androidx.compose.ui.res.stringResource // IMPORTANTE
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.ui.theme.AzulClaro
import cat.copernic.aguamap1.ui.theme.AzulGrisaceo
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Gris
import cat.copernic.aguamap1.ui.theme.GrisClaro
import coil.compose.AsyncImage

@Composable
fun CategoryItem(category: Category, count: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp) // Añadido un pequeño margen para que no respiren pegadas
            .clickable(
                onClickLabel = stringResource(R.string.expand_info), // CORREGIDO: Indica qué hará el botón
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
            AsyncImage(
                model = category.imageUrl,
                // CORREGIDO: Accesibilidad contextual
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
                Text(
                    text = category.name,
                    fontWeight = FontWeight.Bold,
                    color = AzulGrisaceo,
                    fontSize = 16.sp,
                    maxLines = 1 // Evita que nombres muy largos rompan el diseño
                )

                Text(
                    // Aquí ya lo hacías perfecto: inyección de %d
                    text = stringResource(R.string.category_item_count, count),
                    fontSize = 13.sp,
                    color = Gris
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null, // Visual solamente
                tint = GrisClaro,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}