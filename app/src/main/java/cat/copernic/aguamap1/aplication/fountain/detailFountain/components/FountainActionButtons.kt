package cat.copernic.aguamap1.aplication.fountain.detailFountain.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.fountain.Fountain
import cat.copernic.aguamap1.domain.model.fountain.StateFountain
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.GrisClaro
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.Rojo

/**
 * Componente que presenta las acciones principales de interacción con una fuente.
 * Gestiona la visibilidad del botón de confirmación basado en el umbral de votos positivos
 * y proporciona la opción de reporte de incidencias.
 *
 * @param uiFountain Objeto de dominio que contiene los datos actuales de la fuente.
 * @param hasVotedPositive Estado booleano que indica si el usuario actual ya ha validado la fuente.
 * @param onConfirm Callback para ejecutar la lógica de votación positiva.
 * @param onReport Callback para iniciar el flujo de reporte o denuncia de la fuente.
 */
@Composable
fun FountainActionButtons(
    uiFountain: Fountain,
    hasVotedPositive: Boolean,
    onConfirm: () -> Unit,
    onReport: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        /**
         * Botón de validación comunitaria.
         * Solo es visible mientras la fuente no haya alcanzado el quórum de validación (3 votos).
         */
        if (uiFountain.status != StateFountain.ACCEPTED) {
            Button(
                onClick = onConfirm,
                enabled = !hasVotedPositive,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue10,
                    disabledContainerColor = GrisClaro,
                    contentColor = Negro,
                    disabledContentColor = Negro
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (hasVotedPositive) Icons.Default.Check else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (hasVotedPositive) stringResource(R.string.voted_label)
                    else stringResource(R.string.confirm_button),
                    fontSize = 14.sp
                )
            }
        }

        /**
         * Botón de reporte.
         * Permite a los usuarios notificar problemas o información errónea sobre el punto de agua.
         */
        Button(
            onClick = onReport,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Rojo),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Flag, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.report_button), fontSize = 14.sp)
        }
    }
}