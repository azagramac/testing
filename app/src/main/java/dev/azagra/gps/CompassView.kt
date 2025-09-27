package dev.azagra.gps

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.R
import kotlin.math.min

class CompassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var azimuth: Float = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val arrowPath = Path()

    private var satellites: List<Pair<Float, Float>> = emptyList()

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = resolveColor(android.R.attr.colorOnSurface)

        textPaint.color = resolveColor(android.R.attr.colorOnSurface)
        textPaint.textSize = 48f
        textPaint.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) * 0.8f

        // Fondo circular
        paint.style = Paint.Style.FILL
        paint.color = resolveColor(android.R.attr.colorSurface)
        canvas.drawCircle(cx, cy, radius, paint)

        // Borde
        paint.style = Paint.Style.STROKE
        paint.color = resolveColor(android.R.attr.colorOnSurface)
        canvas.drawCircle(cx, cy, radius, paint)

        // Flecha norte
        canvas.save()
        canvas.rotate(-azimuth, cx, cy)
        paint.style = Paint.Style.FILL
        paint.color = resolveColor(android.R.attr.colorPrimary)
        arrowPath.reset()
        arrowPath.moveTo(cx, cy - radius * 0.9f)
        arrowPath.lineTo(cx - 20f, cy)
        arrowPath.lineTo(cx + 20f, cy)
        arrowPath.close()
        canvas.drawPath(arrowPath, paint)
        canvas.restore()

        // Texto N/E/S/W
        paint.style = Paint.Style.FILL
        paint.color = resolveColor(android.R.attr.colorOnSurface)
        textPaint.color = resolveColor(android.R.attr.colorOnSurface)
        canvas.drawText("N", cx, cy - radius + 60f, textPaint)
        canvas.drawText("S", cx, cy + radius - 20f, textPaint)
        canvas.drawText("E", cx + radius - 30f, cy + 15f, textPaint)
        canvas.drawText("W", cx - radius + 30f, cy + 15f, textPaint)

        // Satélites (opcional)
        paint.style = Paint.Style.FILL
        paint.color = resolveColor(android.R.attr.colorSecondary)
        satellites.forEach { (angle, snr) ->
            val rad = Math.toRadians(angle.toDouble() - azimuth)
            val satX = cx + radius * 0.6f * Math.sin(rad).toFloat()
            val satY = cy - radius * 0.6f * Math.cos(rad).toFloat()
            canvas.drawCircle(satX, satY, 10f, paint)
        }
    }

    fun updateOrientation(newAzimuth: Float) {
        azimuth = newAzimuth
        invalidate()
    }

    fun updateSatellites(satData: List<Pair<Float, Float>>) {
        satellites = satData
        invalidate()
    }

    private fun resolveColor(attr: Int): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attr))
        val color = typedArray.getColor(0, Color.MAGENTA)
        typedArray.recycle()
        return color
    }
}
