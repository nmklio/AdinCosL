package edu.guigu.accountbook.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.guigu.accountbook.R
import edu.guigu.accountbook.databinding.ItemRecordBinding

/**
 * RecyclerView 适配器：把数据"画"到列表的每一行
 *
 * RecyclerView 工作原理（类比电影院）：
 * - Adapter    = 放映员（负责把数据画面放入每一帧）
 * - ViewHolder = 放映机（持有控件引用，复用不毁坏）
 * - LayoutManager = 座椅排列方式（Linear 纵向 / Grid 网格）
 */
class RecordAdapter : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    // ===== 假数据（今天先用着，明天换真的） =====
    data class FakeRecord(
        val icon: String,      // 首字
        val iconColor: Int,    // 背景色
        val category: String,  // 分类
        val date: String,      // 日期
        val note: String?,     // 备注
        val amount: String,    // 金额文字
        val amountColor: Int   // 金额颜色
    )

    private val records = listOf(
        FakeRecord("餐", 0xFFFF6B6B.toInt(), "餐饮", "2025年05月24日", "午餐外卖",
            "-¥35.50", 0xFFE74C3C.toInt()),
        FakeRecord("薪", 0xFF2ECC71.toInt(), "工资", "2025年05月15日", null,
            "+¥8000.00", 0xFF2ECC71.toInt()),
        FakeRecord("交", 0xFF4ECDC4.toInt(), "交通", "2025年05月23日", "地铁通勤",
            "-¥12.00", 0xFFE74C3C.toInt()),
        FakeRecord("购", 0xFF45B7D1.toInt(), "购物", "2025年05月22日", null,
            "-¥258.00", 0xFFE74C3C.toInt()),
        FakeRecord("娱", 0xFF96CEB4.toInt(), "娱乐", "2025年05月20日", "电影票",
            "-¥79.90", 0xFFE74C3C.toInt()),
    )

    override fun getItemCount(): Int = records.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(records[position])
    }

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemRecordBinding.bind(itemView)

        fun bind(record: FakeRecord) {
            binding.tvCategoryIcon.text = record.icon
            binding.tvCategoryIcon.background.mutate()?.let { drawable ->
                (drawable as? android.graphics.drawable.GradientDrawable)?.setColor(record.iconColor)
            }

            binding.tvCategoryName.text = record.category
            binding.tvDate.text = record.date

            if (record.note != null) {
                binding.tvNote.text = "备注：${record.note}"
                binding.tvNote.visibility = View.VISIBLE
            } else {
                binding.tvNote.visibility = View.GONE
            }

            binding.tvAmount.text = record.amount
            binding.tvAmount.setTextColor(record.amountColor)
        }
    }
}
