package cat.copernic.aguamap1.presentation.maps.components

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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.presentation.maps.mapView.MapViewModel
import cat.copernic.aguamap1.presentation.util.FilterState
import cat.copernic.aguamap1.presentation.util.SortOption
import cat.copernic.aguamap1.ui.theme.AguaMapGradient
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro

@Composable
fun MapTopBar(
    isMapView: Boolean,
    onToggleView: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
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
                    stringResource(R.string.search_fountains),
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
                    IconButton(onClick = { viewModel.toggleFilterMenu() }) {
                        Icon(
                            painterResource(R.drawable.filter_alt_24px),
                            contentDescription = stringResource(R.string.filter),
                            tint = if (viewModel.filterState != FilterState()) Blue10 else Negro.copy(
                                alpha = 0.7f
                            )
                        )
                    }
                    FilterDropDown(
                        expanded = viewModel.showFilterMenu,
                        onDismiss = { viewModel.toggleFilterMenu() },
                        state = viewModel.filterState,
                        categories = viewModel.categories,
                        onFilterChanged = { viewModel.updateFilters(it) },
                        showSortOptions = !isMapView
                    )
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

@Composable
fun FilterDropDown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    state: FilterState,
    categories: List<Category>,
    onFilterChanged: (FilterState) -> Unit,
    showSortOptions: Boolean
) {
    val menuShape = RoundedCornerShape(28.dp)

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
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.filter_title),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Blanco
                )
                Surface(
                    color = Negro.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { onFilterChanged(FilterState()) }
                ) {
                    Text(
                        text = stringResource(R.string.clear_filters),
                        color = Blanco,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = Blanco.copy(alpha = 0.3f))

            // Body
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FilterSectionTitle(stringResource(R.string.filter_by_category), Blanco)
                    categories.forEach { category ->
                        FilterChip(
                            label = category.name,
                            selected = state.selectedCategory?.id == category.id,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { onFilterChanged(state.copy(selectedCategory = if (state.selectedCategory?.id == category.id) null else category)) }
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    FilterSectionTitle(stringResource(R.string.min_rating), Blanco)
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (star <= state.minRating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (star <= state.minRating) Blanco else Blanco.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onFilterChanged(state.copy(minRating = star.toFloat())) }
                            )
                        }
                    }

                    if (showSortOptions) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FilterSectionTitle(stringResource(R.string.sort_by), Blanco)
                        SortOption.entries.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onFilterChanged(state.copy(sortBy = option)) }
                            ) {
                                RadioButton(
                                    selected = state.sortBy == option,
                                    onClick = null,
                                    modifier = Modifier.size(28.dp),
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Blanco,
                                        unselectedColor = Blanco.copy(alpha = 0.5f)
                                    )
                                )
                                Text(
                                    text = when (option) {
                                        SortOption.DISTANCE -> stringResource(R.string.sort_distance)
                                        SortOption.RATING -> stringResource(R.string.sort_rating)
                                        SortOption.DATE -> stringResource(R.string.sort_date)
                                    },
                                    fontSize = 11.sp, color = Blanco
                                )
                            }
                        }
                    }
                    OperationalSwitch(state.onlyOperational) {
                        onFilterChanged(
                            state.copy(
                                onlyOperational = it
                            )
                        )
                    }
                }
            }

            // Distance Slider
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Blanco.copy(alpha = 0.3f))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.max_distance),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Blanco
                )
                Text(
                    "${state.maxDistanceKm.toInt()} km",
                    color = Blanco,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Slider(
                value = state.maxDistanceKm,
                onValueChange = { onFilterChanged(state.copy(maxDistanceKm = it)) },
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

@Composable
fun OperationalSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.only_operational),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Blanco
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Blue10,
                checkedTrackColor = Blanco.copy(alpha = 0.5f),
                uncheckedThumbColor = Blanco.copy(alpha = 0.6f),
                uncheckedTrackColor = Negro.copy(alpha = 0.2f)
            ),
            modifier = Modifier.scale(0.8f)
        )
    }
}

@Composable
fun FilterSectionTitle(title: String, color: Color) {
    Text(
        text = title,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

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
        color = if (selected) Blue10 else Blanco.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, if (selected) Blue10 else Blanco.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            color = Blanco,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}