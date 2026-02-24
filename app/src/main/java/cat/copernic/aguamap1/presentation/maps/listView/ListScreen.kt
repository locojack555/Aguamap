package cat.copernic.aguamap1.presentation.maps.listView

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.presentation.maps.mapView.MapViewModel
import cat.copernic.aguamap1.presentation.util.getStatusColor
import cat.copernic.aguamap1.presentation.util.getStatusText
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Gris
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.Rojo
import coil.compose.AsyncImage
import java.util.Locale

@Composable
fun ListScreen(
    viewModel: MapViewModel,
    onFountainClick: (Fountain) -> Unit // Añadido para desacoplar del ViewModel
) {
    val state = viewModel.uiState
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Blanco
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Espaciador para la TopBar personalizada
            Spacer(modifier = Modifier.height(72.dp))

            if (state.fountains.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.noth_fountains), color = Negro)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 100.dp // Espacio para botones flotantes
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(state.fountains) { fountain ->
                        FountainItem(
                            fountain = fountain,
                            onClick = { onFountainClick(fountain) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FountainItem(fountain: Fountain, onClick: () -> Unit) {
    val themeColor = fountain.getStatusColor()
    val statusLabel = fountain.getStatusText()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(IntrinsicSize.Min)
        ) {
            // Imagen de la fuente
            AsyncImage(
                model = fountain.imageUrl,
                contentDescription = null,
                placeholder = painterResource(R.drawable.pin_lleno),
                error = painterResource(R.drawable.pin_lleno),
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = fountain.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Negro,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = fountain.description,
                    fontSize = 13.sp,
                    color = Gris,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // --- ESTRELLAS DINÁMICAS ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val rating = fountain.ratingAverage
                    repeat(5) { index ->
                        val isFilled = index < rating.toInt()
                        Icon(
                            imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (isFilled) Color(0xFFFFC107) else Gris.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = " ${
                            String.format(
                                Locale.US,
                                "%.1f",
                                rating
                            )
                        } (${fountain.totalRatings})",
                        fontSize = 12.sp,
                        color = Gris,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Distancia
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.pin_lleno),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Gris
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val distanceText = fountain.distanceFromUser?.let {
                            if (it < 1000) "${it.toInt()}m"
                            else String.format(Locale.US, "%.1fkm", it / 1000.0)
                        } ?: "---"
                        Text(
                            text = distanceText,
                            fontSize = 14.sp,
                            color = Negro
                        )
                    }

                    // Etiqueta de Estado (Funcionando/Averiada/etc)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, themeColor),
                        color = Blanco
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.gota),
                                contentDescription = null,
                                tint = if (!fountain.operational) Rojo else themeColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = statusLabel,
                                color = themeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}