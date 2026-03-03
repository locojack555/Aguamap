package cat.copernic.aguamap1.presentation.categories.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.AzulClaro
import cat.copernic.aguamap1.ui.theme.AzulOscuro
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Gris
import cat.copernic.aguamap1.ui.theme.GrisClaro
import cat.copernic.aguamap1.ui.theme.Rojo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    operationalFilter: Boolean?,
    onToggleFilter: (Boolean) -> Unit,
    isAdmin: Boolean,
    onAddClick: () -> Unit
) {
    Surface(color = Blanco, shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.categories_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulOscuro
                )
                if (isAdmin) {
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier
                            .border(1.dp, GrisClaro, RoundedCornerShape(8.dp))
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            // CAMBIO: Añadido stringResource para accesibilidad
                            contentDescription = stringResource(R.string.new_category),
                            tint = Blue10
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(stringResource(R.string.search_placeholder), color = Gris) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        // CAMBIO: Añadido stringResource (puedes usar search_placeholder o crear uno nuevo)
                        contentDescription = stringResource(R.string.search_fountains),
                        tint = AzulOscuro
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AzulClaro,
                    focusedContainerColor = AzulClaro,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Blue10
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            val isSelected = operationalFilter == false
            FilterChip(
                selected = isSelected,
                onClick = { onToggleFilter(isSelected) },
                label = { Text(stringResource(R.string.category_only_broken)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Warning,
                        // CAMBIO: Añadido stringResource
                        contentDescription = stringResource(R.string.legend_averiada),
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) Blanco else Rojo
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Blanco,
                    labelColor = Rojo,
                    selectedContainerColor = Rojo,
                    selectedLabelColor = Blanco
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Rojo,
                    selectedBorderColor = Rojo,
                    borderWidth = 1.dp
                )
            )
        }
    }
}