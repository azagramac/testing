package dev.azagra.gps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SnrGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var snrValues: List<Float> = emptyList()

    private val paintBar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.MAGENTA
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    fun updateData(values: List<Float>) {
        snrValues = values
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (snrValues.isEmpty()) return

        val barWidth = width.toFloat() / snrValues.size
        val maxSNR = snrValues.maxOrNull() ?: 1f
        val scale = height / (maxSNR * 1.1f) // dejar margen

        snrValues.forEachIndexed { index, snr ->
            val left = index * barWidth + barWidth * 0.1f
            val right = (index + 1) * barWidth - barWidth * 0.1f
            val top = height - snr * scale
            val bottom = height.toFloat()
            canvas.drawRect(left, top, right, bottom, paintBar)
            canvas.drawText("%.0f".format(snr), (left + right) / 2, top - 8f, paintText)
        }
    }
}
