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
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Gris
import cat.copernic.aguamap1.ui.theme.GrisClaro
import cat.copernic.aguamap1.ui.theme.GrisOscuro
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.NegroMinimal
import cat.copernic.aguamap1.ui.theme.NegroSuave
import cat.copernic.aguamap1.ui.theme.Rojo
import cat.copernic.aguamap1.ui.theme.Verde

/**
 * Diálogo principal de reporte de incidencias para una fuente.
 * Permite a los usuarios notificar si una fuente no existe, si está averiada
 * o confirmar su existencia para revertir reportes negativos previos.
 *
 * @param negativeVotes Número actual de votos negativos/reportes de inexistencia.
 * @param hasVotedNegative Indica si el usuario actual ya ha reportado que la fuente no existe.
 * @param isOperational Estado operativo actual de la fuente (para alternar el reporte de avería).
 * @param onDismiss Cierra el diálogo.
 * @param onConfirmExistence Acción para validar que la fuente SÍ existe (revertir negativos).
 * @param onReportNoExiste Acción para reportar que la fuente no se encuentra en el lugar.
 * @param onReportAveria Acción para notificar un cambio en el estado de funcionamiento.
 * @param onShowOther Acción para abrir el diálogo secundario de motivos personalizados.
 */
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
                /**
                 * SECCIÓN: REVERTIR REPORTE.
                 * Solo se muestra si la comunidad ha reportado negativamente la fuente.
                 * Permite una "validación positiva" que contrarresta el proceso de borrado automático.
                 */
                if (negativeVotes > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = NegroMinimal
                    )
                }

                Text(
                    stringResource(R.string.report_dialog_text),
                    color = Negro,
                    fontSize = 14.sp
                )

                /**
                 * BOTÓN: NO EXISTE.
                 * Envía un voto negativo. Si se alcanza un umbral, la fuente puede ser eliminada.
                 */
                TextButton(
                    onClick = {
                        onReportNoExiste()
                        onDismiss()
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

                /**
                 * BOTÓN: AVERÍA / ARREGLADA.
                 * Cambia el estado 'operational' de la fuente en la base de datos.
                 */
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

                /**
                 * BOTÓN: OTROS.
                 * Abre un campo de texto libre para incidencias no categorizadas.
                 */
                TextButton(
                    onClick = {
                        onShowOther()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        tint = NegroSuave,
                        modifier = Modifier.size(20.dp)
                    )
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

/**
 * Diálogo secundario para reportes con motivo personalizado.
 * Incluye un campo de texto y validación de contenido no vacío.
 *
 * @param textValue Contenido actual del reporte.
 * @param onValueChange Actualiza el estado del texto en el ViewModel.
 * @param onDismiss Cierra el diálogo.
 * @param onSend Envía el reporte personalizado a los administradores.
 */
@Composable
fun OtherReportDialog(
    textValue: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit
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
                    onSend()
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