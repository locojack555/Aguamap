package cat.copernic.aguamap1.presentation.profile.edit.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.*
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ProfileImageSection(
    imageToShow: Any?,
    isLocalSelection: Boolean,
    isUploading: Boolean,
    progress: Int,
    onAddPhoto: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteExisting: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.edit_profile_photo_label), fontWeight = FontWeight.Bold, color = AzulOscuro, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.size(140.dp).clip(CircleShape).clickable { onAddPhoto() }.background(GrisClaro, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (imageToShow != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(imageToShow).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().border(3.dp, AzulClaro, CircleShape),
                        error = painterResource(R.drawable.ic_placeholder)
                    )
                    if (isLocalSelection) {
                        IconButton(onClick = onClearSelection, modifier = Modifier.align(Alignment.TopEnd).size(28.dp).background(Color.White, CircleShape)) {
                            Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(14.dp))
                        }
                    }
                    if (isUploading) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f)), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(36.dp))
                                Text("$progress%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AccountCircle, null, tint = AzulClaro, modifier = Modifier.size(70.dp))
                        Text(stringResource(R.string.edit_profile_tap_to_add), fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.edit_profile_tap_to_change), fontSize = 11.sp, color = Color.Gray)

            if (imageToShow != null && !isLocalSelection && !isUploading) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onDeleteExisting, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.edit_profile_delete_photo), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ProfileNameSection(value: String, onValueChange: (String) -> Unit, enabled: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.edit_profile_name_label), fontWeight = FontWeight.SemiBold, color = AzulOscuro)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
    }
}

@Composable
fun EditProfileBottomBar(isEnabled: Boolean, isLoading: Boolean, onSave: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(GrisClaro).padding(16.dp)) {
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = isEnabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.edit_profile_save_btn))
            }
        }
    }
}