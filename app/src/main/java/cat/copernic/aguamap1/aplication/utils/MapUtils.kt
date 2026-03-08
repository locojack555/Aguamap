package cat.copernic.aguamap1.aplication.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable

/**
 * Utilidades para la manipulación de elementos gráficos sobre el mapa.
 * Proporciona métodos para generar recursos visuales dinámicos (como etiquetas de distancia)
 * que no pueden ser simples recursos estáticos.
 */
object MapUtils {

    /**
     * Crea una etiqueta (tag) visual que muestra la distancia a una fuente.
     * Genera un Bitmap con un fondo redondeado azul claro, borde azul oscuro y texto centrado.
     *
     * @param context Contexto de la aplicación para acceder a los recursos y densidad de pantalla.
     * @param text El texto a mostrar (ej: "150 m" o "1.2 km").
     * @return Un BitmapDrawable listo para ser usado como marcador o superposición en Google Maps.
     */
    fun createDistanceTag(context: Context, text: String): BitmapDrawable {
        val density = context.resources.displayMetrics.density

        // 1. Configuración del pincel para el texto
        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 14 * density
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        // 2. Cálculo de dimensiones dinámicas según el tamaño del texto
        val bounds = Rect()
        paintText.getTextBounds(text, 0, text.length, bounds)

        val paddingHorizontal = 24 * density
        val paddingVertical = 12 * density

        val width = bounds.width() + paddingHorizontal.toInt()
        val height = bounds.height() + paddingVertical.toInt()

        // 3. Creación del lienzo (Canvas)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 4. Configuración de pinceles para el fondo y el borde
        val paintRect = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E3F2FD") // Azul muy claro (fondo)
        }

        val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2196F3") // Azul Google (borde)
            style = Paint.Style.STROKE
            strokeWidth = 2 * density
        }


        // 5. Dibujo del fondo redondeado y el borde
        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val cornerRadius = 8 * density
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paintRect)
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paintStroke)

        // 6. Dibujo del texto centrado vertical y horizontalmente
        // Se usa exactCenterY para corregir el desfase de la línea base del texto
        canvas.drawText(
            text,
            width / 2f,
            (height / 2f) - bounds.exactCenterY(),
            paintText
        )

        return BitmapDrawable(context.resources, bitmap)
    }
}