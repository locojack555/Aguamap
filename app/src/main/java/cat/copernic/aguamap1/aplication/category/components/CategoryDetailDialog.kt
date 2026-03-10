package cat.copernic.aguamap1.aplication.category.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.category.Category
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.ui.theme.AzulClaro
import cat.copernic.aguamap1.ui.theme.AzulGrisaceo
import cat.copernic.aguamap1.ui.theme.AzulOscuro
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Gris
import cat.copernic.aguamap1.ui.theme.GrisClaro
import cat.copernic.aguamap1.ui.theme.GrisOscuro
import cat.copernic.aguamap1.ui.theme.InfoBlue
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Rojo
import coil.compose.AsyncImage

/**
 * Renderiza un cuadro de diálogo modal que muestra la información detallada de una categoría
 * y la lista de fuentes asociadas a la misma.
 */
@Composable
fun CategoryDetailDialog(
    category: Category,
    fountains: List<Fountain>,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onDeleteCategory: () -> Unit,
    onEditCategory: () -> Unit,
    onFountainClick: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Blanco,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = category.imageUrl,
                        contentDescription = category.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.gota)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = category.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AzulOscuro,
                        modifier = Modifier.weight(1f)
                    )
                    if (isAdmin) {
                        IconButton(onClick = onEditCategory) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = InfoBlue
                            )
                        }
                        IconButton(onClick = onDeleteCategory) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = Rojo
                            )
                        }
                    }
                }

                if (category.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = category.description, color = GrisOscuro, fontSize = 14.sp)
                }

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = GrisClaro
                )

                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    if (fountains.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.category_no_fountains),
                                color = Gris,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        items(fountains) { fountain ->
                            FountainRow(
                                fountain = fountain,
                                onClick = { onFountainClick(fountain.id) })
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(stringResource(R.string.category_close), color = Blanco) }
            }
        }
    }
}

/**
 * Componente interno que representa una fila simplificada con Nombre, Valoración y Distancia.
 */
@Composable
private fun FountainRow(fountain: Fountain, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(AzulClaro, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.gota),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                fountain.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AzulGrisaceo
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // VALORACIÓN
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Naranja,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "%.1f".format(fountain.ratingAverage),
                    fontSize = 11.sp,
                    color = GrisOscuro,
                    modifier = Modifier.padding(start = 2.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // DISTANCIA (Si está disponible)
                fountain.distanceFromUser?.let { dist ->
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = InfoBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    val distanceText = if (dist < 1000) "${dist.toInt()}m"
                    else "%.1fkm".format(dist / 1000)
                    Text(
                        text = distanceText,
                        fontSize = 11.sp,
                        color = GrisOscuro,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            if (!fountain.operational) {
                Text(
                    stringResource(R.string.legend_averiada),
                    color = Rojo,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}