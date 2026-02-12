package cat.copernic.aguamap1.presentation.reusable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Negro

@Composable
fun HomeTopBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        color = Blanco,
        shadowElevation = 6.dp
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {/*buscar*/ },
            placeholder = { Text("Buscar fuentes...", color = Negro) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.search_24px),
                    contentDescription = "Buscar",
                    tint = Color.Unspecified
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* Filtro */ }) {
                        Icon(
                            painterResource(R.drawable.filter_alt_24px),
                            contentDescription = "Filtro",
                            tint = Color.Unspecified
                        )
                    }
                    IconButton(onClick = { /* Lista */ }) {
                        Icon(
                            painterResource(R.drawable.format_list_bulleted_24px),
                            contentDescription = "Lista",
                            tint = Color.Unspecified
                        )
                    }
                    IconButton(onClick = { /* Notificaciones */ }) {
                        BadgedBox(badge = { Badge { Text("1") } }) {
                            Icon(
                                painterResource(R.drawable.notifications_24px),
                                contentDescription = "Notificaciones",
                                tint = Color.Unspecified
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}