package cat.copernic.aguamap1.presentation.categories

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
import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.presentation.categories.components.CategoriesHeader
import cat.copernic.aguamap1.presentation.categories.components.CategoryDetailDialog
import cat.copernic.aguamap1.presentation.categories.components.CategoryFormDialog
import cat.copernic.aguamap1.presentation.categories.components.CategoryItem
import cat.copernic.aguamap1.ui.theme.BgGray50
import cat.copernic.aguamap1.ui.theme.Rojo

@Composable
fun CategoriesScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    // Añadimos el callback de navegación para conectar con el NavHost
    userLat: Double?,
    userLng: Double?,
    onFountainClick: (Fountain) -> Unit
) {

    LaunchedEffect(userLat, userLng) {
        if (userLat != null && userLng != null) {
            viewModel.setLocation(userLat, userLng)
        }
    }
    val isAdmin = viewModel.isAdmin
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val operationalFilter by viewModel.operationalFilter.collectAsState()
    val fountainsByCategory by viewModel.fountainsByCategory.collectAsState()

    val errorMessage = viewModel.errorMessage

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
            CategoriesHeader(
                searchQuery = searchQuery,
                onSearchChange = { viewModel.updateSearchQuery(it) },
                operationalFilter = operationalFilter,
                onToggleFilter = { isSelected -> viewModel.toggleOperationalFilter(isSelected) },
                isAdmin = isAdmin,
                onAddClick = { viewModel.resetForm(); showCreateDialog = true }
            )

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
                        onClick = { selectedCategory = category; showDetailDialog = true }
                    )
                }
            }
        }
    }

    // --- SECCIÓN DE DIÁLOGOS ---

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(stringResource(R.string.category_accept))
                }
            },
            title = { Text(stringResource(R.string.error_title)) },
            text = { Text(errorMessage!!) }
        )
    }

    if (showCreateDialog) {
        CategoryFormDialog(
            title = stringResource(R.string.category_new_title),
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false },
            onConfirm = { viewModel.saveCategory { showCreateDialog = false } }
        )
    }

    if (showEditDialog && selectedCategory != null) {
        CategoryFormDialog(
            title = stringResource(R.string.category_edit_title),
            viewModel = viewModel,
            onDismiss = { showEditDialog = false },
            onConfirm = { viewModel.saveCategory { showEditDialog = false } }
        )
    }

    if (showDetailDialog && selectedCategory != null) {
        CategoryDetailDialog(
            category = selectedCategory!!,
            fountains = fountainsByCategory[selectedCategory!!.id] ?: emptyList(),
            isAdmin = isAdmin,
            onDismiss = { showDetailDialog = false },
            onDeleteCategory = {
                val categoryId = selectedCategory!!.id
                showDetailDialog = false

                if (viewModel.fountainsByCategory.value[categoryId].isNullOrEmpty()) {
                    // Si está vacía, pedimos confirmación para borrar
                    showDeleteConfirmation = true
                } else {
                    // Si tiene fuentes, ejecutamos delete para que el ViewModel
                    // dispare el mensaje de error: "No se puede eliminar..."
                    viewModel.deleteCategory(categoryId) {}
                }
            },
            onEditCategory = {
                viewModel.onEditCategory(selectedCategory!!)
                showEditDialog = true
            },
            onFountainClick = { id ->
                val f = fountainsByCategory[selectedCategory!!.id]?.find { it.id == id }
                if (f != null) {
                    showDetailDialog = false
                    // ¡CLAVE!: Llamamos al callback de navegación en lugar de abrir un Detail local
                    onFountainClick(f)
                }
            }
        )
    }

    if (showDeleteConfirmation && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_category_title)) },
            text = { Text(stringResource(R.string.delete_category_msg, selectedCategory!!.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(selectedCategory!!.id) {
                            showDeleteConfirmation = false
                            showDetailDialog = false
                            selectedCategory = null
                        }
                    }
                ) {
                    Text(stringResource(R.string.category_save), color = Rojo)
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