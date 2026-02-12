package cat.copernic.aguamap1.presentation.reusable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Buscador estilizado
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Buscar...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Botones de acción
        IconButton(onClick = { /* Lista */ }) {
            Icon(
                Icons.Default.Search,
                contentDescription = null
            )
        }
        IconButton(onClick = { /* Filtro */ }) {
            Icon(
                Icons.Default.Search,
                contentDescription = null
            )
        }

        // Notificaciones con Badge
        BadgedBox(badge = { Badge { Text("1") } }) {
            Icon(Icons.Default.Notifications, contentDescription = null)
        }
    }
}