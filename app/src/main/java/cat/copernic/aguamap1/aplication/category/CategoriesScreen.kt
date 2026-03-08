package cat.copernic.aguamap1.aplication.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.category.Category
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.aplication.category.components.CategoriesHeader
import cat.copernic.aguamap1.aplication.category.components.CategoryDetailDialog
import cat.copernic.aguamap1.aplication.category.components.CategoryFormDialog
import cat.copernic.aguamap1.aplication.category.components.CategoryItem
import cat.copernic.aguamap1.ui.theme.BgGray50
import cat.copernic.aguamap1.ui.theme.Rojo

/**
 * Pantalla principal de Categorías de AguaMap.
 * * Orquesta la visualización de grupos de fuentes, permitiendo filtrar por nombre o estado
 * operativo. Gestiona el ciclo de vida de múltiples diálogos (detalle, creación, edición, borrado)
 * y sincroniza la ubicación del usuario para cálculos de proximidad.
 *
 * @param viewModel Instancia del ViewModel inyectada por Hilt.
 * @param userLat Latitud actual del usuario para el cálculo de distancias.
 * @param userLng Longitud actual del usuario para el cálculo de distancias.
 * @param onFountainClick Callback de navegación que se dispara cuando el usuario selecciona
 * una fuente específica dentro de una categoría.
 */
@Composable
fun CategoriesScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    userLat: Double?,
    userLng: Double?,
    onFountainClick: (Fountain) -> Unit
) {
    // Sincronización de ubicación: Actualiza el estado del VM cuando cambian las coordenadas
    LaunchedEffect(userLat, userLng) {
        if (userLat != null && userLng != null) {
            viewModel.setLocation(userLat, userLng)
        }
    }

    // Observación de estados reactivos del ViewModel
    val isAdmin = viewModel.isAdmin
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val operationalFilter by viewModel.operationalFilter.collectAsState()
    val fountainsByCategory by viewModel.fountainsByCategory.collectAsState()
    val errorMessage = viewModel.errorMessage

    // Estados locales para el control de la visibilidad de los diálogos
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray50)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Cabecera interactiva con búsqueda y filtros
            CategoriesHeader(
                searchQuery = searchQuery,
                onSearchChange = { viewModel.updateSearchQuery(it) },
                operationalFilter = operationalFilter,
                onToggleFilter = { isSelected -> viewModel.toggleOperationalFilter(isSelected) },
                isAdmin = isAdmin,
                onAddClick = {
                    viewModel.resetForm()
                    showCreateDialog = true
                }
            )

            // Lista eficiente de categorías
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories, key = { it.id }) { category ->
                    CategoryItem(
                        category = category,
                        count = fountainsByCategory[category.id]?.size ?: 0,
                        onClick = {
                            selectedCategory = category
                            showDetailDialog = true
                        }
                    )
                }
            }
        }
    }

    // --- SECCIÓN DE DIÁLOGOS LOCALIZADOS ---

    // Diálogo de error global: Muestra fallos de validación o red
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(stringResource(R.string.category_accept))
                }
            },
            title = { Text(stringResource(R.string.error_title)) },
            text = { Text(errorMessage) }
        )
    }

    // Formulario para añadir una nueva categoría
    if (showCreateDialog) {
        CategoryFormDialog(
            title = stringResource(R.string.category_new_title),
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false },
            onConfirm = { viewModel.saveCategory { showCreateDialog = false } }
        )
    }

    // Formulario para editar una categoría existente
    if (showEditDialog && selectedCategory != null) {
        CategoryFormDialog(
            title = stringResource(R.string.category_edit_title),
            viewModel = viewModel,
            onDismiss = { showEditDialog = false },
            onConfirm = { viewModel.saveCategory { showEditDialog = false } }
        )
    }

    // Vista detallada de las fuentes de la categoría seleccionada
    if (showDetailDialog && selectedCategory != null) {
        CategoryDetailDialog(
            category = selectedCategory!!,
            fountains = fountainsByCategory[selectedCategory!!.id] ?: emptyList(),
            isAdmin = isAdmin,
            onDismiss = { showDetailDialog = false },
            onDeleteCategory = {
                val categoryId = selectedCategory!!.id
                // Validación previa: No permitir borrar si contiene fuentes
                if (viewModel.fountainsByCategory.value[categoryId].isNullOrEmpty()) {
                    showDetailDialog = false
                    showDeleteConfirmation = true
                } else {
                    viewModel.deleteCategory(categoryId) {}
                }
            },
            onEditCategory = {
                viewModel.onEditCategory(selectedCategory!!)
                showDetailDialog = false
                showEditDialog = true
            },
            onFountainClick = { id ->
                val f = fountainsByCategory[selectedCategory!!.id]?.find { it.id == id }
                if (f != null) {
                    showDetailDialog = false
                    onFountainClick(f)
                }
            }
        )
    }

    // Diálogo de confirmación destructiva para eliminación de categorías
    if (showDeleteConfirmation && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_category_title)) },
            text = {
                Text(stringResource(R.string.delete_category_msg, selectedCategory!!.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(selectedCategory!!.id) {
                            showDeleteConfirmation = false
                            selectedCategory = null
                        }
                    }
                ) {
                    Text(stringResource(R.string.delete_confirm), color = Rojo)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.category_cancel))
                }
            }
        )
    }
}