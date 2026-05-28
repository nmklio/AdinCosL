package edu.guigu.accountbook.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import edu.guigu.accountbook.data.dao.CategorySummary
import edu.guigu.accountbook.data.model.Record
import edu.guigu.accountbook.util.DateUtils

class ExpensePieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D9D9D9")
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C7C7C7")
        strokeWidth = dp(1f)
        style = Paint.Style.STROKE
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#55FF6B6B")
        style = Paint.Style.FILL
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6B6B")
        strokeWidth = dp(3f)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6B6B")
        style = Paint.Style.FILL
    }

    private val axisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        textAlign = Paint.Align.CENTER
        textSize = sp(10f)
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        textAlign = Paint.Align.CENTER
        textSize = sp(13f)
        isFakeBoldText = true
    }

    private val chartBounds = RectF()
    private val linePath = Path()
    private val fillPath = Path()
    private var summaries: List<CategorySummary> = emptyList()
    private var totalExpense: Double = 0.0

    fun setSummaries(newSummaries: List<CategorySummary>) {
        summaries = newSummaries
        totalExpense = summaries.sumOf { it.total }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        if (summaries.isEmpty() || totalExpense <= 0.0) {
            canvas.drawText("暂无支出数据", width / 2f, height / 2f, titlePaint)
            return
        }

        chartBounds.set(dp(22f), dp(22f), width - dp(18f), height - dp(34f))
        drawGrid(canvas)
        drawLineChart(canvas)
        drawTopCategoryLabel(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        val lines = 3
        repeat(lines + 1) { index ->
            val y = chartBounds.top + chartBounds.height() / lines * index
            canvas.drawLine(chartBounds.left, y, chartBounds.right, y, gridPaint)
        }
    }

    private fun drawLineChart(canvas: Canvas) {
        linePath.reset()
        fillPath.reset()

        val maxAmount = summaries.maxOf { it.total }.coerceAtLeast(1.0)
        val points = summaries.mapIndexed { index, summary ->
            val x = if (summaries.size == 1) {
                chartBounds.centerX()
            } else {
                chartBounds.left + chartBounds.width() / (summaries.size - 1) * index
            }
            val y = chartBounds.bottom - (summary.total / maxAmount * chartBounds.height()).toFloat()
            Triple(summary, x, y)
        }

        points.forEachIndexed { index, (_, x, y) ->
            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, chartBounds.bottom)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        points.lastOrNull()?.let { (_, x, _) ->
            fillPath.lineTo(x, chartBounds.bottom)
            fillPath.close()
        }

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(linePath, linePaint)

        points.forEach { (summary, x, y) ->
            pointPaint.color = Record.getCategoryColor(summary.category)
            canvas.drawCircle(x, y, dp(4f), pointPaint)
            canvas.drawText(summary.category, x, height - dp(12f), axisTextPaint)
        }
    }

    private fun drawTopCategoryLabel(canvas: Canvas) {
        val top = summaries.maxByOrNull { it.total } ?: return
        canvas.drawText("最高：${top.category} ¥${DateUtils.formatAmount(top.total)}", width / 2f, dp(15f), titlePaint)
    }

    private fun dp(value: Float): Float {
        return value * resources.displayMetrics.density
    }

    private fun sp(value: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)
    }
}
