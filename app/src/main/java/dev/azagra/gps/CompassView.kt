package dev.azagra.gps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class CompassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var heading = 0f

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = ContextCompat.getColor(context, R.color.foregroundColor)
    }

    fun setHeading(angle: Float) {
        heading = angle
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = width.coerceAtMost(height) / 2f - 10
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)

        // Flecha brújula
        paint.color = ContextCompat.getColor(context, R.color.foregroundColor)
        canvas.save()
        canvas.rotate(heading, width / 2f, height / 2f)
        canvas.drawLine(width / 2f, height / 2f, width / 2f, height / 2f - radius, paint)
        canvas.restore()
    }
}
