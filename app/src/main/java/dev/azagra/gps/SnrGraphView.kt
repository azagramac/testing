package dev.azagra.gps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class SnrGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var snrValues: List<Float> = emptyList()

    init {
        paint.style = Paint.Style.FILL
        paint.color = ContextCompat.getColor(context, R.color.snrBar)
    }

    fun setSnrValues(values: List<Float>) {
        snrValues = values
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val barWidth = width / (snrValues.size.coerceAtLeast(1) * 1.5f)
        val maxHeight = height.toFloat()

        snrValues.forEachIndexed { index, snr ->
            val left = index * barWidth * 1.5f
            val top = maxHeight - (snr / 100f) * maxHeight
            val right = left + barWidth
            val bottom = maxHeight
            canvas.drawRect(left, top, right, bottom, paint)
        }
    }
}
