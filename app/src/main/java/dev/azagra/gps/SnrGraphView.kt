package dev.azagra.gps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class SnrGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var snrData: List<Float> = emptyList()
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        barPaint.color = resolveColor(android.R.attr.colorPrimary)
        textPaint.color = resolveColor(android.R.attr.colorOnSurface)
        textPaint.textSize = 32f
        textPaint.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (snrData.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val barWidth = w / max(snrData.size, 1)

        val maxSnr = max(snrData.maxOrNull() ?: 0f, 50f)

        snrData.forEachIndexed { index, snr ->
            val barHeight = (snr / maxSnr) * h
            val left = index * barWidth
            val top = h - barHeight
            val right = left + barWidth * 0.8f
            val bottom = h
            canvas.drawRect(left, top, right, bottom, barPaint)
        }
    }

    fun updateSnrData(newSnrData: List<Float>) {
        snrData = newSnrData
        invalidate()
    }

    private fun resolveColor(attr: Int): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attr))
        val color = typedArray.getColor(0, 0xFF00FF)
        typedArray.recycle()
        return color
    }
}
