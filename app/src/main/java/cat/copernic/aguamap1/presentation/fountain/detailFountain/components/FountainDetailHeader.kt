package cat.copernic.aguamap1.presentation.fountain.detailFountain.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.*
import coil.compose.AsyncImage

@Composable
fun FountainDetailHeader(
    imageUrl: String,
    isAdmin: Boolean,
    isOwner: Boolean,
    isPending: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            placeholder = painterResource(R.drawable.pin_lleno),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(BlancoTranslucido, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Negro)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isAdmin || (isOwner && isPending)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.background(BlancoTranslucido, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Blue10)
                    }
                }
                if (isAdmin) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.background(BlancoTranslucido, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Rojo)
                    }
                }
            }
        }
    }
}