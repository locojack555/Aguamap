package cat.copernic.aguamap1.presentation.categories

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.model.Fountain

val BgGray50 = Color(0xFFF9FAFB)
val TextGray800 = Color(0xFF1F2937)
val DangerRed = Color(0xFFEF4444) // Color rojo para acción de borrar
val InfoBlue = Color(0xFF3B82F6)  // Color azul para acción de editar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    isAdmin: Boolean = true
) {
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val stateFilter by viewModel.stateFilter.collectAsState()
    val fountainsByCategory by viewModel.fountainsByCategory.collectAsState()

    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(BgGray50)) {
        Surface(color = Color.White, shadowElevation = 2.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.categories_title), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextGray800)
                    if (isAdmin) {
                        IconButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)).size(40.dp)
                        ) { Icon(Icons.Default.Add, contentDescription = "Añadir Categoría") }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = BgGray50, focusedContainerColor = BgGray50,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = sortOrder == SortOrder.BEST_RATING,
                        onClick = { viewModel.updateSortOrder(if (sortOrder == SortOrder.BEST_RATING) SortOrder.NAME else SortOrder.BEST_RATING) },
                        label = { Text(stringResource(R.string.best_rated)) },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = sortOrder == SortOrder.WORST_RATING,
                        onClick = { viewModel.updateSortOrder(if (sortOrder == SortOrder.WORST_RATING) SortOrder.NAME else SortOrder.WORST_RATING) },
                        label = { Text(stringResource(R.string.worst_rated)) },
                        leadingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = stateFilter == FountainStateFilter.OPERATIONAL,
                        onClick = { viewModel.updateStateFilter(if (stateFilter == FountainStateFilter.OPERATIONAL) FountainStateFilter.ALL else FountainStateFilter.OPERATIONAL) },
                        label = { Text(stringResource(R.string.only_operational)) }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                val catId = category.id.lowercase().trim()
                val catName = category.name.lowercase().trim()
                val resId = context.resources.getIdentifier(catName, "string", context.packageName)
                val translatedName = if (resId != 0) stringResource(resId) else category.name

                val categoryFountains = fountainsByCategory.entries.filter { (clave, _) ->
                    clave == catId || clave == catName
                }.flatMap { it.value }

                CategoryItem(
                    category = category,
                    displayName = translatedName,
                    count = categoryFountains.size,
                    onClick = {
                        selectedCategory = category
                        showDetailDialog = true
                    }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { newCat ->
                viewModel.addCategory(newCat)
                showCreateDialog = false
            }
        )
    }

    if (showEditDialog && selectedCategory != null) {
        EditCategoryDialog(
            category = selectedCategory!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedCat ->
                viewModel.updateCategory(updatedCat)
                selectedCategory = updatedCat
                showEditDialog = false
            }
        )
    }

    if (showDetailDialog && selectedCategory != null) {
        val catId = selectedCategory!!.id.lowercase().trim()
        val catName = selectedCategory!!.name.lowercase().trim()

        val listForDialog = fountainsByCategory.entries.filter { (clave, _) ->
            clave == catId || clave == catName
        }.flatMap { it.value }

        val resId = context.resources.getIdentifier(catName, "string", context.packageName)
        val translatedName = if (resId != 0) stringResource(resId) else selectedCategory!!.name

        CategoryDetailDialog(
            category = selectedCategory!!,
            displayName = translatedName,
            fountains = listForDialog,
            isAdmin = isAdmin,
            onDismiss = { showDetailDialog = false },
            onDeleteCategory = {
                viewModel.deleteCategory(selectedCategory!!.id)
                showDetailDialog = false
            },
            onEditCategory = { showEditDialog = true }
        )
    }
}

@Composable
fun CreateCategoryDialog(onDismiss: () -> Unit, onConfirm: (Category) -> Unit) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("💧") }
    var description by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("#F5F5DC") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_category), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre clave (ej: bebible)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = icon, onValueChange = { icon = it }, label = { Text("Icono (Emoji)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Color Hex (ej: #F5F5DC)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(Category(name = name, icon = icon, color = color, description = description)) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
            ) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun EditCategoryDialog(category: Category, onDismiss: () -> Unit, onConfirm: (Category) -> Unit) {
    var name by remember { mutableStateOf(category.name) }
    var icon by remember { mutableStateOf(category.icon) }
    var description by remember { mutableStateOf(category.description) }
    var color by remember { mutableStateOf(category.color) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre clave") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = icon, onValueChange = { icon = it }, label = { Text("Icono (Emoji)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Color Hex (ej: #F5F5DC)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(category.copy(name = name, icon = icon, color = color, description = description))
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun CategoryItem(category: Category, displayName: String, count: Int, onClick: () -> Unit) {
    val categoryColor = try { Color(android.graphics.Color.parseColor(category.color)) } catch (e: Exception) { Color.Blue }

    Card(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
            contentDescription = "Categoría $displayName, contiene $count fuentes."
            role = Role.Button
        }.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(categoryColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Text(text = category.icon, fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = displayName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = BgGray50, shape = RoundedCornerShape(16.dp), modifier = Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))) {
                        Text(text = count.toString(), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "$count ${stringResource(R.string.fountains)}", fontSize = 14.sp, color = Color.Gray)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun CategoryDetailDialog(
    category: Category,
    displayName: String,
    fountains: List<Fountain>,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onDeleteCategory: () -> Unit,
    onEditCategory: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {

                // CABECERA CON BOTONES INTEGRADOS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = category.icon, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = displayName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // BOTONES DE ADMIN ARRIBA A LA DERECHA
                    if (isAdmin) {
                        IconButton(onClick = onEditCategory, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = InfoBlue)
                        }
                        IconButton(onClick = onDeleteCategory, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_category), tint = DangerRed)
                        }
                    }
                }

                // DESCRIPCIÓN (Si existe)
                if (category.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = category.description, color = TextGray800, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = BgGray50) // Línea separadora
                Spacer(modifier = Modifier.height(12.dp))

                // CONTADOR y LISTA
                Text(text = "${fountains.size} ${stringResource(R.string.fountains)}", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    if (fountains.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                Text(text = "No hay fuentes en esta categoría", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        items(fountains) { fountain ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BgGray50, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(painter = painterResource(id = R.drawable.gota), contentDescription = null, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = fountain.name.ifEmpty { "Fuente sin nombre" }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (fountain.description.isNotEmpty()) {
                                        Text(text = fountain.description, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                                        Text(text = String.format("%.1f", fountain.ratingAverage), fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp))
                                    }
                                    Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (fountain.status.name == "PENDING") {
                                            Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(4.dp)) { Text("Pendiente", fontSize = 10.sp, color = Color(0xFFB45309), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) }
                                        }
                                        if (!fountain.operational) {
                                            Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(4.dp)) { Text("Averiada", fontSize = 10.sp, color = Color(0xFFB91C1C), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) }
                                        }
                                    }
                                }
                                IconButton(onClick = { /* Ampliar Info */ }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Info, contentDescription = stringResource(R.string.expand_info), tint = InfoBlue, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }

                // Botón cerrar del diálogo
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = onDismiss) { Text("Cerrar") }
                }
            }
        }
    }
}