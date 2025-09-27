package dev.azagra.gps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CompassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.GRAY
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 50f
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    private val paintSatellite = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLUE
    }

    private var bearing: Float = 0f
    private var satellitesCount: Int = 0

    fun updateCompass(newBearing: Float, satellites: Int) {
        bearing = newBearing
        satellitesCount = satellites
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = Math.min(cx, cy) - 16f

        // Círculo exterior
        canvas.drawCircle(cx, cy, radius, paintCircle)

        // Puntos cardinales
        canvas.drawText("N", cx, cy - radius + 50f, paintText)
        canvas.drawText("S", cx, cy + radius - 10f, paintText)
        canvas.drawText("E", cx + radius - 30f, cy + 15f, paintText)
        canvas.drawText("W", cx - radius + 30f, cy + 15f, paintText)

        // Dibujar satélites como puntos alrededor
        for (i in 0 until satellitesCount) {
            val angle = 360f / (satellitesCount.coerceAtLeast(1)) * i - bearing
            val rad = Math.toRadians(angle.toDouble())
            val sx = (cx + radius * 0.8 * Math.sin(rad)).toFloat()
            val sy = (cy - radius * 0.8 * Math.cos(rad)).toFloat()
            canvas.drawCircle(sx, sy, 10f, paintSatellite)
        }
    }
}
