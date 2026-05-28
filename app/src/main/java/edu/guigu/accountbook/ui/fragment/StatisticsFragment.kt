package edu.guigu.accountbook.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.guigu.accountbook.data.dao.CategorySummary
import edu.guigu.accountbook.databinding.FragmentStatisticsBinding
import edu.guigu.accountbook.ui.viewmodel.RecordViewModel
import edu.guigu.accountbook.util.DateUtils
import java.util.Locale

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecordViewModel

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

        viewModel.expenseCategorySummary.observe(viewLifecycleOwner) { summaries ->
            updateCategorySummary(summaries)
        }
    }

    private fun updateBalance() {
        val income = viewModel.totalIncome.value ?: 0.0
        val expense = viewModel.totalExpense.value ?: 0.0
        val balance = income - expense
        binding.tvBalance.text = "¥${DateUtils.formatAmount(balance)}"
    }

    private fun updateCategorySummary(summaries: List<CategorySummary>) {
        binding.pieChart.setSummaries(summaries)
        binding.llCategorySummary.removeAllViews()

        if (summaries.isEmpty()) {
            binding.tvCategoryEmpty.visibility = View.VISIBLE
            binding.llCategorySummary.visibility = View.VISIBLE
            return
        }

        binding.tvCategoryEmpty.visibility = View.GONE
        binding.llCategorySummary.visibility = View.VISIBLE

        val total = summaries.sumOf { it.total }
        summaries.forEach { summary ->
            binding.llCategorySummary.addView(createCategoryLine(summary, total))
        }
    }

    private fun createCategoryLine(summary: CategorySummary, total: Double): View {
        val context = requireContext()
        val percent = if (total > 0) summary.total / total * 100 else 0.0

        return TextView(context).apply {
            text = "• ${summary.category}: ¥${DateUtils.formatAmount(summary.total)} (${String.format(Locale.CHINA, "%.0f%%", percent)})"
            setTextColor(android.graphics.Color.parseColor("#555555"))
            textSize = 12f
            setPadding(0, dp(2), 0, dp(2))
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
