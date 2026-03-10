package cat.copernic.aguamap1.aplication.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.category.Category
import cat.copernic.aguamap1.aplication.map.mapView.MapViewModel
import cat.copernic.aguamap1.aplication.utils.FilterState
import cat.copernic.aguamap1.aplication.utils.SortOption
import cat.copernic.aguamap1.ui.theme.AguaMapGradient
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro

/**
 * Barra de búsqueda superior con filtros integrados y alternancia de vista (Mapa/Lista).
 * * @param isMapView Indica si la vista actual es el mapa para cambiar el icono de alternancia.
 * @param onToggleView Callback para cambiar entre modo mapa y modo lista.
 * @param viewModel Instancia del ViewModel para gestionar el estado de búsqueda y filtros.
 */
@Composable
fun MapTopBar(
    isMapView: Boolean,
    onToggleView: () -> Unit,
    viewModel: MapViewModel
) {
    // Contenedor principal redondeado con sombra para la barra de búsqueda
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        color = Blanco,
        shadowElevation = 6.dp
    ) {
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = {
                Text(
                    text = stringResource(R.string.search_fountains),
                    color = Negro.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.search_24px),
                    contentDescription = null,
                    tint = Negro.copy(alpha = 0.7f)
                )
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    // Botón para abrir/cerrar el menú de filtros
                    IconButton(onClick = { viewModel.toggleFilterMenu() }) {
                        Icon(
                            painter = painterResource(R.drawable.filter_alt_24px),
                            contentDescription = stringResource(R.string.filter),
                            // El color cambia a azul si hay filtros activos (distintos al estado inicial)
                            tint = if (viewModel.filterState != FilterState()) Blue10 else Negro.copy(
                                alpha = 0.7f
                            )
                        )
                    }

                    // Componente que renderiza el menú desplegable de filtros
                    FilterDropDown(
                        expanded = viewModel.showFilterMenu,
                        onDismiss = { viewModel.toggleFilterMenu() },
                        currentState = viewModel.filterState,
                        categories = viewModel.categories,
                        onApplyFilters = {
                            viewModel.updateFilters(it)
                            viewModel.toggleFilterMenu()
                        },
                        // Las opciones de ordenación solo se muestran en la vista de lista
                        showSortOptions = !isMapView
                    )

                    // Botón para alternar entre la vista de mapa y la vista de lista
                    IconButton(onClick = { onToggleView() }) {
                        Icon(
                            painter = painterResource(
                                if (isMapView) R.drawable.format_list_bulleted_24px
                                else R.drawable.map_24px
                            ),
                            contentDescription = null,
                            tint = Negro.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Negro,
                unfocusedTextColor = Negro
            )
        )
    }
}

/**
 * Menú de filtros con estado local temporal y botón de aplicar.
 * Utiliza un estado temporal (tempState) para que el usuario pueda previsualizar cambios
 * antes de aplicarlos definitivamente al ViewModel.
 */
@Composable
fun FilterDropDown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    currentState: FilterState,
    categories: List<Category>,
    onApplyFilters: (FilterState) -> Unit,
    showSortOptions: Boolean
) {
    // Sincroniza el estado temporal con el estado real cada vez que se abre el menú
    var tempState by remember(expanded) { mutableStateOf(currentState) }

    val menuShape = RoundedCornerShape(28.dp)

    // Ajuste del tema local para aplicar bordes redondeados al DropdownMenu
    MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = menuShape)) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .width(340.dp)
                .background(AguaMapGradient, shape = menuShape)
                .border(0.5.dp, Blanco.copy(alpha = 0.2f), menuShape)
                .padding(16.dp)
        ) {
            // --- CABECERA DEL MENÚ ---
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filter_title),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Blanco
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Botón para limpiar todos los filtros (reset al estado inicial)
                    Surface(
                        color = Blue10,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { tempState = FilterState() }
                    ) {
                        Text(
                            text = stringResource(R.string.clear_filters),
                            color = Blanco, fontWeight = FontWeight.Bold, fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    // Botón para aplicar los filtros seleccionados
                    Surface(
                        color = Blue10,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { onApplyFilters(tempState) }
                    ) {
                        Text(
                            text = stringResource(R.string.apply_filters),
                            color = Blanco, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = Blanco.copy(alpha = 0.3f))

            // --- CUERPO DEL MENÚ ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // COLUMNA IZQUIERDA: Categorías y Switch Operacional
                Column(modifier = Modifier.weight(1f)) {
                    FilterSectionTitle(stringResource(R.string.filter_by_category), Blanco)
                    categories.forEach { category ->
                        FilterChip(
                            label = category.name,
                            selected = tempState.selectedCategory?.id == category.id,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = {
                                // Toggle de selección de categoría
                                tempState = tempState.copy(
                                    selectedCategory = if (tempState.selectedCategory?.id == category.id) null else category
                                )
                            }
                        )
                    }

                    // El switch se muestra aquí si estamos en modo lista (showSortOptions es true)
                    if (showSortOptions) {
                        OperationalSwitch(tempState.onlyOperational) {
                            tempState = tempState.copy(onlyOperational = it)
                        }
                    }
                }

                // COLUMNA DERECHA: Rating y Ordenación
                Column(modifier = Modifier.weight(1f)) {
                    FilterSectionTitle(stringResource(R.string.min_rating), Blanco)
                    // Sistema de selección por estrellas
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (star <= tempState.minRating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (star <= tempState.minRating) Blanco else Blanco.copy(
                                    alpha = 0.4f
                                ),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        tempState = tempState.copy(minRating = star.toFloat())
                                    }
                            )
                        }
                    }

                    // Opciones de ordenación (solo para modo lista)
                    if (showSortOptions) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FilterSectionTitle(stringResource(R.string.sort_by), Blanco)

                        SortOption.entries.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { tempState = tempState.copy(sortBy = option) }
                            ) {
                                RadioButton(
                                    selected = tempState.sortBy == option,
                                    onClick = null,
                                    modifier = Modifier.size(28.dp),
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Blanco,
                                        unselectedColor = Blanco.copy(alpha = 0.5f)
                                    )
                                )
                                Text(
                                    text = when (option) {
                                        SortOption.DISTANCE_ASC -> stringResource(R.string.sort_distance_asc)
                                        SortOption.DISTANCE_DESC -> stringResource(R.string.sort_distance_desc)
                                        SortOption.RATING_DESC -> stringResource(R.string.sort_rating_desc)
                                        SortOption.RATING_ASC -> stringResource(R.string.sort_rating_asc)
                                        SortOption.DATE_DESC -> stringResource(R.string.sort_date_desc)
                                        SortOption.DATE_ASC -> stringResource(R.string.sort_date_asc)
                                    },
                                    fontSize = 10.sp, color = Blanco, lineHeight = 12.sp
                                )
                            }
                        }
                    } else {
                        // En modo mapa, el switch operacional se coloca en la columna derecha
                        OperationalSwitch(tempState.onlyOperational) {
                            tempState = tempState.copy(onlyOperational = it)
                        }
                    }
                }
            }

            // --- SECCIÓN INFERIOR: DISTANCIA MÁXIMA ---
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Blanco.copy(alpha = 0.3f))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.max_distance),
                    fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Blanco
                )
                Text(
                    text = "${tempState.maxDistanceKm.toInt()} km",
                    color = Blanco, fontWeight = FontWeight.Bold, fontSize = 13.sp
                )
            }
            // Control deslizante para ajustar el radio de búsqueda
            Slider(
                value = tempState.maxDistanceKm,
                onValueChange = { tempState = tempState.copy(maxDistanceKm = it) },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    activeTrackColor = Blanco,
                    inactiveTrackColor = Blanco.copy(alpha = 0.3f),
                    thumbColor = Blanco
                )
            )
        }
    }
}

/**
 * Componente de switch personalizado para filtrar solo fuentes operativas.
 */
@Composable
fun OperationalSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.only_operational),
            fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Blanco
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                // Estilo cuando está ACTIVO (filtrando operativas)
                checkedThumbColor = Negro.copy(alpha = 0.8f),
                checkedTrackColor = Blanco.copy(alpha = 0.5f),
                checkedBorderColor = Color.Transparent,

                // Estilo cuando está DESACTIVADO (mostrando todas)
                uncheckedThumbColor = Blue10,
                uncheckedTrackColor = Negro.copy(alpha = 0.2f),
                uncheckedBorderColor = Blanco.copy(alpha = 0.3f)
            ),
            modifier = Modifier.scale(0.8f)
        )
    }
}

/**
 * Título estilizado para las secciones del menú de filtros.
 */
@Composable
fun FilterSectionTitle(title: String, color: Color) {
    Text(
        text = title,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color
    )
}

/**
 * Chip de selección individual para las categorías.
 * Cambia a un fondo oscuro (Negro suave) al ser seleccionado.
 */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        // Color adaptativo según el estado de selección
        color = if (selected) Negro.copy(alpha = 0.8f) else Blanco.copy(alpha = 0.15f),
        border = BorderStroke(
            1.dp,
            if (selected) Negro.copy(alpha = 0.9f) else Blanco.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            color = Blanco,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}