package edu.guigu.accountbook.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import edu.guigu.accountbook.data.dao.CategorySummary
import edu.guigu.accountbook.data.dao.MonthlyTrend
import edu.guigu.accountbook.data.model.Record
import edu.guigu.accountbook.databinding.FragmentStatisticsBinding
import edu.guigu.accountbook.ui.viewmodel.RecordViewModel
import edu.guigu.accountbook.util.DateUtils
import java.text.DecimalFormat

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecordViewModel
    private val amountFormatter = DecimalFormat("#,###")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[RecordViewModel::class.java]
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeData() {
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvIncome.text = "¥${DateUtils.formatAmount(income)}"
            updateBalance()
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.tvExpense.text = "¥${DateUtils.formatAmount(expense)}"
            updateBalance()
        }

        viewModel.expenseCategorySummary.observe(viewLifecycleOwner) { summary ->
            setupPieChart(summary)
        }

        viewModel.monthlyTrend.observe(viewLifecycleOwner) { trend ->
            setupLineChart(trend)
        }
    }

    private fun updateBalance() {
        val income = viewModel.totalIncome.value ?: 0.0
        val expense = viewModel.totalExpense.value ?: 0.0
        binding.tvBalance.text = "¥${DateUtils.formatAmount(income - expense)}"
    }

    private fun setupPieChart(summary: List<CategorySummary>) {
        binding.tvCategoryEmpty.visibility = if (summary.isEmpty()) View.VISIBLE else View.GONE

        if (summary.isEmpty()) {
            binding.pieChart.clear()
            binding.pieChart.centerText = "暂无数据"
            return
        }

        val entries = summary.map { PieEntry(it.total.toFloat(), it.category) }
        val dataSet = PieDataSet(entries, "").apply {
            colors = summary.map {
                val color = Record.getCategoryColor(it.category)
                Color.rgb(Color.red(color), Color.green(color), Color.blue(color))
            }
            sliceSpace = 3f
            valueTextSize = 10.5f
            valueTextColor = Color.WHITE
            selectionShift = 0f
            setDrawValues(true)
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                    return "¥${DateUtils.formatAmount(value.toDouble())}"
                }
            })
        }

        binding.pieChart.apply {
            data = pieData
            centerText = "支出分类"
            setCenterTextSize(13f)
            setUsePercentValues(false)
            setDrawEntryLabels(true)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(10.5f)
            description.isEnabled = false
            isHighlightPerTapEnabled = false
            setTouchEnabled(false)
            setExtraOffsets(6f, 0f, 6f, 0f)
            legend.apply {
                isEnabled = true
                textColor = Color.DKGRAY
                textSize = 9.5f
                form = Legend.LegendForm.SQUARE
                formSize = 8f
                xEntrySpace = 6f
                orientation = Legend.LegendOrientation.HORIZONTAL
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                setDrawInside(false)
            }
            isDrawHoleEnabled = true
            holeRadius = 39f
            transparentCircleRadius = 48f
            setTransparentCircleAlpha(90)
            animateY(800)
            invalidate()
        }
    }

    private fun setupLineChart(trend: List<MonthlyTrend>) {
        if (trend.isEmpty()) {
            binding.lineChart.clear()
            return
        }

        val incomeEntries = mutableListOf<Entry>()
        val expenseEntries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        trend.forEachIndexed { index, item ->
            incomeEntries.add(Entry(index.toFloat(), item.income.toFloat()))
            expenseEntries.add(Entry(index.toFloat(), item.expense.toFloat()))
            labels.add(item.month)
        }

        val incomeSet = LineDataSet(incomeEntries, "收入").apply {
            color = Color.parseColor("#2ECC71")
            setCircleColor(Color.parseColor("#2ECC71"))
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 8.5f
            valueTextColor = Color.BLACK
            valueFormatter = AmountLabelFormatter()
        }

        val expenseSet = LineDataSet(expenseEntries, "支出").apply {
            color = Color.parseColor("#E74C3C")
            setCircleColor(Color.parseColor("#E74C3C"))
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 8.5f
            valueTextColor = Color.BLACK
            valueFormatter = AmountLabelFormatter()
        }

        binding.lineChart.apply {
            data = LineData(incomeSet, expenseSet)
            description.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisLeft.textColor = Color.BLACK
            axisLeft.textSize = 9f
            axisLeft.valueFormatter = AxisLabelFormatter()
            xAxis.position = XAxis.XAxisPosition.TOP
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.granularity = 1f
            xAxis.textColor = Color.BLACK
            xAxis.textSize = 9f
            legend.apply {
                textColor = Color.DKGRAY
                textSize = 9.5f
                form = Legend.LegendForm.SQUARE
                formSize = 8f
                orientation = Legend.LegendOrientation.HORIZONTAL
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                setDrawInside(false)
            }
            setExtraOffsets(2f, 2f, 8f, 0f)
            setTouchEnabled(false)
            animateX(800)
            invalidate()
        }
    }

    private inner class AxisLabelFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return amountFormatter.format(value.toDouble())
        }
    }

    private inner class AmountLabelFormatter : ValueFormatter() {
        override fun getPointLabel(entry: Entry?): String {
            return amountFormatter.format(entry?.y?.toDouble() ?: 0.0)
        }
    }
}
