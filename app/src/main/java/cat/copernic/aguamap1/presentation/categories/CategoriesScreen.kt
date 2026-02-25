package cat.copernic.aguamap1.presentation.categories

import androidx.activity.compose.BackHandler
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
import cat.copernic.aguamap1.presentation.fountain.addFountain.detailFountain.DetailFountainViewModel
import cat.copernic.aguamap1.presentation.fountain.comments.FountainCommentsViewModel
import cat.copernic.aguamap1.presentation.fountain.detailFountain.DetailFountainScreen
import cat.copernic.aguamap1.ui.theme.BgGray50
import cat.copernic.aguamap1.ui.theme.Rojo

@Composable
fun CategoriesScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    detailFountainViewModel: DetailFountainViewModel = hiltViewModel(),
    commentsViewModel: FountainCommentsViewModel = hiltViewModel()
) {
    val isAdmin = viewModel.isAdmin
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val operationalFilter by viewModel.operationalFilter.collectAsState()
    val fountainsByCategory by viewModel.fountainsByCategory.collectAsState()

    // Capturamos el mensaje de error del ViewModel
    val errorMessage = viewModel.errorMessage

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showFountainDetails by remember { mutableStateOf<Fountain?>(null) }

    // Manejo del botón atrás cuando estamos viendo detalles de una fuente
    BackHandler(enabled = showFountainDetails != null) { showFountainDetails = null }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray50)
    ) {
        if (showFountainDetails != null) {
            DetailFountainScreen(
                fountain = showFountainDetails!!,
                viewModel = detailFountainViewModel,
                commentsViewModel = commentsViewModel,
                onBack = { showFountainDetails = null },
                onDelete = { showFountainDetails = null },
                onConfirm = {},
                onReportAveria = {},
                onReportNoExiste = {}
            )
        } else {
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
    }

    // --- SECCIÓN DE DIÁLOGOS ---

    // 1. DIÁLOGO DE ERROR (Aviso cuando hay fuentes vinculadas)
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

    // 2. Crear Categoría (Ahora usa el selector de cámara/galería interno)
    if (showCreateDialog) {
        CategoryFormDialog(
            title = stringResource(R.string.category_new_title),
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false },
            onConfirm = { viewModel.saveCategory { showCreateDialog = false } }
        )
    }

    // 3. Editar Categoría
    if (showEditDialog && selectedCategory != null) {
        CategoryFormDialog(
            title = stringResource(R.string.category_edit_title),
            viewModel = viewModel,
            onDismiss = { showEditDialog = false },
            onConfirm = { viewModel.saveCategory { showEditDialog = false } }
        )
    }

    // 4. Detalle de Categoría
    if (showDetailDialog && selectedCategory != null) {
        CategoryDetailDialog(
            category = selectedCategory!!,
            fountains = fountainsByCategory[selectedCategory!!.id] ?: emptyList(),
            isAdmin = isAdmin,
            onDismiss = { showDetailDialog = false },
            onDeleteCategory = {
                val categoryId = selectedCategory!!.id
                showDetailDialog = false // Cerramos detalle para mostrar el siguiente diálogo

                if (viewModel.canDeleteCategory(categoryId)) {
                    showDeleteConfirmation = true
                } else {
                    // Esto activará automáticamente el Diálogo de Error (1)
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
                    showFountainDetails = f
                }
            }
        )
    }

    // 5. Confirmación de Borrado (Solo categorías sin fuentes)
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