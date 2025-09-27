package dev.azagra.gps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors
import kotlin.math.max

class SnrGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var snrData: List<Float> = emptyList()
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        barPaint.color = getThemeColor(android.R.attr.colorPrimary)
        textPaint.color = getThemeColor(android.R.attr.colorForeground)
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

    private fun getThemeColor(attr: Int): Int {
        return MaterialColors.getColor(this, attr, 0xFF00FF)
    }
}
