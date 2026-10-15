package com.aistudio.gestbracciantiw.ui.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val italianLocale = Locale.ITALY

actual fun platformFormatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(italianLocale)
    return formatter.format(amount)
}

actual fun platformFormatDecimal(value: Double, decimals: Int, grouping: Boolean): String {
    val formatter = NumberFormat.getNumberInstance(italianLocale)
    formatter.minimumFractionDigits = decimals
    formatter.maximumFractionDigits = decimals
    formatter.isGroupingUsed = grouping
    return formatter.format(value)
}

actual fun platformFormatDate(timestamp: Long, pattern: String): String {
    val formatter = SimpleDateFormat(pattern, italianLocale)
    return formatter.format(Date(timestamp))
}

actual fun platformGetDayOfWeek(timestamp: Long): Int {
    val cal = Calendar.getInstance(italianLocale).apply { timeInMillis = timestamp }
    return cal.get(Calendar.DAY_OF_WEEK)
}
