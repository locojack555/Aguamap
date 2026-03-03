package cat.copernic.aguamap1.presentation.fountain.detailFountain.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.*

@Composable
fun MainReportDialog(
    negativeVotes: Int,
    hasVotedNegative: Boolean,
    isOperational: Boolean,
    onDismiss: () -> Unit,
    onConfirmExistence: () -> Unit,
    onReportNoExiste: () -> Unit,
    onReportAveria: () -> Unit,
    onShowOther: () -> Unit
) {
    AlertDialog(
        containerColor = Blanco,
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.report_dialog_title),
                color = Negro,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // --- SECCIÓN: REVERTIR REPORTE (Aparece si hay votos negativos previos) ---
                if (negativeVotes > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
                                // Ejecuta la actualización optimista en el ViewModel y cierra ya
                                onConfirmExistence()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Verde),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Blanco)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.report_confirm_existence),
                                color = Blanco,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            stringResource(R.string.report_confirm_existence_hint),
                            fontSize = 12.sp,
                            color = NegroSuave,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = NegroMinimal)
                }

                Text(
                    stringResource(R.string.report_dialog_text),
                    color = Negro,
                    fontSize = 14.sp
                )

                // --- BOTÓN: NO EXISTE ---
                TextButton(
                    onClick = {
                        onReportNoExiste()
                        onDismiss() // Cierre inmediato para que se vea el cambio local
                    },
                    enabled = !hasVotedNegative,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Block,
                        null,
                        tint = if (hasVotedNegative) GrisClaro else Rojo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (hasVotedNegative) stringResource(R.string.report_already_voted_not_exists)
                        else stringResource(R.string.report_not_exists),
                        color = if (hasVotedNegative) GrisClaro else Rojo,
                        modifier = Modifier.weight(1f)
                    )
                }

                // --- BOTÓN: AVERÍA / ARREGLADA ---
                TextButton(
                    onClick = {
                        onReportAveria()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Build, null, tint = Blue10, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (isOperational) stringResource(R.string.report_broken)
                        else stringResource(R.string.report_fixed),
                        color = Blue10,
                        modifier = Modifier.weight(1f)
                    )
                }

                // --- BOTÓN: OTROS ---
                TextButton(
                    onClick = {
                        onShowOther()
                        // Aquí NO cerramos onDismiss() porque queremos que se abra el OtherReportDialog
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Info, null, tint = NegroSuave, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.report_other_reason),
                        color = NegroSuave,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.cancel),
                    color = Blue10,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun OtherReportDialog(
    textValue: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit // <--- Este es el nombre que manda
) {
    AlertDialog(
        containerColor = Blanco,
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.report_other_title),
                color = Negro,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.report_other_hint),
                    fontSize = 14.sp,
                    color = GrisOscuro
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = onValueChange,
                    placeholder = { Text(stringResource(R.string.report_other_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSend() // <--- Se ejecuta aquí
                    onDismiss()
                },
                enabled = textValue.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.report_other_send), color = Blanco)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = Gris)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}