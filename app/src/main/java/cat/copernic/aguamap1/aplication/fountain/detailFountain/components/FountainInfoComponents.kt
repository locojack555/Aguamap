package cat.copernic.aguamap1.aplication.fountain.detailFountain.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.NegroMuySuave
import cat.copernic.aguamap1.ui.theme.NegroSuave

/**
 * Tarjeta informativa para mostrar el progreso de validación comunitaria.
 * Utiliza un diseño de superficie con bordes y colores dinámicos según el estado.
 *
 * @param title Título descriptivo del tipo de validación.
 * @param count Cantidad actual de votos o validaciones recibidas.
 * @param target Objetivo de validaciones necesario para el cambio de estado.
 * @param color Color temático aplicado a iconos, bordes y textos.
 * @param icon Icono representativo de la acción de validación.
 */
@Composable
fun ValidationCard(title: String, count: Int, target: Int, color: Color, icon: ImageVector) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    color = color,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Text(
                    text = stringResource(R.string.validation_count_plural, count, target),
                    color = color.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Fila de información técnica estandarizada con icono y par etiqueta-valor.
 *
 * @param icon Icono descriptivo del dato (distancia, estado, etc.).
 * @param label Texto de la etiqueta descriptiva.
 * @param value Valor del dato a mostrar.
 * @param valueColor Color específico para resaltar el valor (ej. Verde para operativo, Rojo para averiado).
 */
@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = NegroMuySuave
        )
        Spacer(Modifier.width(12.dp))

        /**
         * Etiqueta descriptiva en tono neutro.
         */
        Text(
            text = "$label: ",
            color = NegroSuave,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        /**
         * Valor destacado con tipografía bold y color semántico.
         */
        Text(
            text = value,
            color = valueColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}