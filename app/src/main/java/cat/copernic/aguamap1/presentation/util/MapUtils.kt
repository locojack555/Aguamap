package cat.copernic.aguamap1.presentation.util

import android.content.Context
import android.graphics.drawable.BitmapDrawable

object MapUtils {
    fun createDistanceTag(context: Context, text: String): BitmapDrawable {
        val density = context.resources.displayMetrics.density
        val paintText = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 14 * density
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val bounds = android.graphics.Rect()
        paintText.getTextBounds(text, 0, text.length, bounds)
        val width = bounds.width() + (24 * density).toInt()
        val height = bounds.height() + (12 * density).toInt()
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paintRect = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#E3F2FD")
        }
        val paintStroke = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#2196F3")
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2 * density
        }
        val rectF = android.graphics.RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rectF, 8 * density, 8 * density, paintRect)
        canvas.drawRoundRect(rectF, 8 * density, 8 * density, paintStroke)
        canvas.drawText(text, width / 2f, (height / 2f) - bounds.exactCenterY(), paintText)
        return BitmapDrawable(context.resources, bitmap)
    }
}