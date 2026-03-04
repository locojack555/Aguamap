package cat.copernic.aguamap1.presentation.fountain.addFountain.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.fountain.addFountain.AddFountainViewModel
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro

/**
 * Sección del formulario de "Añadir Fuente" dedicada a la selección de categorías.
 * * Utiliza un diseño de chips fluidos ([FlowRow]) que se adaptan automáticamente al ancho
 * de la pantalla, ideal para mostrar múltiples opciones de categorías sin desperdiciar espacio.
 *
 * @param viewModel Instancia de [AddFountainViewModel] que provee la lista de categorías
 * disponibles y gestiona el estado de la categoría actualmente seleccionada.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FountainCategorySection(viewModel: AddFountainViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Título descriptivo de la sección
        Text(
            text = stringResource(R.string.category_title),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Negro
        )

        // Contenedor que permite que los chips salten de línea si no caben en una sola fila
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.categories.forEach { categoryItem ->
                // Lógica de estado inmutable: verifica si este chip coincide con la selección del VM
                val isSelected = viewModel.selectedCategory?.id == categoryItem.id

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        // Alterna la selección: si ya está seleccionado, lo desmarca (null)
                        viewModel.selectedCategory = if (isSelected) null else categoryItem
                    },
                    label = { Text(categoryItem.name) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Blue10,
                        selectedLabelColor = Blanco,
                        selectedLeadingIconColor = Blanco
                    )
                )
            }
        }
    }
}