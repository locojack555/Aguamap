package cat.copernic.aguamap1.presentation.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import cat.copernic.aguamap1.domain.model.Fountain
import cat.copernic.aguamap1.ui.theme.Blanco
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Naranja
import cat.copernic.aguamap1.ui.theme.Rojo
import cat.copernic.aguamap1.ui.theme.Verde

/**
 * Determina el color del marcador basado en el estado de la fuente.
 */
fun Fountain.getMarkerColor(
): Int {
    return when {
        !isOperational -> Rojo.toArgb()
        status == "PENDING" -> Naranja.toArgb()
        category == "Bebible" -> Blue10.toArgb()
        else -> Verde.toArgb()
    }
}

/**
 * Crea un icono con fondo blanco y tinte de color.
 */
fun createFountainIcon(context: Context, resId: Int, tintColor: Int): Drawable {
    val background = ContextCompat.getDrawable(context, resId)?.mutate()?.apply {
        DrawableCompat.setTint(DrawableCompat.wrap(this), Blanco.toArgb())
    }

    val foreground = ContextCompat.getDrawable(context, resId)?.mutate()?.apply {
        DrawableCompat.setTint(DrawableCompat.wrap(this), tintColor)
    }

    return LayerDrawable(arrayOf(background, foreground).filterNotNull().toTypedArray())
}