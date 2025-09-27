package dev.azagra.gps

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.use

class SnrGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var snrData: List<Float> = emptyList()
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        context.theme.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.colorPrimary), 0, 0)
            .use { barPaint.color = it.getColor(0, Color.GREEN) }
    }

    fun updateSnrData(data: List<Float>) {
        snrData = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (snrData.isEmpty()) return

        val widthPerBar = width / snrData.size.toFloat()
        val maxSNR = 60f // Ajusta según tu escala

        snrData.forEachIndexed { index, snr ->
            val barHeight = (snr / maxSNR) * height
            val left = index * widthPerBar + 10
            val top = height - barHeight
            val right = (index + 1) * widthPerBar - 10
            canvas.drawRect(left, top, right, height.toFloat(), barPaint)
        }
    }
}
