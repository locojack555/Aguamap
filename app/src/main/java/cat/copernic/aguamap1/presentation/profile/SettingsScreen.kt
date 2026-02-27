package cat.copernic.aguamap1.presentation.profile

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.presentation.language.LanguageViewModel

@Composable
fun SettingsScreen(
    onClose: () -> Unit = {},
    viewModel: LanguageViewModel = viewModel()
) {
    var selectedLanguage by remember {
        val current = AppCompatDelegate.getApplicationLocales()
            .toLanguageTags()
            .split(",")
            .firstOrNull()
            ?.split("-")
            ?.firstOrNull()
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
            ?: "es"
        mutableStateOf(current)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F3F4))
    ) {
        // CABECERA con gradiente azul
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF00B4DB), Color(0xFF0083B0))
                    )
                )
                .padding(horizontal = 8.dp, vertical = 40.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.config_profile_close_desc),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.config_profile_title),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // CUERPO
        Column(modifier = Modifier.padding(16.dp).fillMaxHeight()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(id = R.string.config_profile_subtitle),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    AguaMapLanguageSelector(
                        selectedLanguage = selectedLanguage,
                        onLanguageSelected = { code ->
                            selectedLanguage = code
                            viewModel.onChangeLanguage(code)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AguaMapLanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrow"
    )

    val languages = listOf(
        "es" to stringResource(id = R.string.config_profile_es),
        "ca" to stringResource(id = R.string.config_profile_en),
        "en" to stringResource(id = R.string.config_profile_en)
    )

    val selectedLabel = languages.firstOrNull { it.first == selectedLanguage }?.second ?: "Español"

    Column {
        // Selector principal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F3F4))
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedLabel,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121)
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF757575),
                modifier = Modifier
                    .size(20.dp)
                    .rotate(arrowRotation)
            )
        }

        // Lista desplegable animada
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
            ) {
                languages.forEachIndexed { index, (code, label) ->
                    val isSelected = code == selectedLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageSelected(code)
                                expanded = false
                            }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 15.sp,
                            color = if (isSelected) Color(0xFF0083B0) else Color(0xFF212121),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF0083B0),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (index < languages.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            thickness = 0.5.dp,
                            color = Color(0xFFEEEEEE)
                        )
                    }
                }
            }
        }
    }


}