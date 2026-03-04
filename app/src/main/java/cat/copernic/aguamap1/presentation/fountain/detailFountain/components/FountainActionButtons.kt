package cat.copernic.aguamap1.presentation.fountain.detailFountain.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.GrisClaro
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.NegroSuave
import cat.copernic.aguamap1.ui.theme.Rojo

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
        // Solo mostramos el botón de confirmar si la fuente tiene menos de 3 votos (está pendiente)
        if (uiFountain.positiveVotes < 3) {
            Button(
                onClick = onConfirm,
                enabled = !hasVotedPositive,
                modifier = Modifier.weight(1f).height(52.dp),
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

        Button(
            onClick = onReport,
            modifier = Modifier.weight(1f).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Rojo),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Flag, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.report_button), fontSize = 14.sp)
        }
    }
}