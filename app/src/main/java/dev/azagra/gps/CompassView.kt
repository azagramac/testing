package dev.azagra.gps

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors

class CompassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var azimuth = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val satellitePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var satellites: List<Pair<Float, Float>> = emptyList() // (angle, SNR)

    init {
        // Color adaptativo Material You
        val color = MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, Color.GREEN)
        paint.color = color
        satellitePaint.color = color

        textPaint.color = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, Color.WHITE)
        textPaint.textSize = 40f
        textPaint.textAlign = Paint.Align.CENTER
    }

    fun updateOrientation(newAzimuth: Float) {
        azimuth = newAzimuth
        invalidate()
    }

    fun updateSatellites(satData: List<Pair<Float, Float>>) {
        satellites = satData
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = (Math.min(cx, cy) * 0.9f)

        // Círculo exterior
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        canvas.drawCircle(cx, cy, radius, paint)

        // Cardinales
        val cardinalPoints = listOf("N", "E", "S", "W")
        for (i in cardinalPoints.indices) {
            val angle = Math.toRadians((i * 90 - azimuth).toDouble())
            val x = cx + (radius - 40) * Math.sin(angle).toFloat()
            val y = cy - (radius - 40) * Math.cos(angle).toFloat()
            canvas.drawText(cardinalPoints[i], x, y + 15, textPaint)
        }

        // Dibujar satélites
        satellites.forEach { (angleDeg, _) ->
            val angle = Math.toRadians((angleDeg - azimuth).toDouble())
            val satRadius = radius - 60
            val x = cx + satRadius * Math.sin(angle).toFloat()
            val y = cy - satRadius * Math.cos(angle).toFloat()
            canvas.drawCircle(x, y, 15f, satellitePaint)
        }
    }
}
