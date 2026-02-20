package cat.copernic.aguamap1.presentation.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import cat.copernic.aguamap1.domain.model.Category
import cat.copernic.aguamap1.domain.model.Fountain

val BgGray50 = Color(0xFFF9FAFB)
val TextGray800 = Color(0xFF1F2937)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    isAdmin: Boolean = true
) {
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val fountainsByCategory by viewModel.fountainsByCategory.collectAsState()

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(BgGray50)) {
        Surface(color = Color.White, shadowElevation = 2.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Categorías", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextGray800)
                    if (isAdmin) {
                        IconButton(
                            onClick = { /* Acción añadir pendiente */ },
                            modifier = Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)).size(40.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir Categoría")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Buscar...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = BgGray50, focusedContainerColor = BgGray50,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                // Buscamos la fuente usando el nombre de la categoría normalizado
                val searchKey = category.name.lowercase().trim()
                val categoryFountains = fountainsByCategory[searchKey] ?: emptyList()

                CategoryItem(
                    category = category,
                    count = categoryFountains.size,
                    onClick = {
                        selectedCategory = category
                        showDetailDialog = true
                    }
                )
            }
        }
    }

    if (showDetailDialog && selectedCategory != null) {
        val searchKey = selectedCategory!!.name.lowercase().trim()
        val listForDialog = fountainsByCategory[searchKey] ?: emptyList()

        CategoryDetailDialog(
            category = selectedCategory!!,
            fountains = listForDialog,
            isAdmin = isAdmin,
            onDismiss = { showDetailDialog = false },
            onDelete = {
                viewModel.deleteCategory(selectedCategory!!.id)
                showDetailDialog = false
            }
        )
    }
}

@Composable
fun CategoryItem(category: Category, count: Int, onClick: () -> Unit) {
    val categoryColor = try { Color(android.graphics.Color.parseColor(category.color)) } catch (e: Exception) { Color.Blue }

    Card(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
            contentDescription = "Categoría ${category.name}, contiene $count fuentes. ${category.description}"
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
                    Text(text = category.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = BgGray50, shape = RoundedCornerShape(16.dp), modifier = Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))) {
                        Text(text = count.toString(), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "$count fuentes", fontSize = 14.sp, color = Color.Gray)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun CategoryDetailDialog(
    category: Category,
    fountains: List<Fountain>,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = category.icon, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = category.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (category.description.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)).padding(12.dp)) {
                        Text(text = category.description, color = Color(0xFF374151), fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${fountains.size} fuentes", fontSize = 14.sp, color = Color.Gray)
                    if (isAdmin) {
                        Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                            Icon(Icons.Default.Delete, contentDescription = "Borrar", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Borrar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (fountains.isEmpty()) {
                        item {
                            Text(
                                text = "No hay fuentes en esta categoría",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()
                            )
                        }
                    } else {
                        items(fountains) { fountain ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BgGray50, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = fountain.name.ifEmpty { "Fuente sin nombre" }, fontWeight = FontWeight.Medium, fontSize = 14.sp)

                                    if (fountain.description.isNotEmpty()) {
                                        Text(text = fountain.description, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }

                                    // Insignias usando los datos reales de tu modelo
                                    Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (fountain.status == "PENDING") {
                                            Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(4.dp)) {
                                                Text("Pendiente", fontSize = 10.sp, color = Color(0xFFB45309), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        if (!fountain.operational) {
                                            Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(4.dp)) {
                                                Text("Averiada", fontSize = 10.sp, color = Color(0xFFB91C1C), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                }

                                // Muestra el rating si tiene votos
                                if (fountain.totalRatings > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                                        Text(text = String.format("%.1f", fountain.ratingAverage), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}