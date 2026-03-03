package cat.copernic.aguamap1.presentation.profile.reports.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Report
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FountainReportCard(
    report: Report,
    reporterName: String,
    onResolve: () -> Unit,
    onGoToFountain: () -> Unit
) {
    var showResolveDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val reportDate = remember(report.timestamp) { if (report.timestamp > 0) dateFormatter.format(Date(report.timestamp)) else "—" }

    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF43A047)) },
            title = { Text(stringResource(R.string.reports_dialog_resolve_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.reports_dialog_resolve_desc)) },
            confirmButton = {
                Button(
                    onClick = { showResolveDialog = false; onResolve() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                ) { Text(stringResource(R.string.reports_btn_resolve)) }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badge y Fecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Report, null, tint = Color(0xFFE53935), modifier = Modifier.size(13.dp))
                        Text(stringResource(R.string.reports_badge_label), fontSize = 11.sp, color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(reportDate, fontSize = 11.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(12.dp)); HorizontalDivider(color = Color(0xFFF0F0F0)); Spacer(Modifier.height(12.dp))

            // Datos de la fuente y reportero
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(color = Color(0xFF0083B0), shape = CircleShape, modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                }
                Column {
                    Text(stringResource(R.string.reports_fountain_label), fontSize = 11.sp, color = Color.Gray)
                    Text(report.fountainName, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                Text(stringResource(R.string.reports_reported_by, reporterName), fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(10.dp))
            Surface(color = Color(0xFFFAFAFA), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(10.dp), Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ChatBubbleOutline, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    Text(report.description.ifBlank { stringResource(R.string.reports_no_description) }, fontSize = 13.sp, color = Color.DarkGray)
                }
            }

            Spacer(Modifier.height(14.dp)); HorizontalDivider(color = Color(0xFFF0F0F0)); Spacer(Modifier.height(10.dp))

            // Botones
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onGoToFountain, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0083B0))) {
                    Text(stringResource(R.string.reports_btn_view_fountain), fontSize = 13.sp)
                }
                Button(onClick = { showResolveDialog = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))) {
                    Text(stringResource(R.string.reports_btn_resolve_short), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyFountainReportsState() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF43A047), modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.reports_empty_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(stringResource(R.string.reports_empty_desc), color = Color.Gray)
    }
}