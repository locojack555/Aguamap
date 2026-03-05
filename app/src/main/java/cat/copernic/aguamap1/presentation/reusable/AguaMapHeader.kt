package cat.copernic.aguamap1.presentation.reusable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.ui.theme.Blanco

/**
 * Componente de cabecera reutilizable para la aplicación AguaMap.
 * * @param modifier Modificador para personalizar el diseño del contenedor.
 * @param logoSize Tamaño de la imagen del logo (gota).
 * @param innerSpacing Espacio vertical entre el logo y el nombre de la aplicación.
 * @param isSplash Indica si el componente se comporta como pantalla de bienvenida,
 * controlando la visibilidad del selector de idioma y el subtítulo.
 */
@Composable
fun AguaMapHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 160.dp,
    innerSpacing: Dp = 0.dp,
    isSplash: Boolean = false
) {
    Box(modifier = modifier.fillMaxWidth()) {
        if (!isSplash) {
            AguaMapLanguage(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 28.dp)
            )
        }
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.gota),
                contentDescription = stringResource(R.string.logo),
                modifier = Modifier.size(logoSize)
            )
            Spacer(modifier = Modifier.size(innerSpacing))
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 50.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Blanco
            )
            if (!isSplash) {
                Text(
                    text = stringResource(R.string.subtitle),
                    fontSize = 20.sp,
                    color = Blanco,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}