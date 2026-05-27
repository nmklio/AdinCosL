package edu.guigu.accountbook.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val DATE_PATTERN = "yyyy年MM月dd日"

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat(DATE_PATTERN, Locale.CHINA)
        return sdf.format(Date(timestamp))
    }

    fun formatAmount(amount: Double): String {
        return String.format(Locale.CHINA, "%.2f", amount)
    }
}
