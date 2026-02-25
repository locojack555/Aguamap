package cat.copernic.aguamap1.presentation.categories

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showFountainDetails by remember { mutableStateOf<Fountain?>(null) }

    BackHandler(enabled = showFountainDetails != null) { showFountainDetails = null }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            viewModel.selectedImageUri = uri
        }

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

    // Diálogos
    if (showCreateDialog) {
        CategoryFormDialog(
            "Nueva Categoría",
            viewModel,
            launcher,
            { showCreateDialog = false },
            { viewModel.saveCategory { showCreateDialog = false } })
    }
    if (showEditDialog && selectedCategory != null) {
        CategoryFormDialog(
            "Editar Categoría",
            viewModel,
            launcher,
            { showEditDialog = false },
            { viewModel.saveCategory { showEditDialog = false } })
    }
    if (showDetailDialog && selectedCategory != null) {
        CategoryDetailDialog(
            category = selectedCategory!!,
            fountains = fountainsByCategory[selectedCategory!!.id] ?: emptyList(),
            isAdmin = isAdmin,
            onDismiss = { showDetailDialog = false },
            onDeleteCategory = {
                viewModel.deleteCategory(selectedCategory!!.id); showDetailDialog = false
            },
            onEditCategory = {
                viewModel.onEditCategory(selectedCategory!!); showEditDialog = true
            },
            onFountainClick = { id ->
                val f = fountainsByCategory[selectedCategory!!.id]?.find { it.id == id }
                if (f != null) {
                    showDetailDialog = false; showFountainDetails = f
                }
            }
        )
    }
}